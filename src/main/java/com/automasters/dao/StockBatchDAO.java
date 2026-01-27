package com.automasters.dao;

import com.automasters.entity.Item;
import com.automasters.entity.StockBatch;
import com.automasters.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class StockBatchDAO {

    public void save(StockBatch batch) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(batch);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public StockBatch findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(StockBatch.class, id);
        }
    }

    public List<StockBatch> findByItem(Item item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockBatch sb WHERE sb.item = :item ORDER BY sb.batchDate",
                    StockBatch.class)
                    .setParameter("item", item)
                    .list();
        }
    }

    public List<StockBatch> findAvailableBatches(Item item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM StockBatch sb WHERE sb.item = :item AND sb.quantity > 0 ORDER BY sb.batchDate",
                    StockBatch.class)
                    .setParameter("item", item)
                    .list();
        }
    }

    public Integer getTotalQuantity(Item item) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long total = session.createQuery(
                    "SELECT SUM(sb.quantity) FROM StockBatch sb WHERE sb.item = :item",
                    Long.class)
                    .setParameter("item", item)
                    .uniqueResult();
            return total != null ? total.intValue() : 0;
        }
    }

    public void updateQuantity(StockBatch batch, int newQuantity) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            batch.setQuantity(newQuantity);
            session.merge(batch);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void reduceQuantity(StockBatch batch, int quantityToReduce) {
        if (quantityToReduce > batch.getQuantity()) {
            throw new IllegalArgumentException("Cannot reduce more than available quantity");
        }
        int newQuantity = batch.getQuantity() - quantityToReduce;
        updateQuantity(batch, newQuantity);
    }

    public void update(StockBatch batch) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(batch);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void delete(StockBatch batch) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(batch);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
