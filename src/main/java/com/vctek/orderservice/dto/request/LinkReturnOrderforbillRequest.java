package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.migration.dto.OrderBillLinkDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkReturnOrderforbillRequest {
    private Long exportExternalId;
    private Long companyId;
    private Long returnOrderId;
    private OrderBillLinkDTO orderBillLinkDTO;

    public Long getExportExternalId() {
        return exportExternalId;
    }

    public void setExportExternalId(Long exportExternalId) {
        this.exportExternalId = exportExternalId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public OrderBillLinkDTO getOrderBillLinkDTO() {
        return orderBillLinkDTO;
    }

    public void setOrderBillLinkDTO(OrderBillLinkDTO orderBillLinkDTO) {
        this.orderBillLinkDTO = orderBillLinkDTO;
    }
}
