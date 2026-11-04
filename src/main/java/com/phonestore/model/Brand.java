package com.phonestore.model;

public class Brand {

    private int brandId;
    private String brandName;
    private String originCountry;
    private String description;
    private boolean active;

    public Brand() {
    }

    public Brand(int brandId, String brandName, String originCountry, String description, boolean active) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.originCountry = originCountry;
        this.description = description;
        this.active = active;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return brandName;
    }
}
