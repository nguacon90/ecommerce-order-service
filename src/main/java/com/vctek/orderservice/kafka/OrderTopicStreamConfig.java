package com.vctek.orderservice.kafka;

import com.vctek.kafka.stream.OrderKafkaInStream;
import com.vctek.kafka.stream.OrderKafkaOutStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({OrderKafkaOutStream.class, OrderKafkaInStream.class})
public class OrderTopicStreamConfig {

}
