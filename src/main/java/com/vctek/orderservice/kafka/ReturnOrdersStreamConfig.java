package com.vctek.orderservice.kafka;

import com.vctek.kafka.stream.ReturnOrdersKafkaOutStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({ReturnOrdersKafkaOutStream.class})
public class ReturnOrdersStreamConfig {

}
