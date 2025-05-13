package com.example.ws.productMicroservice;

import com.example.ws.core.event.ProductCreateEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class IdempotenceProducerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, ProductCreateEvent> kafkaTemplate;

    @MockitoBean
    private KafkaAdmin kafkaAdmin;

    @Test
    void testProducerConfig_whenIdempotenceEnabled_assertsIdempotenceProperties() {
        // Arrange
        ProducerFactory<String, ProductCreateEvent> producerFactory = kafkaTemplate.getProducerFactory();
        // Act
        Map<String, Object> props = producerFactory.getConfigurationProperties();
        // Asserts

        assertEquals("true", props.get("enable.idempotence"));
        assertEquals("5", props.get("max.in.flight.requests.per.connection"));
        assertTrue((Integer) props.get("retries") > 0);
        assertEquals("all", props.get("acks"));
    }
}
