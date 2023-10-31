package com.vctek.orderservice.kafka;

import com.vctek.kafka.stream.OrderStatusKafkaOutStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({OrderStatusKafkaOutStream.class})
public class OrderStatusTopicStreamConfig {

}
