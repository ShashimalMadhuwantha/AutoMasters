package com.automasters.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // "STOCK_IN" or "STOCK_OUT"

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "buy_price")
    private Double buyPrice; // Nullable for STOCK_OUT

    @Column(name = "sell_price")
    private Double sellPrice; // Nullable for STOCK_OUT

    @Column(name = "batch_reference", length = 255)
    private String batchReference;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(length = 500)
    private String notes;

    public StockTransaction() {
        this.transactionDate = LocalDateTime.now();
    }

    public StockTransaction(Item item, String transactionType, Integer quantity) {
        this.item = item;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = LocalDateTime.now();
    }

    public StockTransaction(Item item, String transactionType, Integer quantity,
            Double buyPrice, Double sellPrice, String batchReference) {
        this(item, transactionType, quantity);
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.batchReference = batchReference;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
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

    public String getBatchReference() {
        return batchReference;
    }

    public void setBatchReference(String batchReference) {
        this.batchReference = batchReference;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
