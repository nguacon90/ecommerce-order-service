package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.CheckPermissionData;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component("commerceCartParameterPopulator")
public class CommerceCartParameterPopulator extends AbstractCommerceCartParameterPopulator
        implements Populator<OrderEntryDTO, CommerceAbstractOrderParameter> {
    private CartService cartService;
    private AuthService authService;
    private CheckPermissionClient checkPermissionClient;
    private LogisticService logisticService;

    public CommerceCartParameterPopulator(CartService cartService, AuthService authService,
                                          CheckPermissionClient checkPermissionClient) {
        this.cartService = cartService;
        this.authService = authService;
        this.checkPermissionClient = checkPermissionClient;
    }

    @Override
    public void populate(OrderEntryDTO orderEntryDTO, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        Long userId = authService.getCurrentUserId();

        checkAvailableToSellOf(orderEntryDTO.getProductId());

        populateAbstractOrderModel(userId, orderEntryDTO, commerceAbtractOrderParameter);
        populateBasePrice(userId, orderEntryDTO, commerceAbtractOrderParameter);

        commerceAbtractOrderParameter.setProductId(orderEntryDTO.getProductId());
        commerceAbtractOrderParameter.setQuantity(orderEntryDTO.getQuantity());
        commerceAbtractOrderParameter.setDiscount(orderEntryDTO.getDiscount());
        commerceAbtractOrderParameter.setDiscountType(orderEntryDTO.getDiscountType());
        commerceAbtractOrderParameter.setWeight(orderEntryDTO.getWeight());
    }

    protected void populateBasePrice(Long userId, OrderEntryDTO orderEntryDTO, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        CheckPermissionRequest request = new CheckPermissionRequest();
        request.setCompanyId(orderEntryDTO.getCompanyId());
        request.setUserId(userId);
        String code = getPermissionCodeBy(orderEntryDTO.getOrderType());
        request.setCode(code);
        CheckPermissionData permission = checkPermissionClient.checkPermission(request);
        if (Boolean.TRUE.equals(permission.getPermission())) {
            AbstractOrderModel model = commerceAbtractOrderParameter.getOrder();
            if (!PriceType.DISTRIBUTOR_PRICE.toString().equals(model.getPriceType())
                    && (orderEntryDTO.getPrice() == null || orderEntryDTO.getPrice() < 0)) {
                ErrorCodes err = ErrorCodes.INVALID_PRODUCT_PRICE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if (populateDistributorPrice(orderEntryDTO, commerceAbtractOrderParameter)) {
                return;
            }
            commerceAbtractOrderParameter.setOriginBasePrice(orderEntryDTO.getPrice());
            commerceAbtractOrderParameter.setBasePrice(orderEntryDTO.getPrice());
        } else {
            populateBasePriceFromProduct(orderEntryDTO, commerceAbtractOrderParameter);
        }
    }

    protected void populateBasePriceFromProduct(OrderEntryDTO orderEntryDTO, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        AbstractOrderModel model = commerceAbtractOrderParameter.getOrder();
        Long productId = orderEntryDTO.getProductId();
        double price;

        if (OrderType.ONLINE.name().equals(model.getType()) && PriceType.WHOLESALE_PRICE.name().equals(model.getPriceType())) {
            ProductSearchRequest searchRequest = new ProductSearchRequest();
            searchRequest.setIds(productId.toString());
            searchRequest.setCompanyId(model.getCompanyId());
            searchRequest.setPageSize(1);
            List<ProductSearchData> productSearchData = productService.search(searchRequest);
            if (CollectionUtils.isEmpty(productSearchData) || productSearchData.get(0).getWholesalePrice() == null) {
                ErrorCodes err = ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            price = productSearchData.get(0).getWholesalePrice();
            commerceAbtractOrderParameter.setBasePrice(price);
            commerceAbtractOrderParameter.setOriginBasePrice(price);
            return;
        }

        Integer quantity = orderEntryDTO.getQuantity() != null ? orderEntryDTO.getQuantity().intValue() : null;
        if (populateDistributorPrice(orderEntryDTO, commerceAbtractOrderParameter)) return;

        price = getPriceOf(productId, quantity);
        commerceAbtractOrderParameter.setBasePrice(price);
        commerceAbtractOrderParameter.setOriginBasePrice(price);
    }

    protected boolean populateDistributorPrice(OrderEntryDTO orderEntryDTO, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        AbstractOrderModel model = commerceAbtractOrderParameter.getOrder();
        Long productId = orderEntryDTO.getProductId();
        if (OrderType.ONLINE.name().equals(model.getType()) && PriceType.DISTRIBUTOR_PRICE.name().equals(model.getPriceType())) {
            if (model.getDistributorId() == null) {
                ErrorCodes err = ErrorCodes.INVALID_DISTRIBUTOR_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            Double price = getPriceOf(productId, (int) CommonUtils.readValue(orderEntryDTO.getQuantity()));
            commerceAbtractOrderParameter.setRecommendedRetailPrice(price);
            Map<Long, DistributorSetingPriceData> priceDataMap = logisticService.getProductPriceSetting(
                    model.getDistributorId(), model.getCompanyId(), Arrays.asList(productId));
            if (priceDataMap.containsKey(productId)) {
                DistributorSetingPriceData priceData = priceDataMap.get(productId);
                Double basePrice = getBasePriceDistributorType(priceData, commerceAbtractOrderParameter);
                commerceAbtractOrderParameter.setBasePrice(basePrice);
                commerceAbtractOrderParameter.setOriginBasePrice(price);
                return true;
            }
            commerceAbtractOrderParameter.setOriginBasePrice(price);
            commerceAbtractOrderParameter.setBasePrice(price);
            return true;
        }
        return false;
    }

    private Double getBasePriceDistributorType(DistributorSetingPriceData priceData, CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        if (priceData.getRecommendedRetailPrice() != null) {
            commerceAbtractOrderParameter.setRecommendedRetailPrice(priceData.getRecommendedRetailPrice());
        }
        return logisticService.calculateDistributorSettingPrice(priceData, commerceAbtractOrderParameter.getRecommendedRetailPrice());
    }

    protected String getPermissionCodeBy(String orderType) {
        if (OrderType.ONLINE.toString().equals(orderType)) {
            return PermissionCodes.EDIT_PRICE_ON_ORDER.code();
        }

        if (OrderType.RETAIL.toString().equals(orderType)) {
            return PermissionCodes.EDIT_PRICE_ON_RETAIL.code();
        }

        return PermissionCodes.EDIT_PRICE_ON_WHOLESALE.code();
    }

    protected void populateAbstractOrderModel(Long userId, OrderEntryDTO orderEntryDTO,
                                              CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        String orderCode = orderEntryDTO.getOrderCode();
        Long companyId = orderEntryDTO.getCompanyId();

        CartInfoParameter cartInfoParameter = new CartInfoParameter();
        cartInfoParameter.setCompanyId(companyId);
        cartInfoParameter.setUserId(userId);
        cartInfoParameter.setCode(orderCode);
        cartInfoParameter.setOrderType(orderEntryDTO.getOrderType());
        CartModel cart = cartService.findByIdAndCompanyIdAndTypeAndCreateByUser(cartInfoParameter);
        if (cart == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        commerceAbtractOrderParameter.setCompanyId(companyId);
        commerceAbtractOrderParameter.setOrder(cart);
        commerceAbtractOrderParameter.setWarehouseId(cart.getWarehouseId());
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }
}
