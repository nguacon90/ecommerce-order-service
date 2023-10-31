package com.vctek.orderservice.promotionengine.ruleengineservice.util;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractActionedRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ShipmentRAO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Component
public class RaoUtils {
    private static final String ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE = "actionedRao object is not expected to be NULL here";

    public Set<DiscountRAO> getDiscounts(AbstractActionedRAO actionedRao) {
        Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);
        Set<DiscountRAO> result = new LinkedHashSet();
        if (CollectionUtils.isNotEmpty(actionedRao.getActions())) {
            actionedRao.getActions().stream().filter((action) -> action instanceof DiscountRAO)
                    .forEachOrdered((action) -> result.add((DiscountRAO)action));
        }

        return result;
    }

    public Optional<ShipmentRAO> getShipment(AbstractActionedRAO actionedRao) {
        Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);
        Optional<ShipmentRAO> shipmentRao = Optional.empty();
        if (CollectionUtils.isNotEmpty(actionedRao.getActions())) {
            shipmentRao = actionedRao.getActions().stream()
                    .filter((action) -> action instanceof ShipmentRAO)
                    .map((action) -> (ShipmentRAO)action).findFirst();
        }

        return shipmentRao;
    }

    public void addAction(AbstractActionedRAO actionedRao, AbstractRuleActionRAO action) {
        Preconditions.checkNotNull(actionedRao, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);
        Preconditions.checkNotNull(action, ACTIONED_RAO_OBJECT_NULL_ERROR_MESSAGE);
        action.setAppliedToObject(actionedRao);
        LinkedHashSet<AbstractRuleActionRAO> actions = actionedRao.getActions();
        if (Objects.isNull(actions)) {
            actions = new LinkedHashSet();
            actionedRao.setActions(actions);
        }

        actions.add(action);
    }

    public boolean isAbsolute(DiscountRAO discount) {
        return StringUtils.isNotEmpty(discount.getCurrencyIsoCode());
    }
}
