package com.example.ws.emailNotificationMicroservice.handler;

import com.example.ws.core.event.ProductCreateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "product-created-event-topic")
public class ProductCreatedEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @KafkaHandler
    public void handle(ProductCreateEvent productCreateEvent) {
        LOGGER.info("Received event: {}", productCreateEvent.getTitle());
    }
}
