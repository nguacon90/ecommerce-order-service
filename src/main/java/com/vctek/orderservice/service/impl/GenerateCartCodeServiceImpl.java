package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.GenerateCartCodeService;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class GenerateCartCodeServiceImpl implements GenerateCartCodeService {


    @Override
    public String generateCartCode(AbstractOrderModel newCart) {
        StringBuilder builder = new StringBuilder();
        builder.append(newCart.getCompanyId());
        builder.append(DateUtil.getDateStr(Calendar.getInstance().getTime(), DateUtil.DDMMYY_PATTERN));
        builder.append(newCart.getId());
        return builder.toString();
    }

}
