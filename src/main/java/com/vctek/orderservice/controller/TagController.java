package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.facade.TagFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagController {
    private TagFacade facade;
    private Validator<TagData> tagValidator;

    public TagController(TagFacade facade) {
        this.facade = facade;
    }

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, T(com.vctek.util.PermissionCodes).MANAGER_TAG_FOR_ONLINE_ORDER.code())")
    public ResponseEntity<TagData> createOrUpdate(@RequestBody TagData request) {
        tagValidator.validate(request);
        TagData data = facade.createOrUpdate(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Page<TagData>> findAllBy(TagData tagData,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<TagData> data = facade.findAllBy(tagData, pageable);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setTagValidator(Validator<TagData> tagValidator) {
        this.tagValidator = tagValidator;
    }
}
