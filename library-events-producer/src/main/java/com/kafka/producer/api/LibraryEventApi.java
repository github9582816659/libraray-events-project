package com.kafka.producer.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.producer.domain.LibraryEvent;
import com.kafka.producer.producer.LibraryEventProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/libraryevents")
public class LibraryEventApi {

    private LibraryEventProducer eventProducer;

    public LibraryEventApi(LibraryEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @PostMapping
    public ResponseEntity<LibraryEvent> create(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        // invoke kafka producer
        eventProducer.sendLibraryEvent(libraryEvent);

        // return
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
