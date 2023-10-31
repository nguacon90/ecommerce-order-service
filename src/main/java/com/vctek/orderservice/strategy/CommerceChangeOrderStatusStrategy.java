package com.vctek.orderservice.strategy;

import com.vctek.orderservice.dto.CommerceChangeOrderStatusModification;
import com.vctek.orderservice.dto.CommerceChangeOrderStatusParameter;

public interface CommerceChangeOrderStatusStrategy {
    CommerceChangeOrderStatusModification changeToHigherStatus(CommerceChangeOrderStatusParameter parameter);

    CommerceChangeOrderStatusModification changeToLowerStatus(CommerceChangeOrderStatusParameter parameter);

    void changeStatusOrder(CommerceChangeOrderStatusParameter parameter);

    void importChangeStatusOrder(CommerceChangeOrderStatusParameter parameter, Long modifiedBy);
}
