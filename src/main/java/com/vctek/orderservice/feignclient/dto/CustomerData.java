package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.dto.redis.AddressData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerData implements Serializable {
    private static final long serialVersionUID = -3132481900887111052L;
    private Long id;
    private String name;
    private Long companyId;
    private String phone;
    private String email;
    private String gender;
    private Date dateOfBirth;
    private Integer totalPurchaseTimes;
    private Float totalPurchaseAmount;
    private Date firstPurchaseDate;
    private Date latestPurchaseDate;
    private Long newAddressId;
    private String age;
    private List<AddressData> address;
    private boolean limitedApplyPromotionAndReward;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getTotalPurchaseTimes() {
        return totalPurchaseTimes;
    }

    public void setTotalPurchaseTimes(Integer totalPurchaseTimes) {
        this.totalPurchaseTimes = totalPurchaseTimes;
    }

    public Float getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(Float totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    public Date getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public void setFirstPurchaseDate(Date firstPurchaseDate) {
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public Date getLatestPurchaseDate() {
        return latestPurchaseDate;
    }

    public void setLatestPurchaseDate(Date latestPurchaseDate) {
        this.latestPurchaseDate = latestPurchaseDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AddressData> getAddress() {
        return address;
    }

    public void setAddress(List<AddressData> address) {
        this.address = address;
    }

    public Long getNewAddressId() {
        return newAddressId;
    }

    public void setNewAddressId(Long newAddressId) {
        this.newAddressId = newAddressId;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public boolean isLimitedApplyPromotionAndReward() {
        return limitedApplyPromotionAndReward;
    }

    public void setLimitedApplyPromotionAndReward(boolean limitedApplyPromotionAndReward) {
        this.limitedApplyPromotionAndReward = limitedApplyPromotionAndReward;
    }
}
