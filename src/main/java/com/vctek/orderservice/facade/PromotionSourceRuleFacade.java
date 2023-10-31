package com.vctek.orderservice.facade;

import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.CommercePromotionData;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PromotionSourceRuleFacade {
    PromotionSourceRuleDTO createNew(PromotionSourceRuleDTO request);

    Page<PromotionSourceRuleDTO> findAll(PromotionRuleSearchParam param);

    void changeStatus(PromotionStatusRequest promotionStatusRequest);

    PromotionSourceRuleDTO findBy(Long promotionId, Long companyId);

    PromotionSourceRuleDTO update(PromotionSourceRuleDTO promotionSourceRuleDTO);

    byte[] exportCurrentPage(PromotionRuleSearchParam param);

    void doExportAllPage(PromotionRuleSearchParam param);

    List<PromotionSourceRuleDTO> findAllActivePromotionsForStorefront(Long companyId);

    CommercePromotionData getDetail(Long companyId, Long promotionId);

    PromotionSourceRuleDTO findById(Long promotionSourceRuleId, Long companyId);
}
