package com.example.ws.productMicroservice.service;

import com.example.ws.core.event.ProductCreateEvent;
import com.example.ws.productMicroservice.service.dto.CreateProductDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ProductServiceImpl implements ProductService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final  KafkaTemplate<String, ProductCreateEvent> kafkaTemplate;

    public ProductServiceImpl(KafkaTemplate<String, ProductCreateEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductDto createProductDto) throws ExecutionException, InterruptedException {
        // TODO save db
        final String productId = UUID.randomUUID().toString();
        final ProductCreateEvent event = new ProductCreateEvent(
                        productId,
                        createProductDto.getTitle(),
                        createProductDto.getPrice(),
                        createProductDto.getQuantity());

        final ProducerRecord<String, ProductCreateEvent> producerRecord = new ProducerRecord<>(
                "product-created-event-topic",
                productId,
                event
        );

//        producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());
        producerRecord.headers().add("messageId", "qwerty".getBytes());

        final SendResult<String, ProductCreateEvent> result =
                kafkaTemplate.send(producerRecord).get();

        final RecordMetadata recordMetadata = result.getRecordMetadata();
        LOGGER.info("Partition: {}", recordMetadata.partition());
        LOGGER.info("Topic: {}", recordMetadata.topic());
        LOGGER.info("Offset: {}", recordMetadata.offset());

        // Асинхронная отправка
//        future.whenComplete((result, exception) -> {
//            if (exception != null) {
//                LOGGER.error("Failed to send message: {}", event);
//            } else {
//                LOGGER.info("Message send successfully: {}", result.getRecordMetadata());
//            }
//        });

        LOGGER.info("Return productId: {}", productId);
        return productId;
    }
}
