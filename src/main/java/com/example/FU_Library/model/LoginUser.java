package com.example.demo.model;

import com.example.demo.entity.User;
import com.example.demo.entity.Manager;

public class LoginUser {
    private Long id;
    private String name;
    private String email;
    private String role; // "USER" hoặc "MANAGER"

    public LoginUser() {
    }

    public LoginUser(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = "USER";
    }

    public LoginUser(Manager manager) {
        this.id = manager.getId();
        this.name = manager.getName();
        this.email = manager.getEmail();
        this.role = "MANAGER";
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isManager() {
        return "MANAGER".equals(role);
    }

    public boolean isUser() {
        return "USER".equals(role);
    }
}
