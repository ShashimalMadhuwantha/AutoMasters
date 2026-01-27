package com.automasters.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_batches")
public class StockBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "buy_price", nullable = false)
    private Double buyPrice;

    @Column(name = "sell_price", nullable = false)
    private Double sellPrice;

    @Column(name = "batch_date", nullable = false)
    private LocalDateTime batchDate;

    @Column(name = "batch_reference")
    private String batchReference;

    public StockBatch() {
        this.batchDate = LocalDateTime.now();
    }

    public StockBatch(Item item, Integer quantity, Double buyPrice, Double sellPrice, String batchReference) {
        this.item = item;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.batchReference = batchReference;
        this.batchDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public LocalDateTime getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDateTime batchDate) {
        this.batchDate = batchDate;
    }

    public String getBatchReference() {
        return batchReference;
    }

    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }
}
