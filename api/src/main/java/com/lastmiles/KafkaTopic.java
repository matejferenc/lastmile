package com.lastmiles;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


/**
 * Created by trehak on 12.6.17.
 */
public class KafkaTopic implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopic.class);
    private final String kafkaBootstrapServers;
    private final String topic;
    private final String id;
    private final long pollTimeout;
    private final KafkaConsumer metadataKonsumer;
    private final ReentrantLock lock = new ReentrantLock();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<Integer, KafkaTopicPartition> topicPartitionMap = Maps.newConcurrentMap();

    public KafkaTopic(String kafkaBootstrapServers,
                      String topic,
                      long pollTimeout) throws IOException {
        this.id = KafkaTopicPartition.generateId();
        this.kafkaBootstrapServers = kafkaBootstrapServers;
        this.topic = topic;
        this.pollTimeout = pollTimeout;
        Preconditions.checkNotNull(topic);
        Preconditions.checkArgument(pollTimeout >= 0);
        Preconditions.checkNotNull(executorService);
        Preconditions.checkNotNull(kafkaBootstrapServers);
        Future<KafkaConsumer> konsumerF = executorService.submit(() -> {
            LOGGER.info("Creating metadata consumer for topic {}", topic);
            Map<String, Object> props = Maps.newHashMap();
            props.put("bootstrap.servers", kafkaBootstrapServers);
            props.put("enable.auto.commit", false);
            return new KafkaConsumer(props, new ByteArrayDeserializer(), new ByteArrayDeserializer());
        });
        this.metadataKonsumer = Futures.getChecked(konsumerF, IOException.class);
    }

    public void refreshPartitionConsumers() throws IOException {
        lock.lock();
        try {
            Set<Integer> partitions = getPartitions();
            Sets.SetView<Integer> missing = Sets.difference(partitions, topicPartitionMap.keySet());
            for (Integer p : missing) {
                KafkaTopicPartition kafkaTopicPartition = new KafkaTopicPartition(KafkaTopicPartition.generateId(), kafkaBootstrapServers, topic, p, pollTimeout, executorService);
                topicPartitionMap.put(p, kafkaTopicPartition);
            }
        } finally {
            lock.unlock();
        }
    }

    public Set<Integer> getPartitions() throws IOException {
        Future<Set<Integer>> f = executorService.submit(() -> {
            List<PartitionInfo> pis = (List<PartitionInfo>) metadataKonsumer.listTopics().get(topic);
            if (pis == null) {
                return Sets.newHashSet();
            }
            return pis.stream().map(p -> p.partition()).collect(Collectors.toSet());
        });
        Set<Integer> partitions = Futures.getChecked(f, IOException.class);
        return partitions;
    }

    @Override
    public void close() throws Exception {
        for (KafkaTopicPartition ktp : topicPartitionMap.values()) {
            try {
                ktp.close();
            } catch (Exception e) {
                LOGGER.warn("Exception closing " + ktp);
            }
        }
        Future<Void> f = executorService.submit(() -> {
            metadataKonsumer.close(30, TimeUnit.SECONDS);
            return null;
        });
        Futures.getUnchecked(f);
    }

    public String getId() {
        return id;
    }

    public Collection<KafkaTopicPartition> getTopicPartitions() {
        return Lists.newArrayList(topicPartitionMap.values());
    }

    public KafkaTopicPartition getTopicPartition(int partition) {
        return topicPartitionMap.get(partition);
    }
}
