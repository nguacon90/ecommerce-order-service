package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.repository.CartRepository;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.GenerateCartCodeService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.validate.Validator;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl extends DefaultAbstractOrderService<CartModel, CartEntryModel> implements CartService {
    private CartRepository cartRepository;
    private Validator<CartInfoParameter> createCartValidator;
    private GenerateCartCodeService generateCartCodeService;
    private CartEntryRepository cartEntryRepository;

    public CartServiceImpl(CartRepository cartRepository, GenerateCartCodeService generateCartCodeService,
                           CartEntryRepository cartEntryRepository) {
        this.cartRepository = cartRepository;
        this.generateCartCodeService = generateCartCodeService;
        this.cartEntryRepository = cartEntryRepository;
    }

    @Override
    public CartEntryModel addNewEntry(CartModel order, Long productId, long qty, boolean isImport) {
        CartEntryModel entryModel = getInstanceCartEntryModel(order, productId, qty);
        boolean isFnB = productService.isFnB(productId);
        if(isFnB) {
            addEntryAtLast(order, entryModel);
        } else {
            addEntryAtFirst(order, entryModel);
            normalizeEntryNumbers(order, isImport);
        }
        order.setCalculated(Boolean.FALSE);
        recalculateSubOrderEntryQuantity(entryModel);
        return entryModel;
    }

    @Override
    public boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO) {
        Optional<CartEntryModel> entryModelOptional = cartEntryRepository.findById(orderEntryDTO.getEntryId());
        if(entryModelOptional.isPresent()) {
            return entryModelOptional.get().isSaleOff();
        }

        return false;
    }

    private CartEntryModel getInstanceCartEntryModel(CartModel order, Long productId, long qty) {
        CartEntryModel entryModel = new CartEntryModel();
        entryModel.setOrder(order);
        entryModel.setProductId(productId);
        entryModel.setQuantity(qty);
        entryModel.setOrderCode(order.getCode());
        return entryModel;
    }

    @Override
    public List<CartModel> findAllOrCreateNewByCreatedByUser(CartInfoParameter cartInfoParameter) {
        List<CartModel> carts = cartRepository.findAllByCreateByUserAndTypeAndExchangeAndCompanyIdAndSellSignal(cartInfoParameter.getUserId(),
                cartInfoParameter.getOrderType(), false, cartInfoParameter.getCompanyId(), cartInfoParameter.getSellSignal());
        carts = carts == null ? new ArrayList<>() : carts;
        if (CollectionUtils.isEmpty(carts)) {
            createCartValidator.validate(cartInfoParameter);
            CartModel cart = getOrCreateNewCart(cartInfoParameter);
            carts.add(cart);
        }

        return carts;
    }

    @Override
    public CartModel findByIdAndCompanyIdAndTypeAndCreateByUser(CartInfoParameter cartInfoParameter) {
        return cartRepository.findByCodeAndCompanyIdAndTypeAndCreateByUser(cartInfoParameter.getCode(),
                cartInfoParameter.getCompanyId(), cartInfoParameter.getOrderType(), cartInfoParameter.getUserId());
    }

    @Override
    public CartModel getOrCreateNewCart(CartInfoParameter cartInfoParameter) {

        CartModel cart = new CartModel();
        cart.setCreateByUser(cartInfoParameter.getUserId());
        cart.setCompanyId(cartInfoParameter.getCompanyId());
        cart.setWarehouseId(cartInfoParameter.getWarehouseId());
        cart.setType(cartInfoParameter.getOrderType());
        cart.setCustomerId(cartInfoParameter.getCustomerId());
        cart.setExchange(cartInfoParameter.isExchangeCart());
        cart.setCardNumber(cartInfoParameter.getCardNumber());
        cart.setSellSignal(cartInfoParameter.getSellSignal());
        cart.setExternalId(cartInfoParameter.getExternalId());
        cart.setExternalCode(cartInfoParameter.getExternalCode());
        cart.setGuid(cartInfoParameter.getGuid());
        cart.setOrderSourceModel(cartInfoParameter.getOrderSourceModel());
        if(StringUtils.isNotBlank(cartInfoParameter.getPriceType())) {
            cart.setPriceType(cartInfoParameter.getPriceType());
        } else {
            cart.setPriceType(PriceType.RETAIL_PRICE.toString());
        }
        cartRepository.save(cart);
        cart.setCode(generateCartCodeService.generateCartCode(cart));
        return cartRepository.save(cart);
    }


    @Override
    public CartModel findByCodeAndUserIdAndCompanyId(String code, Long userId, Long companyId) {
        return cartRepository.findByCodeAndCreateByUserAndCompanyId(code, userId, companyId);
    }

    @Override
    public void delete(CartModel cart) {
        this.clearCouponIfNeed(cart);
        this.clearCouldFiredPromotions(cart);
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public void refresh(CartModel cart) {
        modelService.refresh(cart);
    }

    @Override
    public CartModel save(CartModel cart) {
        return modelService.save(cart);
    }

    @Override
    public CartModel findByCodeAndCompanyId(String code, Long companyId) {
        return cartRepository.findByCodeAndCompanyId(code, companyId);
    }

    @Override
    public CartEntryModel findEntryBy(CartModel cartModel, Integer entryNumber) {
        return cartEntryRepository.findByOrderAndEntryNumber(cartModel, entryNumber);
    }

    @Override
    public CartEntryModel findEntryBy(Long entryId, CartModel cartModel) {
        return cartEntryRepository.findByIdAndOrder(entryId, cartModel);
    }

    @Override
    public CartEntryModel saveEntry(CartEntryModel entryModel) {
        return cartEntryRepository.save(entryModel);
    }

    @Override
    public void updateQuantities(CartModel cart, Map<Integer, Long> quantities) {
        if (cart == null) {
            throw new IllegalArgumentException("cart cannot be null");
        }

        if (!MapUtils.isEmpty(quantities)) {
            for (Map.Entry<CartEntryModel, Long> e : getEntryQuantityMap(cart, quantities).entrySet()) {
                CartEntryModel cartEntry = e.getKey();
                Long quantity = e.getValue();
                if (quantity == null || quantity.longValue() < 1) {
                    cart.getEntries().remove(cartEntry);
                } else {
                    cartEntry.setQuantity(quantity);
                }
            }
            cartRepository.save(cart);
        }
    }

    @Override
    public List<CartEntryModel> findAllEntriesBy(CartModel source) {
        return cartEntryRepository.findAllByOrder(source);
    }

    @Override
    public CartModel getCartByGuid(CartInfoParameter parameter) {
        List<CartModel> carts = cartRepository.findAllByCompanyIdAndGuid(parameter.getCompanyId(), parameter.getGuid());
        if(CollectionUtils.isNotEmpty(carts)) {
            return carts.get(0);
        }
        return null;
    }

    @Override
    public CartModel findByUserIdAndCompanyIdAndSellSignal(CartInfoParameter parameter) {
        List<CartModel> carts = cartRepository.findAllByCompanyIdAndCreateByUserAndSellSignalAndDeleted(parameter.getCompanyId(),
                parameter.getUserId(), parameter.getSellSignal(), false);
        if(CollectionUtils.isNotEmpty(carts)) {
            return carts.get(0);
        }
        return null;
    }

    @Override
    public CartEntryModel cloneEntry(AbstractOrderEntryModel originEntry, CartModel cartModel) {
        CartEntryModel cloneEntry = new CartEntryModel();
        super.cloneEntryProperties(originEntry, cloneEntry, cartModel);
        return cloneEntry;
    }

    @Override
    public void cloneSubOrderEntries(AbstractOrderEntryModel entry, CartEntryModel cloneEntry) {
        super.cloneSubOrderEntries(entry, cloneEntry);
    }

    @Override
    public CartModel findById(Long id) {
        Optional<CartModel> option = cartRepository.findById(id);
        return option.isPresent() ? option.get() : null;
    }

    private Map<CartEntryModel, Long> getEntryQuantityMap(final CartModel cart, final Map<Integer, Long> quantities) {
        final List<CartEntryModel> entries = (List) cart.getEntries();

        final Map<CartEntryModel, Long> ret = new LinkedHashMap<CartEntryModel, Long>();

        for (final Map.Entry<Integer, Long> q : quantities.entrySet()) {
            final Integer entryNumber = q.getKey();
            final Long quantity = q.getValue();
            ret.put(getEntry(entries, entryNumber), quantity);
        }

        return ret;
    }

    private CartEntryModel getEntry(final List<CartEntryModel> entries, final Integer entryNumber) {
        for (final CartEntryModel e : entries) {
            if (entryNumber.equals(e.getEntryNumber())) {
                return e;
            }
        }
        throw new IllegalArgumentException("no cart entry found with entry number " + entryNumber + " (got " + entries + ")");
    }

    @Autowired
    public void setCreateCartValidator(Validator<CartInfoParameter> createCartValidator) {
        this.createCartValidator = createCartValidator;
    }
}
