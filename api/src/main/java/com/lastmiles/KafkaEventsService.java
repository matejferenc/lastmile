package com.lastmiles;

import com.google.common.collect.Maps;
import cz.atlascon.travny.data.BinaryWriter;
import cz.atlascon.travny.records.Record;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by trehak on 31.5.17.
 */
public class KafkaEventsService implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventsService.class);
    private final String kafkaBootstrapServers;
    private final Map<String, KafkaTopic> konsumers = Maps.newConcurrentMap();
    private final ScheduledExecutorService mainLoop = Executors.newScheduledThreadPool(1);
    private final KafkaProducer<byte[], byte[]> producer;
    private final long pollTimeoutMillis;
    private final AtomicBoolean running = new AtomicBoolean();

    public KafkaEventsService(String kafkaBootstrapServers,
                              int kafkaProcessingThreads,
                              int kafkaQueueSize,
                              long pollTimeoutMillis) {
        this.pollTimeoutMillis = pollTimeoutMillis;
        LOGGER.info("Creating kafka consumer factory, bootstrap servers {}, threads {}", kafkaBootstrapServers, kafkaProcessingThreads);
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
     * @param topic
     * @return
     */
    public KafkaTopic createConsumer(String topic) throws IOException {
        Map<String, Object> consumerProps = Maps.newHashMap();
        consumerProps.put("bootstrap.servers", kafkaBootstrapServers);
        consumerProps.put("enable.auto.commit", false);
        consumerProps.put("topic", topic);
        consumerProps.put("poll.timeout", pollTimeoutMillis);
        KafkaTopic konsumer = new KafkaTopic(kafkaBootstrapServers, topic, pollTimeoutMillis);
        konsumer.refreshPartitionConsumers();
        konsumers.put(konsumer.getId(), konsumer);
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
    }

}
