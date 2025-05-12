package com.example.ws.productMicroservice;

import com.example.ws.core.event.ProductCreateEvent;
import com.example.ws.productMicroservice.service.ProductService;
import com.example.ws.productMicroservice.service.dto.CreateProductDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
@SpringBootTest(properties =
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {

    @Autowired
    Environment environment;
    @Autowired
    ProductService productService;
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;
    KafkaMessageListenerContainer<String, ProductCreateEvent> listenerContainer;
    BlockingDeque<ConsumerRecord<String, ProductCreateEvent>> records;

    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> consumerFactory =
                new DefaultKafkaConsumerFactory<>(getConsumerProps());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("product-created-event-topic-name"));
        listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingDeque<>();
        listenerContainer.setupMessageListener((MessageListener<String, ProductCreateEvent>)records::add);
        listenerContainer.start();
        ContainerTestUtils.waitForAssignment(listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }


    @Test
    void createProduct_whenValidProductDetails_thenSuccessfullySendMessageKafka() throws ExecutionException, InterruptedException {
        // Arrange

        CreateProductDto productDto = new CreateProductDto(
                "IDEA",
                BigDecimal.valueOf(100),
                99);

        // Act
        productService.createProduct(productDto);

        // Asserts
        ConsumerRecord<String, ProductCreateEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertNotNull(message.key());

        ProductCreateEvent event = message.value();
        assertEquals(productDto.getTitle(), event.getTitle());
        assertEquals(productDto.getPrice(), event.getPrice());
        assertEquals(productDto.getQuantity(), event.getQuantity());
    }

    private Map<String, Object> getConsumerProps() {
        return Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("consumer.group-id"),
                JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")
        );
    }

    @AfterAll
    void afterAll() {
        listenerContainer.stop();
    }
}
