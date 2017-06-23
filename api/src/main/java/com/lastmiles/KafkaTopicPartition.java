package com.lastmiles;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.Futures;
import cz.atlascon.travny.data.BinaryReader;
import cz.atlascon.travny.records.CustomRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 *
 *
 */
public class KafkaTopicPartition<E extends CustomRecord> implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicPartition.class);
    private final String konsumerId;
    private final ExecutorService executorService;
    private final int partition;
    private final long pollTimeout;
    private final TopicPartition topicPartition;
    private final KafkaConsumer konsumer;
    private String kafkaBootstrapServers;
    private volatile long offset;
    private Class<E> cls;


    public KafkaTopicPartition(Class<E> cls,
                               String konsumerId,
                               String kafkaBootstrapServers,
                               int partition,
                               long pollTimeout,
                               ExecutorService executorService) throws IOException {
        this.cls = cls;
        Preconditions.checkNotNull(konsumerId);
        Preconditions.checkArgument(partition >= 0);
        Preconditions.checkArgument(pollTimeout >= 0);
        Preconditions.checkNotNull(executorService);
        Preconditions.checkNotNull(kafkaBootstrapServers);
        this.executorService = executorService;
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.topicPartition = new TopicPartition(cls.getCanonicalName(), partition);
        this.konsumerId = konsumerId;
        this.partition = partition;
        this.pollTimeout = pollTimeout;
        Future<KafkaConsumer> konsumerF = this.executorService.submit(() -> {
            LOGGER.info("Creating consumer for partition {}, topic {}", partition, cls.getCanonicalName());
            Map<String, Object> props = Maps.newHashMap();
            props.put("bootstrap.servers", kafkaBootstrapServers);
            props.put("enable.auto.commit", false);
            KafkaConsumer konsumer = new KafkaConsumer(props, new ByteArrayDeserializer(), new ByteArrayDeserializer());
            konsumer.assign(Sets.newHashSet(topicPartition));
            return konsumer;
        });
        this.konsumer = Futures.getChecked(konsumerF, IOException.class);
        this.offset = getMinOffset();
        seek(offset);
    }

    public List<E> poll() {
        final List<E> records = Lists.newCopyOnWriteArrayList();
        Future<Integer> f = poll(new Consumer<ConsumerRecord<byte[], byte[]>>() {
            @Override
            public void accept(ConsumerRecord<byte[], byte[]> consumerRecord) {
                try {
                    E rec = BinaryReader.fromBytes(consumerRecord.value(), cls);
                    records.add(rec);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        Futures.getUnchecked(f);
        return records;
    }

    /**
     * Poll data
     *
     * @return future of how many records were consumed
     */
    public Future<Integer> poll(Consumer<ConsumerRecord<byte[], byte[]>> consumer) {
        Preconditions.checkNotNull(consumer);
        Future<Integer> future = executorService.submit(() -> {
            ConsumerRecords records = konsumer.poll(pollTimeout);
            if (records.count() > 0) {
                List<ConsumerRecord<byte[], byte[]>> all = records.records(topicPartition);
                for (ConsumerRecord<byte[], byte[]> r : all) {
                    consumer.accept(r);
                }
                doCommit(all.get(all.size() - 1).offset());
            }
            return records.count();
        });
        return future;
    }

    private void doCommit(long offset) {
        konsumer.commitSync(ImmutableMap.of(topicPartition, new OffsetAndMetadata(offset)));
        this.offset = offset;

    }

    public static String generateId() {
        byte[] id = new byte[32];
        new Random().nextBytes(id);
        return BaseEncoding.base32Hex().encode(id);
    }


    public String getId() {
        return konsumerId;
    }

    @Override
    public void close() throws Exception {
        Future<Void> future = executorService.submit(() -> {
            konsumer.close();
            return null;
        });
        // await
        Futures.getUnchecked(future);
    }

    public long getMinOffset() throws IOException {
        Future<Long> future = executorService.submit(() -> {
            Map<TopicPartition, Long> map = konsumer.beginningOffsets(Sets.newHashSet(topicPartition));
            return map.get(topicPartition);
        });
        return Futures.getChecked(future, IOException.class);
    }

    public long getMaxOffset() throws IOException {
        Future<Long> future = executorService.submit(() -> {
            Map<TopicPartition, Long> map = konsumer.endOffsets(Sets.newHashSet(topicPartition));
            return map.get(topicPartition);
        });
        return Futures.getChecked(future, IOException.class);
    }

    public void seek(long offset) {
        Future<Void> seek = executorService.submit(() -> {
            konsumer.seek(topicPartition, offset);
            doCommit(offset);
            return null;
        });
        Futures.getUnchecked(seek);
    }

    public String getTopic() {
        return cls.getCanonicalName();
    }

    public long offset() {
        return offset;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }
}
