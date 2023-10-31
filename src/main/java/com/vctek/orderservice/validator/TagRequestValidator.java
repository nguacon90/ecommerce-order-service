package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.TagService;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TagRequestValidator implements Validator<TagData> {
    private TagService service;
    private int maxTagLength;

    @Override
    public void validate(TagData request) throws ServiceException {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (StringUtils.isBlank(request.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_TAG_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        request.setName(request.getName().trim());
        if(request.getName().length() > maxTagLength) {
            ErrorCodes err = ErrorCodes.TAG_NAME_OVER_MAX_LENGTH;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{maxTagLength});
        }

        List<TagModel> modelList = service.findByCompanyIdAndName(request.getCompanyId(), request.getName());
        if (request.getId() != null) {
            TagModel model = service.findByIdAndCompanyId(request.getId(), request.getCompanyId());
            if (model == null) {
                ErrorCodes err = ErrorCodes.INVALID_TAG;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            modelList = modelList.stream().filter(i -> !i.getId().equals(request.getId())).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(modelList)) {
            ErrorCodes err = ErrorCodes.EXISTED_TAG_NAME;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setService(TagService service) {
        this.service = service;
    }

    @Value("${vctek.config.maxTagLength:255}")
    public void setMaxTagLength(int maxTagLength) {
        this.maxTagLength = maxTagLength;
    }
}
