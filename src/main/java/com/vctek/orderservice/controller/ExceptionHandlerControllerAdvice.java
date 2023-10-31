package com.vctek.orderservice.controller;

import com.vctek.dto.ErrorData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerControllerAdvice.class);

    @ExceptionHandler(value = ServiceException.class)
    public ResponseEntity<ErrorData> handleServiceException(ServiceException e) {
        ErrorData err = new ErrorData(e.getCode(), e.getMessage());
        if(ArrayUtils.isNotEmpty(e.getArgs())) {
            err.setArgs(Arrays.stream(e.getArgs()).map(a -> String.valueOf(a)).collect(Collectors.toList()));
        }
        return new ResponseEntity<>(err, HttpStatus.valueOf(e.getHttpStatus()));
    }

    @ExceptionHandler(value = DataAccessException.class)
    public ResponseEntity<ErrorData> handleDataAccessException(DataAccessException e) {
        LOG.error(e.getMessage(), e);
        ErrorCodes err = ErrorCodes.INTERNAL_SERVER_ERROR;
        ErrorData errorData = new ErrorData(err.code(), err.message());
        return new ResponseEntity<>(errorData, HttpStatus.valueOf(err.httpStatus()));
    }

    @ExceptionHandler(value = SQLException.class)
    public ResponseEntity<ErrorData> handleSQLException(SQLException e) {
        LOG.error(e.getMessage(), e);
        ErrorCodes err = ErrorCodes.INTERNAL_SERVER_ERROR;
        ErrorData errorData = new ErrorData(err.code(), err.message());
        return new ResponseEntity<>(errorData, HttpStatus.valueOf(err.httpStatus()));
    }
}
