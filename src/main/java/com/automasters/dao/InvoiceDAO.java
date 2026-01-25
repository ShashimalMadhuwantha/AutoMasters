package com.automasters.dao;

import com.automasters.entity.Invoice;
import com.automasters.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    public String generateNextInvoiceNumber() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(i) FROM Invoice i", Long.class)
                    .uniqueResult();
            return String.format("INV-%06d", count + 1);
        }
    }

    public Invoice findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Invoice.class, id);
        }
    }
}
