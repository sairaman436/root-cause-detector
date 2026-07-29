/*
 * Purpose: Configures Kafka producers, consumers, retry handling, dead-letter routing, and topic creation.
 * Why it exists: The event bus needs consistent serialization, resilient consumers, and governed topics.
 * Architecture fit: Infrastructure adapter for the Milestone 5 event streaming backbone.
 */
package com.airural.platform.core.events.infrastructure;

import com.airural.platform.shared.events.EventTopic;
import java.util.*;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.*;
import org.springframework.kafka.core.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

/** Kafka infrastructure configuration for platform events. */
@Configuration
@EnableKafka
public class KafkaEventConfiguration {
    private final String bootstrapServers;
    private final String clientId;
    private final String consumerGroup;
    private final boolean consumersEnabled;

    public KafkaEventConfiguration(
            @Value("${airural.events.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${airural.events.kafka.client-id}") String clientId,
            @Value("${airural.events.kafka.consumer-group}") String consumerGroup,
            @Value("${airural.events.kafka.consumers-enabled:false}") boolean consumersEnabled) {
        this.bootstrapServers = bootstrapServers;
        this.clientId = clientId;
        this.consumerGroup = consumerGroup;
        this.consumersEnabled = consumersEnabled;
    }

    /** Creates Kafka topics and matching dead-letter topics. */
    @Bean
    @ConditionalOnProperty(prefix = "airural.events.kafka", name = "enabled", havingValue = "true")
    public KafkaAdmin.NewTopics platformTopics() {
        List<NewTopic> topics = new ArrayList<>();
        for (EventTopic topic : EventTopic.values()) {
            topics.add(new NewTopic(topic.topicName(), 3, (short) 1));
            topics.add(new NewTopic(topic.topicName() + ".dlq", 3, (short) 1));
        }
        return new KafkaAdmin.NewTopics(topics.toArray(NewTopic[]::new));
    }

    /** Configures JSON Kafka producer serialization. */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /** Provides KafkaTemplate for event publishing and dead-letter routing. */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /** Configures JSON Kafka consumer deserialization. */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.airural.platform.shared.events,java.util,java.lang");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /** Configures listener retry and dead-letter behavior. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setAutoStartup(consumersEnabled);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".dlq", record.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L)));
        return factory;
    }
}
