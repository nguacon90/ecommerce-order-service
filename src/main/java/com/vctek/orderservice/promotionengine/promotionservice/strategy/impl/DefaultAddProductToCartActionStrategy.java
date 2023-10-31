package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAddComboParameter;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.dto.ProductInFreeGiftComboData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedOrderAddProductActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.OrderUtils;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.ProductData;
import com.vctek.util.ComboType;
import com.vctek.util.CommonUtils;
import com.vctek.util.ProductTypeSell;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("defaultAddProductToCartActionStrategy")
public class DefaultAddProductToCartActionStrategy extends AbstractRuleActionStrategy<RuleBasedOrderAddProductActionModel> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAddProductToCartActionStrategy.class);
    private CartService cartService;
    private OrderService orderService;
    private OrderUtils orderUtils;
    private ProductService productService;
    private BillService billService;
    private Map<String, List<ProductInFreeGiftComboData>> comboWithProductMap = new ConcurrentHashMap<>();

    public DefaultAddProductToCartActionStrategy(ModelService modelService, PromotionActionService promotionActionService,
                                                 CalculationService calculationService) {
        super(modelService, promotionActionService, calculationService);
    }

    public String generateComboFreeGiftKey(String orderCode, Long productId) {
        return orderCode + CommonUtils.UNDERSCORE + productId;
    }

    @Override
    public Class<RuleBasedOrderAddProductActionModel> forClass() {
        return RuleBasedOrderAddProductActionModel.class;
    }

    @Override
    public List<PromotionResultModel> apply(AbstractRuleActionRAO action) {
        if (!(action instanceof FreeProductRAO)) {
            LOG.warn("cannot apply {}, action is not of type FreeProductRAO, but {}", this.getClass().getSimpleName(), action);
            return Collections.emptyList();
        }

        FreeProductRAO freeAction = (FreeProductRAO) action;
        if (!(freeAction.getAppliedToObject() instanceof CartRAO)) {
            LOG.warn("cannot apply {}, appliedToObject is not of type CartRAO, but {}", this.getClass().getSimpleName(), action.getAppliedToObject());
            return Collections.emptyList();
        }

        if (isValidToApplyFreeProduct(freeAction)) {
            OrderEntryRAO addedOrderEntryRao = freeAction.getAddedOrderEntry();
            PromotionResultModel promoResult = this.getPromotionActionService().createPromotionResult(action);
            if (promoResult == null) {
                LOG.warn("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
                return Collections.emptyList();
            }

            AbstractOrderModel order = promoResult.getOrder();
            if (Objects.isNull(order)) {
                LOG.warn("cannot apply {}, order or cart not found: {}", this.getClass().getSimpleName(), order);
                return Collections.emptyList();
            }
            Long productId = addedOrderEntryRao.getProduct().getId();
            AbstractOrderEntryModel abstractOrderEntry = (AbstractOrderEntryModel) doAddProductToOrder(addedOrderEntryRao, order, productId);
            abstractOrderEntry.setGiveAway(Boolean.TRUE);
            populatePriceAndDiscount(productId, abstractOrderEntry);
            addedOrderEntryRao.setEntryNumber(abstractOrderEntry.getEntryNumber());
            RuleBasedOrderAddProductActionModel actionModel = this.createOrderAddProductAction(action, addedOrderEntryRao.getQuantity(), productId, promoResult);
            this.handleActionMetadata(action, actionModel);
            this.getModelService().saveAll(new Object[]{promoResult, actionModel, order, abstractOrderEntry});
            if (order instanceof OrderModel) {
                OrderModel orderModel = (OrderModel) order;
                if(billService.shouldUpdateBillOf(orderModel)) {
                    subtractStockOfFreeGift(abstractOrderEntry, orderModel);
                }

            }
            return Collections.singletonList(promoResult);
        }

        LOG.warn("cannot apply {}, addedOrderEntry.product.code is not set.", this.getClass().getSimpleName());
        return Collections.emptyList();
    }

    private void subtractStockOfFreeGift(AbstractOrderEntryModel abstractOrderEntry, OrderModel orderModel) {
        if(orderService.isComboEntry(abstractOrderEntry)) {
            billService.updateComboInReturnBillWithOrder(orderModel, abstractOrderEntry);
            return;
        }

        billService.addProductToReturnBill(orderModel, abstractOrderEntry);
    }

    private Object doAddProductToOrder(OrderEntryRAO addedOrderEntryRao, AbstractOrderModel order, Long productId) {
        String key = generateComboFreeGiftKey(order.getCode(), productId);
        if (order instanceof CartModel) {
            CartEntryModel cartEntryModel = this.getCartService().addNewEntry((CartModel) order, productId, addedOrderEntryRao.getQuantity(), true);
            cartService.addProductToComboInPromotion(cartEntryModel, comboWithProductMap.get(key));
            CommerceAddComboParameter param = new CommerceAddComboParameter();
            param.setEntryModel(cartEntryModel);
            param.setAbstractOrderModel(order);
            param.setQuantityToAdd(addedOrderEntryRao.getQuantity());
            param.setProductComboId(productId);
            cartService.doAddComboToCart(param);
            comboWithProductMap.remove(key);
            return cartEntryModel;
        }

        OrderEntryModel orderEntryModel = this.orderService.addNewEntry((OrderModel) order, productId, addedOrderEntryRao.getQuantity(), true);
        orderService.addProductToComboInPromotion(orderEntryModel, comboWithProductMap.get(key));
        CommerceAddComboParameter param = new CommerceAddComboParameter();
        param.setEntryModel(orderEntryModel);
        param.setAbstractOrderModel(order);
        param.setQuantityToAdd(addedOrderEntryRao.getQuantity());
        param.setProductComboId(productId);
        orderService.doAddComboToCart(param);
        comboWithProductMap.remove(key);
        return orderEntryModel;
    }

    private boolean isValidToApplyFreeProduct(FreeProductRAO freeAction) {
        if(!hasFreeProduct(freeAction)) {
            return false;
        }
        OrderEntryRAO addedOrderEntry = freeAction.getAddedOrderEntry();
        Long productId = addedOrderEntry.getProduct().getId();
        ProductData basicProductDetail = productService.getBasicProductDetail(productId);
        if(ProductTypeSell.STOP_SELLING.toString().equalsIgnoreCase(basicProductDetail.getTypeSell())) {
            return false;
        }
        AbstractOrderModel order = this.getPromotionActionService().getOrder(freeAction);
        if(order == null) {
            return false;
        }

        try {
            ProductIsCombo productIsCombo = productService.checkIsCombo(productId, order.getCompanyId(), addedOrderEntry.getQuantity());
            if(productIsCombo.isCombo()) {
                productService.checkAvailableToSale(productIsCombo, order);
            }
        } catch (RuntimeException e) {
            return false;
        }
        return true;
    }

    private boolean hasFreeProduct(FreeProductRAO freeAction) {
        return freeAction.getAddedOrderEntry() != null &&
                freeAction.getAddedOrderEntry().getProduct() != null &&
                freeAction.getAddedOrderEntry().getProduct().getId() != null;
    }

    private void populatePriceAndDiscount(Long productId, AbstractOrderEntryModel abstractOrderEntry) {
        try {
            PriceData priceData = productService.getPriceOfProduct(productId, 0);
            String priceType = abstractOrderEntry.getOrder().getPriceType();
            Double price = priceData != null && priceData.getPrice() != null ? priceData.getPrice() : 0;
            if (PriceType.WHOLESALE_PRICE.toString().equals(priceType) && priceData != null && priceData.getWholesalePrice() != null) {
                price = priceData.getWholesalePrice();
            }
            abstractOrderEntry.setBasePrice(price);
        } catch (ServiceException e) {
            abstractOrderEntry.setBasePrice(0d);
            LOG.error("=== DefaultAddProductToCartActionStrategy ERROR: " + e.getMessage());
        }
        abstractOrderEntry.setDiscount(100d);
        abstractOrderEntry.setDiscountType(DiscountType.PERCENT.toString());
    }

    protected RuleBasedOrderAddProductActionModel createOrderAddProductAction(AbstractRuleActionRAO action, int quantity,
                                                                              Long productId, PromotionResultModel promoResult) {
        RuleBasedOrderAddProductActionModel actionModel = this.createPromotionAction(promoResult, action);
        actionModel.setProductId(productId);
        actionModel.setQuantity(quantity);
        return actionModel;
    }

    @Override
    public void undo(ItemModel item) {
        if (!(item instanceof RuleBasedOrderAddProductActionModel)) {
            return;
        }
        RuleBasedOrderAddProductActionModel action = (RuleBasedOrderAddProductActionModel) item;
        this.handleUndoActionMetadata(action);
        Long productId = action.getProductId();
        Integer quantity = action.getQuantity();
        AbstractOrderModel order = action.getPromotionResult().getOrder();
        AbstractOrderEntryModel undoEntry = this.findOrderEntryForUndo(order, action);
        if (undoEntry == null) {
            LOG.warn("cannot undo {}, cannot find order entry for undo(). Looking for order {} with product {} with quantity {}",
                    new Object[]{this.getClass().getSimpleName(), order.getCode(), productId, quantity});
            return;
        }

        if(cartService.isComboEntry(undoEntry) &&
                !ComboType.FIXED_COMBO.toString().equals(undoEntry.getComboType())
                && CollectionUtils.isNotEmpty(undoEntry.getSubOrderEntries())) {
            List<ProductInFreeGiftComboData> productInComboList = new ArrayList<>();
            undoEntry.getSubOrderEntries().stream().forEach(se -> {
                ProductInFreeGiftComboData data = new ProductInFreeGiftComboData();
                data.setId(se.getProductId());
                data.setComboGroupNumber(se.getComboGroupNumber());
                data.setPrice(se.getPrice());
                data.setQuantity(se.getQuantity());
                data.setOriginPrice(se.getOriginPrice());
                data.setFinalPrice(se.getFinalPrice());
                data.setDiscountValue(se.getDiscountValue());
                data.setRewardAmount(se.getRewardAmount());
                data.setTotalPrice(se.getTotalPrice());
                productInComboList.add(data);
            });

            comboWithProductMap.put(generateComboFreeGiftKey(order.getCode(), undoEntry.getProductId()), productInComboList);
        }

        Long newQuantity = undoEntry.getQuantity() != null ? undoEntry.getQuantity() - quantity : 0L - quantity;
        if (order instanceof CartModel) {
            this.getCartService().updateQuantities((CartModel) order, new SingletonMap(undoEntry.getEntryNumber(), newQuantity));
        } else if (order instanceof OrderModel) {
            OrderModel orderModel = (OrderModel) order;
            this.orderUtils.updateOrderQuantities(orderModel, new SingletonMap(undoEntry.getEntryNumber(), newQuantity));
            if (newQuantity < 1 && billService.shouldUpdateBillOf(orderModel)) {
                CommerceCartModification commerceCartModification = new CommerceCartModification();
                commerceCartModification.setOrder(order);
                commerceCartModification.setEntry(undoEntry);
                commerceCartModification.setProductId(undoEntry.getProductId());
                billService.deleteProductInReturnBillWithOrder(orderModel, commerceCartModification);
            }
        }

        if (newQuantity >= 1) {
            this.undoInternal(action);
            return;
        }

        this.normalizeEntryNumbers(order);
    }

    protected void normalizeEntryNumbers(AbstractOrderModel order) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        Collections.sort(entries, Comparator.comparing(AbstractOrderEntryModel::getEntryNumber));
        for (int i = 0; i < entries.size(); ++i) {
            entries.get(i).setEntryNumber(i);
            this.getModelService().save(entries.get(i));

        }
    }

    protected AbstractOrderEntryModel findOrderEntryForUndo(AbstractOrderModel order, RuleBasedOrderAddProductActionModel action) {
        AbstractOrderEntryModel giveAwayEntry = this.findMatchingGiveAwayEntry(order, action);
        if (giveAwayEntry != null) {
            return giveAwayEntry;
        }

        return null;
//        AbstractOrderEntryModel matchingEntryWithProductAndQuantity = this.getEntryWithMatchingProductAndQuantity(order, action);
//        if (matchingEntryWithProductAndQuantity != null) {
//            return matchingEntryWithProductAndQuantity;
//        } else {
//            Iterator var6 = order.getEntries().iterator();
//
//            AbstractOrderEntryModel entry;
//            do {
//                if (!var6.hasNext()) {
//                    return null;
//                }
//
//                entry = (AbstractOrderEntryModel) var6.next();
//            }
//            while (!action.getProductId().equals(entry.getProductId()) ||
//                    action.getQuantity().compareTo(entry.getQuantity().intValue()) >= 0);
//
//            return entry;
//        }
    }

    protected AbstractOrderEntryModel getEntryWithMatchingProductAndQuantity(AbstractOrderModel order, RuleBasedOrderAddProductActionModel action) {
        Iterator var4 = order.getEntries().iterator();

        AbstractOrderEntryModel entry;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            entry = (AbstractOrderEntryModel) var4.next();
        }
        while (!action.getProductId().equals(entry.getProductId()) ||
                !action.getQuantity().equals(entry.getQuantity().intValue()));

        return entry;
    }

    protected AbstractOrderEntryModel findMatchingGiveAwayEntry(AbstractOrderModel order, RuleBasedOrderAddProductActionModel action) {
        Iterator var4 = order.getEntries().iterator();

        AbstractOrderEntryModel entry;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            entry = (AbstractOrderEntryModel) var4.next();
        }
        while (!BooleanUtils.isTrue(entry.isGiveAway()) ||
                !action.getProductId().equals(entry.getProductId()) ||
                !action.getQuantity().equals(entry.getQuantity().intValue()));

        return entry;
    }

    protected CartService getCartService() {
        return this.cartService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setOrderUtils(OrderUtils orderUtils) {
        this.orderUtils = orderUtils;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    public void removeProductInComboOfOrder(String orderCode) {
        final String prefix = orderCode + CommonUtils.UNDERSCORE;
        comboWithProductMap.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public Map<String, List<ProductInFreeGiftComboData>> getComboWithProductMap() {
        return comboWithProductMap;
    }
}
