package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.CheckOutOfStockParam;
import com.vctek.orderservice.dto.CommerceAddComboParameter;
import com.vctek.orderservice.dto.ProductInFreeGiftComboData;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.BasicProductData;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.repository.DefaultOrderRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.redis.ProductData;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class DefaultAbstractOrderService<O extends AbstractOrderModel, E extends AbstractOrderEntryModel>
        implements AbstractOrderService<O, E> {
    protected ProductService productService;
    protected ModelService modelService;
    private DefaultOrderRepository defaultOrderRepository;
    private LogisticService logisticService;
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;
    private CompanyClient companyClient;
    private InventoryService inventoryService;

    protected void addEntryAtLast(O order, E entry) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        Collections.sort(entries, Comparator.comparing(AbstractOrderEntryModel::getEntryNumber));
        final int lastIndex = CollectionUtils.isEmpty(entries) ? 0 : entries.size() - 1;
        final int ret = CollectionUtils.isEmpty(entries) ? 0 : entries.get(lastIndex).getEntryNumber().intValue() + 1;
        entry.setEntryNumber(ret);
        entries.add(entry);
    }

    protected void addEntryAtFirst(O order, E entry) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        if (CollectionUtils.isEmpty(entries)) {
            this.addEntryAtLast(order, entry);
            return;
        }
        entry.setEntryNumber(-1);
        entries.add(0, entry);
    }

    @Override
    public void normalizeEntryNumbers(final AbstractOrderModel cartModel, boolean isImport) {
        final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        Collections.sort(entries, Comparator.comparing(AbstractOrderEntryModel::getEntryNumber));
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setEntryNumber(Integer.valueOf(i));
            if (!isImport) {
                modelService.save(entries.get(i));
            }
        }
    }

    protected void recalculateSubOrderEntryQuantity(E entry) {
        Set<SubOrderEntryModel> subOrderEntries = entry.getSubOrderEntries();
        if (CollectionUtils.isNotEmpty(subOrderEntries)) {
            int entryQty = entry.getQuantity() == null ? 0 : entry.getQuantity().intValue();
            subOrderEntries.forEach(soe -> soe.setQuantity(entryQty));
        }
    }

    protected void cloneEntryProperties(AbstractOrderEntryModel originEntry, E cloneEntry, O orderModel) {
        cloneEntry.setOrder(orderModel);
        cloneEntry.setDiscountValues(originEntry.getDiscountValues());
        cloneEntry.setDiscount(originEntry.getDiscount());
        cloneEntry.setDiscountType(originEntry.getDiscountType());
        cloneEntry.setEntryNumber(originEntry.getEntryNumber());
        cloneEntry.setFinalPrice(originEntry.getFinalPrice());
        cloneEntry.setFixedDiscount(originEntry.getFixedDiscount());
        cloneEntry.setProductId(originEntry.getProductId());
        cloneEntry.setWarehouseId(originEntry.getWarehouseId());
        cloneEntry.setBasePrice(originEntry.getBasePrice());
        cloneEntry.setOriginBasePrice(originEntry.getOriginBasePrice());
        cloneEntry.setTotalPrice(originEntry.getTotalPrice());
        cloneEntry.setQuantity(originEntry.getQuantity());
        cloneEntry.setOrderCode(orderModel.getCode());
        cloneEntry.setWeight(originEntry.getWeight());
        cloneEntry.setTotalDiscount(originEntry.getTotalDiscount());
        cloneEntry.setCalculated(originEntry.isCalculated());
        cloneEntry.setGiveAway(originEntry.isGiveAway());
        cloneEntry.setFixedPrice(originEntry.isFixedPrice());
        cloneEntry.setDiscountOrderToItem(originEntry.getDiscountOrderToItem());
        cloneEntry.setComboType(originEntry.getComboType());
        cloneEntry.setRecommendedRetailPrice(originEntry.getRecommendedRetailPrice());
        cloneEntry.setRewardAmount(originEntry.getRewardAmount());
        cloneEntry.setSaleOff(originEntry.isSaleOff());
        cloneEntry.setVat(originEntry.getVat());
        cloneEntry.setVatType(originEntry.getVatType());
    }

    protected void cloneSubOrderEntries(AbstractOrderEntryModel originEntry, AbstractOrderEntryModel cloneEntry) {
        Set<SubOrderEntryModel> subOrderEntries = originEntry.getSubOrderEntries();
        Set<SubOrderEntryModel> cloneSubEntries = new HashSet<>();
        if (CollectionUtils.isNotEmpty(subOrderEntries)) {
            cloneEntry.setComboType(originEntry.getComboType());
            for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
                SubOrderEntryModel cloneSubEntry = SerializationUtils.clone(subOrderEntryModel);
                cloneSubEntry.setId(null);
                cloneSubEntry.setOrderEntry(cloneEntry);
                cloneSubEntries.add(cloneSubEntry);
            }

            cloneEntry.setSubOrderEntries(cloneSubEntries);
        }
    }

    @Override
    public void addProductToComboInPromotion(AbstractOrderEntryModel abstractOrderEntryModel, List<ProductInFreeGiftComboData> productInFreeGiftComboDataList) {
        if (CollectionUtils.isEmpty(productInFreeGiftComboDataList)) {
            return;
        }
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        for (ProductInFreeGiftComboData productInFreeGiftComboData : productInFreeGiftComboDataList) {
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setQuantity(productInFreeGiftComboData.getQuantity());
            subOrderEntryModel.setOriginPrice(productInFreeGiftComboData.getOriginPrice());
            subOrderEntryModel.setPrice(productInFreeGiftComboData.getPrice());
            subOrderEntryModel.setProductId(productInFreeGiftComboData.getId());
            subOrderEntryModel.setDiscountValue(productInFreeGiftComboData.getDiscountValue());
            subOrderEntryModel.setFinalPrice(productInFreeGiftComboData.getFinalPrice());
            subOrderEntryModel.setRewardAmount(productInFreeGiftComboData.getRewardAmount());
            subOrderEntryModel.setComboGroupNumber(productInFreeGiftComboData.getComboGroupNumber());
            subOrderEntryModel.setOrderEntry(abstractOrderEntryModel);
            subOrderEntryModel.setTotalPrice(productInFreeGiftComboData.getTotalPrice());
            subOrderEntryModels.add(subOrderEntryModel);
        }

        abstractOrderEntryModel.setSubOrderEntries(subOrderEntryModels);
    }

    @Override
    public void doAddComboToCart(CommerceAddComboParameter commerceAddComboParameter) {
        AbstractOrderModel abstractOrderModel = commerceAddComboParameter.getAbstractOrderModel();
        Long productId = commerceAddComboParameter.getProductComboId();
        long quantityToAdd = commerceAddComboParameter.getQuantityToAdd();
        AbstractOrderEntryModel entryModel = commerceAddComboParameter.getEntryModel();

        ProductIsCombo productIsCombo = productService.checkIsCombo(productId, abstractOrderModel.getCompanyId(),
                (int) quantityToAdd);
        if (!productIsCombo.isCombo()) {
            return;
        }

        productService.checkAvailableToSale(productIsCombo, abstractOrderModel);
        updateComboPriceOfEntryWith(abstractOrderModel, entryModel, productIsCombo, productId);
        entryModel.setComboType(productIsCombo.getComboType());
        if (ComboType.FIXED_COMBO.toString().equals(productIsCombo.getComboType())) {
            addSubOrderEntriesToComboEntry(entryModel, productIsCombo.getComboProducts(), quantityToAdd);
            commerceUpdateCartEntryStrategy.validatePriceForCartEntry(entryModel, abstractOrderModel);
        }
    }

    @Override
    public void updateComboPriceOfEntryWith(AbstractOrderModel model, AbstractOrderEntryModel entryModel, ProductIsCombo productIsCombo, Long productId) {
        if (OrderType.ONLINE.name().equals(model.getType()) && PriceType.WHOLESALE_PRICE.name().equals(model.getPriceType())) {
            entryModel.setBasePrice(productIsCombo.getWholesalePrice());
            entryModel.setOriginBasePrice(productIsCombo.getWholesalePrice());
            return;
        }
        Double basePrice = productIsCombo.getPrice();
        if (OrderType.ONLINE.name().equals(model.getType()) && PriceType.DISTRIBUTOR_PRICE.name().equals(model.getPriceType())) {
            entryModel.setRecommendedRetailPrice(basePrice);
            entryModel.setOriginBasePrice(basePrice);
            Map<Long, DistributorSetingPriceData> priceDataMap = logisticService.getProductPriceSetting(model.getDistributorId(),
                    model.getCompanyId(), Arrays.asList(productId));
            if (priceDataMap.containsKey(productId)) {
                DistributorSetingPriceData setingPriceData = priceDataMap.get(productId);
                if (setingPriceData.getRecommendedRetailPrice() != null) {
                    entryModel.setRecommendedRetailPrice(setingPriceData.getRecommendedRetailPrice());
                }
                basePrice = logisticService.calculateDistributorSettingPrice(setingPriceData, entryModel.getRecommendedRetailPrice());
            }

            entryModel.setBasePrice(basePrice);
            return;
        }
        entryModel.setBasePrice(basePrice);
        entryModel.setOriginBasePrice(basePrice);
    }

    @Override
    public void addSubOrderEntriesToComboEntry(AbstractOrderEntryModel entryModel, List<BasicProductData> subEntries, long quantityToAdd) {
        AbstractOrderModel order = entryModel.getOrder();
        Boolean sellLessZero = companyClient.checkSellLessZero(order.getCompanyId());
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        for (BasicProductData comboProduct : subEntries) {
            if (!sellLessZero || SellSignal.ECOMMERCE_WEB.toString().equals(order.getSellSignal())) {
                CheckOutOfStockParam param = new CheckOutOfStockParam();
                param.setCompanyId(order.getCompanyId());
                param.setProductId(comboProduct.getId());
                param.setQuantity(quantityToAdd);
                param.setWarehouseId(order.getWarehouseId());
                param.setAbstractOrderModel(order);
                inventoryService.validateOutOfStock(param);
            }
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setProductId(comboProduct.getId());
            subOrderEntryModel.setOriginPrice(comboProduct.getPrice());
            subOrderEntryModel.setOrderEntry(entryModel);
            subOrderEntryModel.setQuantity(Math.toIntExact(quantityToAdd));
            subOrderEntryModels.add(subOrderEntryModel);
        }
        entryModel.setSubOrderEntries(subOrderEntryModels);
    }

    @Override
    public boolean isComboEntry(AbstractOrderEntryModel abstractOrderEntry) {
        return StringUtils.isNotBlank(abstractOrderEntry.getComboType()) ||
                CollectionUtils.isNotEmpty(abstractOrderEntry.getSubOrderEntries());
    }

    @Override
    public void addSubOrderEntries(AbstractOrderEntryModel abstractOrderEntry, List<BasicProductData> comboProducts, int qty) {
        Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
        for (BasicProductData comboProduct : comboProducts) {
            SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
            subOrderEntryModel.setProductId(comboProduct.getId());
            subOrderEntryModel.setOriginPrice(comboProduct.getPrice());
            subOrderEntryModel.setOrderEntry(abstractOrderEntry);
            subOrderEntryModel.setQuantity(qty);
            subOrderEntryModels.add(subOrderEntryModel);
        }
        abstractOrderEntry.setSubOrderEntries(subOrderEntryModels);
    }

    protected void clearCouponIfNeed(AbstractOrderModel abstractOrderModel) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = abstractOrderModel.getOrderHasCouponCodeModels();
        if (CollectionUtils.isNotEmpty(orderHasCouponCodeModels)) {
            for (OrderHasCouponCodeModel orderHasCouponCodeModel : orderHasCouponCodeModels) {
                orderHasCouponCodeModel.setOrder(null);
                orderHasCouponCodeModel.setCouponCode(null);
            }

            abstractOrderModel.getOrderHasCouponCodeModels().removeAll(orderHasCouponCodeModels);
        }
    }

    protected void clearCouldFiredPromotions(AbstractOrderModel abstractOrderModel) {
        Set<PromotionSourceRuleModel> couldFirePromotions = abstractOrderModel.getCouldFirePromotions();
        if (CollectionUtils.isNotEmpty(couldFirePromotions)) {
            abstractOrderModel.getCouldFirePromotions().removeAll(couldFirePromotions);
        }
    }

    @Override
    public boolean isValidEntryForPromotion(AbstractOrderEntryModel entryModel) {
        if (!this.isComboEntry(entryModel) && !entryModel.isSaleOff()) {
            return true;
        }

        if (entryModel.isSaleOff()) {
            return false;
        }

        ProductData comboData = productService.getBasicProductDetail(entryModel.getProductId());
        if (comboData != null && comboData.isAllowAppliedPromotion()) {
            return true;
        }
        return false;
    }

    @Override
    public AbstractOrderModel findByOrderCodeAndCompanyId(String orderCode, Long companyId) {
        return defaultOrderRepository.findByOrderCodeAndCompanyId(orderCode, companyId);
    }

    @Autowired
    public void setDefaultOrderRepository(DefaultOrderRepository defaultOrderRepository) {
        this.defaultOrderRepository = defaultOrderRepository;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setCommerceUpdateCartEntryStrategy(CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy) {
        this.commerceUpdateCartEntryStrategy = commerceUpdateCartEntryStrategy;
    }

    @Autowired
    public void setCompanyClient(CompanyClient companyClient) {
        this.companyClient = companyClient;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
}
