package com.example.savefoodapp.models;

public class User {

    private int id;
    private String name;
    private String email;
    private String password;
    private String passwordSalt;
    private String role;
    private int organizationId;
    private double latitude;
    private double longitude;

    public User(
            int id,
            String name,
            String email,
            String password,
            String passwordSalt,
            String role,
            int organizationId
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.organizationId = organizationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getOrganizationId() {
        return organizationId;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}