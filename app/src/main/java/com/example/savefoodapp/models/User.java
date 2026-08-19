package com.example.savefoodapp.models;

public class User {
    private int id;
    private String email;
    private String password;
    private String role;
    private int organizationId;

    public User(int id, String email, String password, String role, int organizationId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.organizationId = organizationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}
