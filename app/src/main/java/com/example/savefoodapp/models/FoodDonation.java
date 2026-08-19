package com.example.savefoodapp.models;

public class FoodDonation {
    private int id;
    private int foodOrganizationId;
    private String foodName;
    private int quantity;
    private String description;
    private String expiryDate;
    private String status;


    public FoodDonation(int id, int foodOrganizationId, String foodName,
                        int quantity, String description, String expiryDate,
                        String status) {
        this.id = id;
        this.foodOrganizationId = foodOrganizationId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.description = description;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFoodOrganizationId() {
        return foodOrganizationId;
    }

    public void setFoodOrganizationId(int foodOrganizationId) {
        this.foodOrganizationId = foodOrganizationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }
}
