package com.automasters.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Column(name = "vehicle_number", nullable = false)
    private String vehicleNumber;

    @Column(name = "current_mileage")
    private Integer currentMileage;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {
        this.invoiceDate = LocalDateTime.now();
    }

    public Invoice(String invoiceNumber, String customerName, String contactNumber, String vehicleNumber,
            Integer currentMileage) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = LocalDateTime.now();
        this.customerName = customerName;
        this.contactNumber = contactNumber;
        this.vehicleNumber = vehicleNumber;
        this.currentMileage = currentMileage;
        this.totalAmount = 0.0;
    }

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        calculateTotal();
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        calculateTotal();
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(InvoiceItem::getPrice)
                .sum();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Integer getCurrentMileage() {
        return currentMileage;
    }

    public void setCurrentMileage(Integer currentMileage) {
        this.currentMileage = currentMileage;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }
}
