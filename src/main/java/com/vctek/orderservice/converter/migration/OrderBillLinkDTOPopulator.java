package com.vctek.orderservice.converter.migration;

import com.vctek.converter.Populator;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.migration.dto.OrderBillLinkDetailDTO;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.model.ToppingItemModel;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.SubOrderEntryService;
import com.vctek.orderservice.service.ToppingItemService;
import com.vctek.orderservice.service.ToppingOptionService;
import com.vctek.orderservice.service.impl.DefaultCalculationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OrderBillLinkDTOPopulator implements Populator<OrderModel, OrderBillLinkDTO> {
    private EntryRepository entryRepository;
    private SubOrderEntryService subOrderEntryService;
    private DefaultCalculationService defaultCalculationService;
    private ToppingItemService toppingItemService;

    @Override
    public void populate(OrderModel source, OrderBillLinkDTO target) {
        target.setCompanyId(source.getCompanyId());
        target.setOrderCode(source.getCode());
        target.setOrderType(source.getType());
        target.setWarehouseId(source.getWarehouseId());
        target.setExchange(source.isExchange());
        target.setEntries(populateEntries(source));
    }

    private List<OrderBillLinkDetailDTO> populateEntries(OrderModel source) {
        List<OrderBillLinkDetailDTO> details = new ArrayList<>();
        List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(source);
        OrderBillLinkDetailDTO entry;
        for (AbstractOrderEntryModel entryModel : entries) {
            List<SubOrderEntryModel> subOrderEntries = subOrderEntryService.findAllBy(entryModel);
            if (CollectionUtils.isNotEmpty(subOrderEntries) || StringUtils.isNotBlank(entryModel.getComboType())) {
                List<OrderBillLinkDetailDTO> comboEntries = populateComboEntries(entryModel, subOrderEntries);
                details.addAll(comboEntries);
                continue;
            }

            Set<ToppingItemModel> toppingItemModels = toppingItemService.findAllByEntryId(entryModel.getId());
            if (CollectionUtils.isNotEmpty(toppingItemModels)) {
                List<OrderBillLinkDetailDTO> toppingItemEntries = populateToppingItemEntries(entryModel, toppingItemModels);
                details.addAll(toppingItemEntries);
            }
            entry = new OrderBillLinkDetailDTO();
            entry.setOrderEntryId(entryModel.getId());
            entry.setProductId(entryModel.getProductId());
            entry.setDiscountValue(defaultCalculationService.calculateFinalDiscountOfEntry(entryModel));
            details.add(entry);
        }
        return details;
    }

    private List<OrderBillLinkDetailDTO> populateToppingItemEntries(AbstractOrderEntryModel entryModel, Set<ToppingItemModel> toppingItemModels) {
        List<OrderBillLinkDetailDTO> toppingItemEntries = new ArrayList<>();
        OrderBillLinkDetailDTO subEntry;
        for (ToppingItemModel toppingItemModel : toppingItemModels) {
            subEntry = new OrderBillLinkDetailDTO();
            subEntry.setOrderEntryId(entryModel.getId());
            subEntry.setProductId(toppingItemModel.getProductId());
            subEntry.setToppingOptionId(toppingItemModel.getToppingOptionModel().getId());
            subEntry.setQuantity(Long.valueOf(toppingItemModel.getQuantity()));
            toppingItemEntries.add(subEntry);
        }
        return toppingItemEntries;
    }

    private List<OrderBillLinkDetailDTO> populateComboEntries(AbstractOrderEntryModel entryModel, List<SubOrderEntryModel> subOrderEntries) {
        List<OrderBillLinkDetailDTO> comboEntries = new ArrayList<>();
        OrderBillLinkDetailDTO subEntry;
        for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
            subEntry = new OrderBillLinkDetailDTO();
            subEntry.setComboId(entryModel.getProductId());
            subEntry.setOrderEntryId(entryModel.getId());
            subEntry.setProductId(subOrderEntryModel.getProductId());
            subEntry.setSubOrderEntryId(subOrderEntryModel.getId());
            subEntry.setPrice(subOrderEntryModel.getPrice());
            subEntry.setDiscountValue(subOrderEntryModel.getDiscountValue());
            comboEntries.add(subEntry);
        }
        return comboEntries;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setSubOrderEntryService(SubOrderEntryService subOrderEntryService) {
        this.subOrderEntryService = subOrderEntryService;
    }

    @Autowired
    public void setDefaultCalculationService(DefaultCalculationService defaultCalculationService) {
        this.defaultCalculationService = defaultCalculationService;
    }

    @Autowired
    public void setToppingItemService(ToppingItemService toppingItemService) {
        this.toppingItemService = toppingItemService;
    }
}
