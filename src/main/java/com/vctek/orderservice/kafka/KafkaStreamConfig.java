package com.vctek.orderservice.kafka;

import com.vctek.kafka.stream.*;
import com.vctek.kafka.stream.loyalty.*;
import com.vctek.kafka.stream.promotion.PromotionOutStream;
import com.vctek.migration.kafka.stream.SyncBIllInStream;
import com.vctek.migration.kafka.stream.SyncInvoiceLinkInStream;
import com.vctek.migration.kafka.stream.SyncInvoiceLinkOutStream;
import com.vctek.migration.kafka.stream.SyncOrderHistoryInStream;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding({UpdateReturnOrderInStream.class, UpdateReturnOrderOutStream.class, ReturnOrdersKafkaInStream.class,
        LoyaltyTransactionOutStream.class, LoyaltyInvoiceOutStream.class,
        ProductInfoInStream.class, InvoiceKafkaInStream.class,
        MigratePaymentMethodKafkaOutStream.class, SyncBIllInStream.class,
        MigrateOrderBillLinkInStream.class, MigrateOrderBillLinkOutStream.class,
        SyncOrderHistoryInStream.class, SyncInvoiceLinkInStream.class,
        SyncInvoiceLinkOutStream.class, UpdatePaidAmountOrderInStream.class, UpdatePaidAmountOrderOutStream.class,
        PromotionOutStream.class, RecalculateOrderReportOutStream.class, OrderSettingCustomerKafkaOutStream.class,
        LoyaltyRewardRequestOutStream.class, LoyaltyRewardResponseInStream.class, BillInStream.class,
        CustomerKafkaInStream.class, LoyaltyTransactionResponseInStream.class, OrderProcessKafkaOutStream.class,
        OrderProcessInStream.class, OrderProcessResultInStream.class, ProductInventoryOutStream.class,
        CustomerCouponKafkaInStream.class})
public class KafkaStreamConfig {

}
