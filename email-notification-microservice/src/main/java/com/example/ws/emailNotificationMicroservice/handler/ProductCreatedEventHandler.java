package com.example.ws.emailNotificationMicroservice.handler;

import com.example.ws.core.event.ProductCreateEvent;
import com.example.ws.emailNotificationMicroservice.exception.NonRetryableException;
import com.example.ws.emailNotificationMicroservice.exception.RetryableException;
import com.example.ws.emailNotificationMicroservice.persistence.entity.ProcessedEventEntity;
import com.example.ws.emailNotificationMicroservice.persistence.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RestTemplate restTemplate;
    private final ProcessedEventRepository processedEventRepository;

    public ProductCreatedEventHandler(RestTemplate restTemplate,
                                      ProcessedEventRepository processedEventRepository) {
        this.restTemplate = restTemplate;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaHandler
    @Transactional
    public void handle(@Payload ProductCreateEvent productCreateEvent,
                       @Header("messageId") String messageId,
                       @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {
        LOGGER.info("Received event: {}", productCreateEvent.getTitle());

        ProcessedEventEntity entity = processedEventRepository.findByMessageId(messageId);
        if (entity != null) {
            LOGGER.info("Received duplicate with messageId: {}", messageId);
            return;
        }

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange("http://localhost:8090/response/200", HttpMethod.GET, null, String.class);
            if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                LOGGER.info("Received response: {}", response.getBody());
            }
        } catch (ResourceAccessException e) {
            LOGGER.error(e.getMessage());
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {
            LOGGER.error(e.getMessage());
            throw new NonRetryableException(e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new NonRetryableException(e);
        }

        try {
            processedEventRepository.save(new ProcessedEventEntity(messageId, productCreateEvent.getProductId()));
        } catch (DataIntegrityViolationException e) {
            LOGGER.error(e.getMessage());
            throw new NonRetryableException(e);
        }
    }
}
