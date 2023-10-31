package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductImageData implements Serializable {
    private static final long serialVersionUID = -8298347828392583991L;
    private Long id;
    private String image;
    private String originName;
    private boolean isDefault;
    private boolean showOnWebsite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isShowOnWebsite() {
        return showOnWebsite;
    }

    public void setShowOnWebsite(boolean showOnWebsite) {
        this.showOnWebsite = showOnWebsite;
    }
}
