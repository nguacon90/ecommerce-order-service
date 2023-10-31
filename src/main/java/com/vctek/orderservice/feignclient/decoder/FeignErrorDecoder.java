package com.vctek.orderservice.feignclient.decoder;

import com.google.gson.Gson;
import com.vctek.dto.ErrorData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if(response.status() != HttpStatus.OK.value() && response.status() != HttpStatus.NO_CONTENT.value()
                && response.body() != null) {
            try {
                String body = Util.toString(response.body().asReader());
                ErrorData errorData = new Gson().fromJson(body, ErrorData.class);
                Object[] args = CollectionUtils.isNotEmpty(errorData.getArgs()) ? errorData.getArgs().toArray() : null;
                throw new ServiceException(errorData.getError(), errorData.getMessage(), response.status(), args);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                ErrorCodes err = ErrorCodes.INTERNAL_SERVER_ERROR;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}
