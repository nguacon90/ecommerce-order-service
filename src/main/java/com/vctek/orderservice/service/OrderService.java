package com.vctek.orderservice.service;

import com.vctek.kafka.data.BillDto;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.migration.dto.PaidAmountOrderData;
import com.vctek.orderservice.dto.AddTagRequest;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.SaleQuantity;
import com.vctek.orderservice.dto.UpdateReturnOrderBillDTO;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.SaleQuantityRequest;
import com.vctek.orderservice.dto.request.storefront.CountOrderData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.util.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface OrderService extends AbstractOrderService<OrderModel, OrderEntryModel> {
    OrderModel findById(Long id);

    OrderModel createOrderFromCart(CartModel cartModel);

    void cloneSubOrderEntries(AbstractOrderEntryModel originEntry, AbstractOrderEntryModel cloneEntry);

    void cloneToppingOptions(AbstractOrderEntryModel originEntry, AbstractOrderEntryModel cloneEntry);

    void transferPromotionsToOrder(Set<PromotionResultModel> promotionResults, OrderModel orderModel);

    void transferCouponCodeToOrder(Set<OrderHasCouponCodeModel> appliedCouponCodes, OrderModel orderModel);

    OrderEntryModel cloneOrderEntry(OrderModel orderModel, AbstractOrderEntryModel originEntry);

    OrderModel save(OrderModel orderModel);

    void refresh(OrderModel orderModel);

    OrderModel findByCodeAndCompanyId(String code, Long companyId);

    Page<OrderModel> findAll(Pageable pageable);

    Page<OrderModel> findAllByFromDate(Pageable pageable, Date fromDate);

    Page<OrderModel> findAllByAndCompanyIdFromDate(Long companyId, Date fromDate, Pageable pageable);

    OrderModel findByCodeAndCompanyIdAndDeleted(String code, Long companyId,  boolean deleted);

    OrderModel findByCodeAndCompanyIdAndOrderTypeAndDeleted(String code, Long companyId, String orderType, boolean deleted);

    void resetPreAndHoldingStockOf(OrderModel orderModel);

    OrderEntryModel findEntryBy(OrderModel orderModel, Integer entryNumber);

    OrderEntryModel saveEntry(OrderEntryModel entryModel);

    List<SaleQuantity> findAllSaleEntryBy(SaleQuantityRequest request);

    Page<OrderModel> findAllByCompanyIdAndCreateTime(Long companyId, Date fromDate, Date toDate, Pageable pageable);

    Page<OrderModel> findAllByCompanyIdAndTypeAndFromDate(Pageable pageable, Long companyId, String type, Date fromDate);

    void updatePaidAmountOrder(OrderModel order);

    /**
     * Using only for Reindex Paid amount
     * @param pageable
     * @param name
     * @return
     */
    Page<OrderModel> findAllByCompanyIdAndType(Pageable pageable, Long companyId, String name);

    List<SaleQuantity> findAllSaleComboEntries(SaleQuantityRequest request);

    List<OrderModel> findOrderCombo(Long companyId);

    double updateAndCalculateDiffRevertAmountOfReturnEntries(UpdateReturnOrderBillDTO updateReturnOrderBillDTO);

    Page<OrderModel> findAllByCompanyId(Long companyId, Pageable pageable);

    void linkBillToOrder(List<OrderBillLinkDTO> content);

    void updatePaidAmountOrder(List<PaidAmountOrderData> content);

    Page<OrderModel> findAllByAndCompanyIdAndOrderTypes(Long companyId, List<String> orderTypes, Pageable pageable);

    List<OrderModel> findByCompanyIdAndOrderCodeIn(Long companyId, List<String> orderCodes);

    OrderEntryModel findEntryBy(Long entryId, OrderModel order);

    void saveAll(List<OrderModel> updateExchangeOrders);

    List<OrderModel> updateOnlineOriginBasePrice(OrderReportRequest orderReportRequest, Pageable pageable);

    OrderModel findOrderByExternalIdAndSellSignal(CartInfoParameter cartInfoParameter);

    void updateOrderBill(BillDto billDto);

    void holdingStockAndResetPreStockOf(List<OrderEntryModel> entries);

    void resetPreAndHoldingStockOfEntries(List<OrderEntryModel> entries);

    void cloneSubOrderEntriesForKafkaImportOrderStatus(AbstractOrderEntryModel entryModel, OrderEntryModel cloneEntry);

    void cloneToppingOptionsForKafkaImportOrderStatus(AbstractOrderEntryModel entryModel, OrderEntryModel cloneEntry);

    void cloneSettingCustomerOption(OrderModel order, OrderModel cloneRetailOrderModel);

    void transferPromotionsToOrderForKafkaImportOrderStatus(OrderModel order, OrderModel cloneRetailOrderModel);

    void transferCouponCodeToOrderForKafkaImportOrderStatus(OrderModel order, OrderModel cloneRetailOrderModel);

    OrderModel cloneOrderFormModel(AbstractOrderModel model);

    void updateLockOrder(OrderModel orderModel, boolean lockOrder);

    void updateLockOrders(Long companyId, List<String> orderCodes, boolean lock);

    void addTag(AddTagRequest addTagRequest);

    void removeTag(Long companyId, String orderCode, Long tagId);

    Page<OrderModel> findAllByCompanyIdAndOrderStatus(Pageable pageable, Long companyId, OrderStatus preOrder);

    List<CountOrderData> storefrontCountOrderByUser(OrderSearchRequest request);
}
