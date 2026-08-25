package com.example.savefoodapp.data.models;

public class DonationRequest {

    private int id;
    private int donationId;
    private int charityOrganizationId;
    private int quantityRequested;
    private String status;

    public DonationRequest(
            int id,
            int donationId,
            int charityOrganizationId,
            int quantityRequested,
            String status
    ) {
        this.id = id;
        this.donationId = donationId;
        this.charityOrganizationId = charityOrganizationId;
        this.quantityRequested = quantityRequested;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDonationId() {
        return donationId;
    }

    public void setDonationId(int donationId) {
        this.donationId = donationId;
    }

    public int getCharityOrganizationId() {
        return charityOrganizationId;
    }

    public void setCharityOrganizationId(int charityOrganizationId) {
        this.charityOrganizationId = charityOrganizationId;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(int quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}