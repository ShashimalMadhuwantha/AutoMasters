package com.automasters.dao;

import com.automasters.entity.Item;
import com.automasters.entity.StockTransaction;
import com.automasters.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class StockTransactionDAO {

    public void save(StockTransaction transaction) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<StockTransaction> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockTransaction st ORDER BY st.transactionDate DESC",
                    StockTransaction.class)
                    .list();
        }
    }

    public List<StockTransaction> findByItem(Item item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockTransaction st WHERE st.item = :item ORDER BY st.transactionDate DESC",
                    StockTransaction.class)
                    .setParameter("item", item)
                    .list();
        }
    }

    public List<StockTransaction> findByType(String transactionType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockTransaction st WHERE st.transactionType = :type ORDER BY st.transactionDate DESC",
                    StockTransaction.class)
                    .setParameter("type", transactionType)
                    .list();
        }
    }

    public List<StockTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockTransaction st WHERE st.transactionDate BETWEEN :start AND :end ORDER BY st.transactionDate DESC",
                    StockTransaction.class)
                    .setParameter("start", startDate)
                    .setParameter("end", endDate)
                    .list();
        }
    }

    public List<StockTransaction> findByItemAndType(Item item, String transactionType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockTransaction st WHERE st.item = :item AND st.transactionType = :type ORDER BY st.transactionDate DESC",
                    StockTransaction.class)
                    .setParameter("item", item)
                    .setParameter("type", transactionType)
                    .list();
        }
    }
}
