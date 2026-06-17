package org.example.bookservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "order-topic")
class BookServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
