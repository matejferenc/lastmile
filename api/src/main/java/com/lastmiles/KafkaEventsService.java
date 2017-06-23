package com.lastmiles;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import cz.atlascon.travny.data.BinaryWriter;
import cz.atlascon.travny.records.CustomRecord;
import cz.atlascon.travny.records.Record;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by trehak on 31.5.17.
 */
public class KafkaEventsService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventsService.class);
    private final String kafkaBootstrapServers;
    private final Map<String, KafkaTopic> konsumers = Maps.newConcurrentMap();
    private final ExecutorService pollers = Executors.newFixedThreadPool(4);
    private final ScheduledExecutorService mainLoop = Executors.newScheduledThreadPool(1);
    private final ConcurrentMap<Class, KafkaTopic> topics = Maps.newConcurrentMap();
    private final KafkaProducer<byte[], byte[]> producer;
    private final long pollTimeoutMillis;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public KafkaEventsService(String kafkaBootstrapServers) {
        this.pollTimeoutMillis = 200;
        LOGGER.info("Creating kafka consumer factory, bootstrap servers {}, threads {}", kafkaBootstrapServers, 2);
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.producer = createProducer();
        mainLoop.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (String id : konsumers.keySet()) {
                    try {
                        KafkaTopic k = konsumers.get(id);
                        if (k != null) {
                            k.refreshPartitionConsumers();
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Exception rebalancing consumer!", e);
                    }
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public <E extends CustomRecord> void startPolling(Class<E> cls, Consumer<E> consumer) {
        pollers.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while (running.get()) {
                    List<E> list = poll(cls);
                    for (E e : list) {
                        consumer.accept(e);
                    }
                }
                return null;
            }
        });
    }

    public <E extends CustomRecord> List<E> poll(Class<E> cls) {
        final List<E> recs = Lists.newArrayList();
        KafkaTopic topic = topics.computeIfAbsent(cls, c -> {
            try {
                return createConsumer(cls);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Collection<KafkaTopicPartition> topicPartitions = topic.getTopicPartitions();
        for (KafkaTopicPartition ktp : topicPartitions) {
            List<E> l = ktp.poll();
            recs.addAll(l);
        }
        return recs;
    }

    private KafkaProducer<byte[], byte[]> createProducer() {
        Map<String, Object> producerProps = Maps.newHashMap();
        producerProps.put("bootstrap.servers", kafkaBootstrapServers);
        producerProps.put("acks", "all");
        producerProps.put("block.on.buffer.full", true);
        producerProps.put("retries", Integer.MAX_VALUE);
        return new KafkaProducer<>(producerProps, new ByteArraySerializer(), new ByteArraySerializer());
    }

    /**
     * Produce raw event to topic, does not add any prefix
     * <p>
     * Does not prepend prefix
     *
     * @param topic
     * @param key
     * @param value
     * @return
     */
    private Future<RecordMetadata> produceRawToTopic(String topic, byte[] key, byte[] value) {
        Future<RecordMetadata> future = producer.send(new ProducerRecord<>(topic, key, value));
        return future;
    }

    /**
     * Produces record to specified topic name, prefixes with "events."
     *
     * @param record
     * @return
     */
    private Future<RecordMetadata> produce(String topic, Record record) throws IOException {
        return produceRawToTopic(topic, null, BinaryWriter.toBytes(record));
    }

    /**
     * Produces record to topic with same name as record, prefixes with "events."
     *
     * @param record
     * @return
     */
    public Future<RecordMetadata> produce(Record record) throws IOException {
        return produce(record.getSchema().getName(), record);
    }


    public void close(String konsumerId) throws Exception {
        KafkaTopic existing = konsumers.get(konsumerId);
        if (existing == null) {
            return;
        }
        konsumers.remove(konsumerId);
        existing.close();
    }

    public KafkaTopic getExisting(String id) {
        return konsumers.get(id);
    }

    /**
     * Creates new Kafka consumer for topic
     *
     * @return
     */
    public <E extends CustomRecord> KafkaTopic<E> createConsumer(Class<E> cls) throws IOException {
        Map<String, Object> consumerProps = Maps.newHashMap();
        consumerProps.put("bootstrap.servers", kafkaBootstrapServers);
        consumerProps.put("enable.auto.commit", false);
        consumerProps.put("poll.timeout", pollTimeoutMillis);
        KafkaTopic konsumer = new KafkaTopic(kafkaBootstrapServers, cls, pollTimeoutMillis);
        konsumer.refreshPartitionConsumers();
        konsumers.put(konsumer.getId(), konsumer);
        konsumer.getTopicPartitions().forEach(ktp -> {
            KafkaTopicPartition kkk = (KafkaTopicPartition) ktp;
            try {
                kkk.seek(kkk.getMinOffset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return konsumer;
    }

    @Override
    public void close() throws Exception {
        LOGGER.info("Shutting down polling threads");
        running.set(false);
        mainLoop.shutdown();
        mainLoop.awaitTermination(10, TimeUnit.SECONDS);
        LOGGER.info("Closing konsumers");
        for (KafkaTopic kt : konsumers.values()) {
            kt.close();
        }
        LOGGER.info("Closing konsumers executor service");
        LOGGER.info("Closing producer");
        producer.close(10, TimeUnit.SECONDS);
        MoreExecutors.shutdownAndAwaitTermination(pollers, 10, TimeUnit.SECONDS);
    }

}