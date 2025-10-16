package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "memberships")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType type;

    @Column(nullable = false)
    private Integer maxBooksPerMonth;

    @Column(nullable = false)
    private Double price;

    @Column
    private String description;

    public enum MembershipType {
        ORDINARY, ADVANCE, PREMIUM
    }

    // Constructors
    public Membership() {
    }

    public Membership(MembershipType type, Integer maxBooksPerMonth, Double price, String description) {
        this.type = type;
        this.maxBooksPerMonth = maxBooksPerMonth;
        this.price = price;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MembershipType getType() {
        return type;
    }

    public void setType(MembershipType type) {
        this.type = type;
    }

    public Integer getMaxBooksPerMonth() {
        return maxBooksPerMonth;
    }

    public void setMaxBooksPerMonth(Integer maxBooksPerMonth) {
        this.maxBooksPerMonth = maxBooksPerMonth;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
