package com.kafka.producer.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kafka.producer.domain.LibraryEvent;
import com.kafka.producer.domain.LibraryEventType;
import com.kafka.producer.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/libraryevents")
@Slf4j
public class LibraryEventApi {

    private LibraryEventProducer eventProducer;

    public LibraryEventApi(LibraryEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @PostMapping
    public ResponseEntity<LibraryEvent> create(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {

        // invoke kafka async producer
        /*log.info("sendLibraryEventAsync start");
        eventProducer.sendLibraryEventAsync(libraryEvent);
        log.info("sendLibraryEventAsync end");*/

        // invoke kafka async producer
        /*log.info("sendLibraryEventSync start");
        eventProducer.sendLibraryEventSync(libraryEvent);
        log.info("sendLibraryEventSync end");*/

        // invoke kafka async producer
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        eventProducer.sendLibraryEventAsyncUsingProducerRecord(libraryEvent);

        // return
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {

        if (libraryEvent.getLibraryEventId()==null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please pass LibraryEventID");
        }

        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        eventProducer.sendLibraryEventAsyncUsingProducerRecord(libraryEvent);

        // return
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }
}
