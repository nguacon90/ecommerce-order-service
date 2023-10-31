package com.vctek.orderservice.promotionengine.ruleengine.concurrency;

import java.io.Serializable;

public interface TaskResult extends Serializable {
    TaskResultState getState();
}
