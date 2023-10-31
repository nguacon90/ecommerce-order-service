package com.vctek.orderservice.kafka.consumer;

import com.vctek.kafka.data.CustomerCouponDto;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.service.KafkaListener;
import com.vctek.kafka.stream.CustomerCouponKafkaInStream;
import com.vctek.orderservice.service.CustomerCouponService;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerCouponListener implements KafkaListener<CustomerCouponDto> {

    private CustomerCouponService customerCouponService;

    public CustomerCouponListener(CustomerCouponService customerCouponService) {
        this.customerCouponService = customerCouponService;
    }

    @Override
    @StreamListener(CustomerCouponKafkaInStream.CUSTOMER_COUPON_TOPIC_IN)
    public void handleMessage(KafkaMessage<CustomerCouponDto> kafkaMessage) {
        CustomerCouponDto dto = kafkaMessage.getContent();
        if(dto.getCompanyId() == null || dto.getCouponId() == null || dto.getCustomerId() == null) {
            return;
        }

        customerCouponService.saveCustomerCoupon(dto);
    }
}
