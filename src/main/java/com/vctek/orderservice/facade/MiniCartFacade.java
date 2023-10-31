package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.MiniCartData;

import java.util.List;

public interface MiniCartFacade {

    List<MiniCartData> findAllByUser(CartInfoParameter cartInfoParameter);
}
