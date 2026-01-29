package com.automasters.dao;

import com.automasters.entity.Invoice;
import com.automasters.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDAO {

    public void save(Invoice invoice) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(invoice);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Invoice> findByVehicleNumber(String vehicleNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Invoice i WHERE LOWER(i.vehicleNumber) LIKE LOWER(:vehicleNumber) ORDER BY i.invoiceDate DESC",
                    Invoice.class)
                    .setParameter("vehicleNumber", "%" + vehicleNumber + "%")
                    .list();
        }
    }

    public List<Invoice> findByDateRange(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Invoice i WHERE i.invoiceDate >= :startOfDay AND i.invoiceDate < :endOfDay ORDER BY i.invoiceDate DESC",
                    Invoice.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .list();
        }
    }

    public List<Invoice> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Invoice i WHERE i.invoiceDate >= :startDateTime AND i.invoiceDate < :endDateTime ORDER BY i.invoiceDate DESC",
                    Invoice.class)
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .list();
        }
    }

    public double calculateTotalIncome(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Double total = session.createQuery(
                    "SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.invoiceDate >= :startOfDay AND i.invoiceDate < :endOfDay",
                    Double.class)
                    .setParameter("startOfDay", startOfDay)
                    .setParameter("endOfDay", endOfDay)
                    .uniqueResult();
            return total != null ? total : 0.0;
        }
    }

    public double calculateTotalIncome(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Double total = session.createQuery(
                    "SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.invoiceDate >= :startDateTime AND i.invoiceDate < :endDateTime",
                    Double.class)
                    .setParameter("startDateTime", startDateTime)
                    .setParameter("endDateTime", endDateTime)
                    .uniqueResult();
            return total != null ? total : 0.0;
        }
    }

    public String generateNextInvoiceNumber() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(i) FROM Invoice i", Long.class)
                    .uniqueResult();

            if (count == 0) {
                // First invoice - return default, user can edit
                return "INV-00001";
            } else {
                // Get the last invoice number and increment
                String lastInvoiceNumber = session.createQuery(
                        "SELECT i.invoiceNumber FROM Invoice i ORDER BY i.id DESC",
                        String.class)
                        .setMaxResults(1)
                        .uniqueResult();

                // Extract number from format INV-XXXXX
                if (lastInvoiceNumber != null && lastInvoiceNumber.startsWith("INV-")) {
                    String numberPart = lastInvoiceNumber.substring(4);
                    int nextNumber = Integer.parseInt(numberPart) + 1;
                    return String.format("INV-%07d", nextNumber);
                }

                // Fallback if format is unexpected
                return String.format("INV-%07d", count + 1);
            }
        }
    }

    public boolean isFirstInvoice() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(i) FROM Invoice i", Long.class)
                    .uniqueResult();
            return count == 0;
        }
    }

    public Invoice findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Invoice.class, id);
        }
    }
}
