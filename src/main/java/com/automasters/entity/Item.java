package com.automasters.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "items", uniqueConstraints = {
        @UniqueConstraint(columnNames = "item_name_lower")
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_name_lower", nullable = false, unique = true)
    private String itemNameLower;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    public Item() {
        this.createdDate = LocalDateTime.now();
    }

    public Item(String itemName, String description) {
        this.itemName = itemName;
        this.itemNameLower = itemName.toLowerCase().trim();
        this.description = description;
        this.createdDate = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    private void updateItemNameLower() {
        if (itemName != null) {
            this.itemNameLower = itemName.toLowerCase().trim();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
        this.itemNameLower = itemName.toLowerCase().trim();
    }

    public String getItemNameLower() {
        return itemNameLower;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
