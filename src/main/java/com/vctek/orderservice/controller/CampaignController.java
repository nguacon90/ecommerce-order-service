package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.CampaignData;
import com.vctek.orderservice.facade.CampaignFacade;
import com.vctek.validate.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {
    private CampaignFacade campaignFacade;
    private Validator<CampaignData> campaignValidator;

    public CampaignController(CampaignFacade campaignFacade, Validator<CampaignData> campaignValidator) {
        this.campaignFacade = campaignFacade;
        this.campaignValidator = campaignValidator;
    }

    @PostMapping
    public ResponseEntity<CampaignData> createNew(@RequestBody CampaignData campaignData) {
        campaignValidator.validate(campaignData);
        CampaignData data = campaignFacade.createNew(campaignData);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{campaignId}")
    public ResponseEntity<CampaignData> update(CampaignData campaignData, @PathVariable("campaignId") Long campaignId) {
        campaignData.setId(campaignId);
        campaignValidator.validate(campaignData);
        CampaignData data = campaignFacade.update(campaignData);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CampaignData>> findAll(@RequestParam("companyId") Long companyId,
                                                      @RequestParam(value = "status", required = false) String status) {
        List<CampaignData> data = campaignFacade.findAll(companyId, status);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }
}
