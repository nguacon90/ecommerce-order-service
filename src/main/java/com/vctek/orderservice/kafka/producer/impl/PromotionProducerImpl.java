package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.CouponDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.kafka.data.promotion.PromotionDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.promotion.PromotionOutStream;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.kafka.producer.PromotionProducer;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PromotionProducerImpl implements PromotionProducer {
    private KafkaProducerService kafkaProducerService;
    private PromotionOutStream promotionOutStream;
    private Converter<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRuleConverter;
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleConditionPopulator;
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleActionPopulator;
    private Converter<CouponModel, CouponDTO> couponDTOConverter;

    @Override
    public void sendPromotionToKafka(PromotionSourceRuleModel sourceRuleModel) {
        PromotionDTO promotionDTO = new PromotionDTO();
        PromotionSourceRuleDTO sourceRuleDTO = basicPromotionSourceRuleConverter.convert(sourceRuleModel);
        promotionSourceRuleConditionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        promotionSourceRuleActionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        promotionDTO.setPromotionSourceRuleDTO(sourceRuleDTO);
        promotionDTO.setCreatedBy(sourceRuleModel.getCreatedBy());
        promotionDTO.setCreatedDate(sourceRuleModel.getCreatedDate());
        promotionDTO.setModifiedBy(sourceRuleModel.getModifiedBy());
        promotionDTO.setModifiedDate(sourceRuleModel.getModifiedDate());
        promotionDTO.setStatus(sourceRuleModel.getStatus());

        KafkaMessage<PromotionDTO> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(promotionDTO);
        kafkaMessage.setType(KafkaMessageType.DIM_PROMOTION);
        kafkaProducerService.send(kafkaMessage, promotionOutStream.produceTopic());
    }

    @Override
    public void sendCouponToKafka(CouponModel couponModel) {
        PromotionDTO promotionDTO = new PromotionDTO();
        CouponDTO couponDTO = couponDTOConverter.convert(couponModel);
        promotionDTO.setCouponDTO(couponDTO);

        KafkaMessage<PromotionDTO> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(promotionDTO);
        kafkaMessage.setType(KafkaMessageType.DIM_COUPON);
        kafkaProducerService.send(kafkaMessage, promotionOutStream.produceTopic());
    }

    @Autowired
    public void setKafkaProducerService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Autowired
    public void setPromotionOutStream(PromotionOutStream promotionOutStream) {
        this.promotionOutStream = promotionOutStream;
    }

    @Autowired
    public void setBasicPromotionSourceRuleConverter(Converter<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRuleConverter) {
        this.basicPromotionSourceRuleConverter = basicPromotionSourceRuleConverter;
    }

    @Autowired
    @Qualifier("promotionSourceRuleConditionPopulator")
    public void setPromotionSourceRuleConditionPopulator(Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleConditionPopulator) {
        this.promotionSourceRuleConditionPopulator = promotionSourceRuleConditionPopulator;
    }

    @Autowired
    @Qualifier("promotionSourceRuleActionPopulator")
    public void setPromotionSourceRuleActionPopulator(Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleActionPopulator) {
        this.promotionSourceRuleActionPopulator = promotionSourceRuleActionPopulator;
    }

    @Autowired
    public void setCouponDTOConverter(Converter<CouponModel, CouponDTO> couponDTOConverter) {
        this.couponDTOConverter = couponDTOConverter;
    }
}
