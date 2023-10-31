package com.vctek.orderservice.controller;

import com.vctek.orderservice.facade.PromotionKieModuleFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/promotion-modules")
public class PromotionKieModuleController extends AbstractController {
    private PromotionKieModuleFacade promotionKieModuleFacade;

    public PromotionKieModuleController(PromotionKieModuleFacade promotionKieModuleFacade) {
        this.promotionKieModuleFacade = promotionKieModuleFacade;
    }

    @PostMapping("/init")
    public ResponseEntity initPromotionModule(@RequestParam Long companyId) {
        promotionKieModuleFacade.init(companyId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
