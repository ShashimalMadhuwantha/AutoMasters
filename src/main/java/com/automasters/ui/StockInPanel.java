package com.automasters.ui;

import com.automasters.dao.ItemDAO;
import com.automasters.dao.StockBatchDAO;
import com.automasters.entity.Item;
import com.automasters.entity.StockBatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StockInPanel extends JPanel {

    private JTextField searchField;
    private JComboBox<String> itemComboBox;
    private JTextField quantityField;
    private JTextField buyPriceField;
    private JTextField sellPriceField;
    private JTextField batchRefField;
    private JLabel totalStockLabel;
    private ItemDAO itemDAO;
    private StockBatchDAO stockBatchDAO;
    private Item selectedItem;

    public StockInPanel() {
        itemDAO = new ItemDAO();
        stockBatchDAO = new StockBatchDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("📥 Stock In");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Form panel
        JPanel formPanel = createFormPanel();
        contentPanel.add(formPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    // Public method to refresh data from database
    public void refresh() {
        // Don't clear selected item - only refresh when user changes dropdown
        // If item is selected, update the quantity display
        if (selectedItem != null) {
            updateQuantityDisplay();
        }
    }

    private void updateQuantityDisplay() {
        if (selectedItem != null) {
            int totalQty = stockBatchDAO.getTotalQuantity(selectedItem);
            totalStockLabel.setText("Current Stock: " + totalQty + " units");
        } else {
            totalStockLabel.setText("");
        }
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel titleLabel = new JLabel("Add Stock");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        int row = 0;

        // Search Item
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Search Item *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchField = createTextField();
        JButton searchButton = createSmallButton("Search");
        searchButton.addActionListener(e -> searchItem());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        fieldsPanel.add(searchPanel, gbc);

        row++;

        // Item Selection
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Selected Item *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemComboBox.setPreferredSize(new Dimension(0, 40));
        itemComboBox.addActionListener(e -> onItemSelected());
        fieldsPanel.add(itemComboBox, gbc);

        row++;

        // Current Total Stock
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(createLabel("Current Total Stock"), gbc);

        gbc.gridx = 1;
        totalStockLabel = createLabel("0");
        totalStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalStockLabel.setForeground(new Color(59, 130, 246));
        fieldsPanel.add(totalStockLabel, gbc);

        row++;

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(createLabel("Quantity *"), gbc);

        gbc.gridx = 1;
        quantityField = createTextField();
        fieldsPanel.add(quantityField, gbc);

        row++;

        // Buy Price
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(createLabel("Buy Price (Rs.) *"), gbc);

        gbc.gridx = 1;
        buyPriceField = createTextField();
        fieldsPanel.add(buyPriceField, gbc);

        row++;

        // Sell Price
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(createLabel("Sell Price (Rs.) *"), gbc);

        gbc.gridx = 1;
        sellPriceField = createTextField();
        fieldsPanel.add(sellPriceField, gbc);

        row++;

        // Batch Reference
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(createLabel("Batch Reference"), gbc);

        gbc.gridx = 1;
        batchRefField = createTextField();
        fieldsPanel.add(batchRefField, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton clearButton = createStyledButton("Clear", new Color(100, 116, 139));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);

        JButton addButton = createStyledButton("Add Stock", new Color(34, 197, 94));
        addButton.addActionListener(e -> addStock());
        buttonPanel.add(addButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchItem() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter item name to search.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            java.util.List<Item> items = itemDAO.searchItems(searchTerm);
            itemComboBox.removeAllItems();

            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No items found matching '" + searchTerm + "'.",
                        "No Results", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Item item : items) {
                itemComboBox.addItem(item.getItemName());
            }

            if (!items.isEmpty()) {
                selectedItem = items.get(0);
                updateTotalStock();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onItemSelected() {
        if (itemComboBox.getSelectedItem() != null) {
            String itemName = itemComboBox.getSelectedItem().toString();
            selectedItem = itemDAO.findByName(itemName);
            updateQuantityDisplay();
            updateTotalStock();
        }
    }

    private void updateTotalStock() {
        if (selectedItem != null) {
            int total = stockBatchDAO.getTotalQuantity(selectedItem);
            totalStockLabel.setText(String.valueOf(total));
        } else {
            totalStockLabel.setText("0");
        }
    }

    private void addStock() {
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please search and select an item first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            searchField.requestFocus();
            return;
        }

        String quantityText = quantityField.getText().trim();
        String buyPriceText = buyPriceField.getText().trim();
        String sellPriceText = sellPriceField.getText().trim();
        String batchRef = batchRefField.getText().trim();

        if (quantityText.isEmpty() || buyPriceText.isEmpty() || sellPriceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            double buyPrice = Double.parseDouble(buyPriceText);
            double sellPrice = Double.parseDouble(sellPriceText);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                quantityField.requestFocus();
                return;
            }

            if (buyPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Buy price must be greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                buyPriceField.requestFocus();
                return;
            }

            if (sellPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Sell price must be greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                sellPriceField.requestFocus();
                return;
            }

            if (sellPrice < buyPrice) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Sell price is less than buy price. Continue?",
                        "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            StockBatch batch = new StockBatch(selectedItem, quantity, buyPrice, sellPrice,
                    batchRef.isEmpty() ? null : batchRef);
            stockBatchDAO.save(batch);

            // Log transaction
            com.automasters.entity.StockTransaction transaction = new com.automasters.entity.StockTransaction(
                    selectedItem, "STOCK_IN", quantity, buyPrice, sellPrice, batchRef.isEmpty() ? null : batchRef);
            com.automasters.dao.StockTransactionDAO transactionDAO = new com.automasters.dao.StockTransactionDAO();
            transactionDAO.save(transaction);

            JOptionPane.showMessageDialog(this,
                    String.format("Stock added successfully!\n%d units of '%s' @ Rs.%.2f buy / Rs.%.2f sell",
                            quantity, selectedItem.getItemName(), buyPrice, sellPrice),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            updateTotalStock();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and prices.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding stock: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        quantityField.setText("");
        buyPriceField.setText("");
        sellPriceField.setText("");
        batchRefField.setText("");
        quantityField.requestFocus();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(71, 85, 105));
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        return button;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(59, 130, 246));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 40));
        return button;
    }
}
