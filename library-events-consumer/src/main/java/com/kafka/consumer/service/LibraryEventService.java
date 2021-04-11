package com.kafka.consumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.consumer.entity.LibraryEvent;
import com.kafka.consumer.repository.LibraryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
@Slf4j
public class LibraryEventService {

    private LibraryEventRepository libraryEventRepository;
    private ObjectMapper objectMapper;
    private KafkaTemplate<Integer, String> kafkaTemplate;

    public LibraryEventService(LibraryEventRepository libraryEventRepository, ObjectMapper objectMapper, KafkaTemplate<Integer, String> kafkaTemplate) {
        this.libraryEventRepository = libraryEventRepository;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("LibraryEvent : {}", libraryEvent);

        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.info("Invalid LibraryEventType");
                break;
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library Event ID is missing");
        }

        if (!libraryEventRepository.findById(libraryEvent.getLibraryEventId()).isPresent()) {
            throw new IllegalArgumentException("Not a valid Library Event");
        }
    }

    @Transactional
    void save(LibraryEvent libraryEvent) {
        log.info("libraryEvent : {}", libraryEvent);
        log.info("book : {}", libraryEvent.getBook());
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventRepository.save(libraryEvent);
        log.info("Successfully saved library event {}", libraryEvent);
    }

    public void handleRecovery(ConsumerRecord<Integer, String> record) {
        Integer key = record.key();
        String value = record.value();

        // send will take topic name as first argument and we can send to n no of topics
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key,value,ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key,value,result);
            }
        });
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in handleFailure : {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key : {} and the value is {}, partition is {}", key,value, result.getRecordMetadata().partition());
    }
}
