package com.kafka.producer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.producer.domain.Book;
import com.kafka.producer.domain.LibraryEvent;
import com.kafka.producer.domain.LibraryEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @InjectMocks
    LibraryEventProducer eventProducer;

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendLibraryEventAsyncUsingProducerRecord_failure() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Choudhary,Rahul")
                .bookName("Spring Kafka")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();

        //when
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //then
        assertThrows(Exception.class, () -> eventProducer.sendLibraryEventAsyncUsingProducerRecord(libraryEvent).get());
    }

    @Test
    void sendLibraryEventAsyncUsingProducerRecord_success() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Choudhary,Rahul")
                .bookName("Spring Kafka")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();

        //when
        String record = objectMapper.writeValueAsString(libraryEvent);

        SettableListenableFuture future = new SettableListenableFuture();

        ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("library-events", libraryEvent.getLibraryEventId(), record);
        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1),
                1,
                1,
                334,
                System.currentTimeMillis(),
                1,
                2);
        SendResult<Integer, String> sendResult = new SendResult<Integer, String>(producerRecord, recordMetadata);
        future.set(sendResult);
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        //then
        ListenableFuture<SendResult<Integer, String>> listenableFuture = eventProducer.sendLibraryEventAsyncUsingProducerRecord(libraryEvent);
        SendResult<Integer, String> integerStringSendResult = listenableFuture.get();
        assert integerStringSendResult.getRecordMetadata().partition()==1;
    }
}
