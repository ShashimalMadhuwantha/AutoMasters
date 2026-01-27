package com.automasters.dao;

import com.automasters.entity.Item;
import com.automasters.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ItemDAO {

    public void save(Item item) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public Item findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Item.class, id);
        }
    }

    public Item findByName(String itemName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Item i WHERE LOWER(i.itemName) = LOWER(:name)",
                    Item.class)
                    .setParameter("name", itemName.trim())
                    .uniqueResult();
        }
    }

    public boolean existsByName(String itemName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(i) FROM Item i WHERE LOWER(i.itemName) = LOWER(:name)",
                    Long.class)
                    .setParameter("name", itemName.trim())
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    public List<Item> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Item i ORDER BY i.itemName", Item.class)
                    .list();
        }
    }

    public List<Item> searchItems(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Item i WHERE LOWER(i.itemName) LIKE LOWER(:search) ORDER BY i.itemName",
                    Item.class)
                    .setParameter("search", "%" + searchTerm.trim() + "%")
                    .list();
        }
    }

    public void update(Item item) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Item> findSimilarItems(String itemName, double similarityThreshold) {
        // Get all items and filter by similarity
        List<Item> allItems = findAll();
        List<Item> similarItems = new java.util.ArrayList<>();

        for (Item item : allItems) {
            if (com.automasters.util.StringSimilarity.areSimilar(itemName, item.getItemName(), similarityThreshold)) {
                similarItems.add(item);
            }
        }

        return similarItems;
    }

    public void delete(Item item) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(item);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
}
