package com.kafka.consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.consumer.service.LibraryEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LibraryEventConsumerManualOffsetAck implements AcknowledgingMessageListener<Integer, String> {

    private LibraryEventService libraryEventService;

    public LibraryEventConsumerManualOffsetAck(LibraryEventService libraryEventService) {
        this.libraryEventService = libraryEventService;
    }

    @Override
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord consumerRecord, Acknowledgment acknowledgment) {
        log.info("ConsumerRecord : {}",consumerRecord);

        try {
            libraryEventService.create(consumerRecord);
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
