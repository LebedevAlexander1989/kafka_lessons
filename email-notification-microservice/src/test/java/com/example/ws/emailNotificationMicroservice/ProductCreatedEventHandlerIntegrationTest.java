package com.example.ws.emailNotificationMicroservice;

import com.example.ws.core.event.ProductCreateEvent;
import com.example.ws.emailNotificationMicroservice.handler.ProductCreatedEventHandler;
import com.example.ws.emailNotificationMicroservice.persistence.entity.ProcessedEventEntity;
import com.example.ws.emailNotificationMicroservice.persistence.repository.ProcessedEventRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@EmbeddedKafka
@ActiveProfiles("test")
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductCreatedEventHandlerIntegrationTest {

    @MockitoSpyBean
    private ProductCreatedEventHandler productCreatedEventHandler;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void testProductCreatedEventHandler_onProductCreated_handleEvents() throws ExecutionException, InterruptedException {
        // Arrange
        ProductCreateEvent productCreateEvent = new ProductCreateEvent(
                UUID.randomUUID().toString(),
                "test",
                BigDecimal.valueOf(100),
                1
        );

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreateEvent.getProductId();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "product-created-event-topic",
                messageKey,
                productCreateEvent
        );

        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        when(processedEventRepository.findByMessageId(anyString())).thenReturn(new ProcessedEventEntity());
        when(processedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);

        when(restTemplate.exchange(
                any(String.class),
                any(HttpMethod.class),
                isNull(),
                eq(String.class))).thenReturn(new ResponseEntity<>("", new HttpHeaders(), HttpStatus.OK));

        //Act
        kafkaTemplate.send(record).get();

        //Assert
        ArgumentCaptor<String> captorMessageId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captorMessageKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductCreateEvent> captorProductCreatedEvent = ArgumentCaptor.forClass(ProductCreateEvent.class);

        verify(productCreatedEventHandler,
                timeout(5000).times(1)).handle(
                        captorProductCreatedEvent.capture(),
                captorMessageId.capture(),
                captorMessageKey.capture());

        assertEquals(messageId, captorMessageId.getValue());
        assertEquals(messageKey, captorMessageKey.getValue());
        assertEquals(productCreateEvent.getProductId(), captorProductCreatedEvent.getValue().getProductId());
    }
}
