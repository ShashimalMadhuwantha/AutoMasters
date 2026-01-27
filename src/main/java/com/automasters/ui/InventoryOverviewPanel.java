package com.automasters.ui;

import com.automasters.dao.ItemDAO;
import com.automasters.dao.StockBatchDAO;
import com.automasters.entity.Item;
import com.automasters.entity.StockBatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class InventoryOverviewPanel extends JPanel {

    private JTextField searchField;
    private JTable itemsTable;
    private JTable batchesTable;
    private DefaultTableModel itemsTableModel;
    private DefaultTableModel batchesTableModel;
    private ItemDAO itemDAO;
    private StockBatchDAO stockBatchDAO;
    private JLabel selectedItemLabel;
    private JLabel totalStockLabel;

    public InventoryOverviewPanel() {
        itemDAO = new ItemDAO();
        stockBatchDAO = new StockBatchDAO();
        initializeUI();
        loadAllItems();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("📋 Inventory Overview");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content - split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setBackground(new Color(245, 247, 250));

        // Left panel - Items list
        JPanel leftPanel = createItemsPanel();
        splitPane.setLeftComponent(leftPanel);

        // Right panel - Batch details
        JPanel rightPanel = createBatchDetailsPanel();
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        // Title
        JLabel titleLabel = new JLabel("Items");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        searchField = createTextField();
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchItems();
            }
        });

        JButton searchButton = createStyledButton("Search", new Color(59, 130, 246));
        searchButton.addActionListener(e -> searchItems());

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Items table
        String[] columns = { "Item Name", "Total Stock" };
        itemsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        itemsTable = new JTable(itemsTableModel);
        itemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemsTable.setRowHeight(35);
        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        itemsTable.setSelectionBackground(new Color(219, 234, 254));
        itemsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add selection listener
        itemsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadBatchesForSelectedItem();
            }
        });

        // Color code low stock
        itemsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && column == 1) {
                    try {
                        int stock = Integer.parseInt(value.toString());
                        if (stock == 0) {
                            c.setBackground(new Color(254, 226, 226)); // Light red
                        } else if (stock < 10) {
                            c.setBackground(new Color(254, 243, 199)); // Light yellow
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } catch (NumberFormatException ex) {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        // Combine search and table
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBatchDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        // Header with item info
        JPanel headerPanel = new JPanel(new BorderLayout(10, 5));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        selectedItemLabel = new JLabel("Select an item to view batch details");
        selectedItemLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        selectedItemLabel.setForeground(new Color(30, 41, 59));

        totalStockLabel = new JLabel("");
        totalStockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalStockLabel.setForeground(new Color(71, 85, 105));

        headerPanel.add(selectedItemLabel, BorderLayout.NORTH);
        headerPanel.add(totalStockLabel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Batches table
        String[] columns = { "Batch ID", "Date", "Reference", "Qty", "Buy Price", "Sell Price", "Age (days)" };
        batchesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        batchesTable = new JTable(batchesTableModel);
        batchesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        batchesTable.setRowHeight(35);
        batchesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        batchesTable.getTableHeader().setBackground(new Color(241, 245, 249));
        batchesTable.setSelectionBackground(new Color(219, 234, 254));

        // Color code by age
        batchesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected && column == 6) { // Age column
                    try {
                        int age = Integer.parseInt(value.toString());
                        if (age > 180) { // Older than 6 months
                            c.setBackground(new Color(254, 226, 226)); // Light red
                        } else if (age > 90) { // Older than 3 months
                            c.setBackground(new Color(254, 243, 199)); // Light yellow
                        } else {
                            c.setBackground(new Color(220, 252, 231)); // Light green
                        }
                    } catch (NumberFormatException ex) {
                        c.setBackground(Color.WHITE);
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(batchesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadAllItems() {
        itemsTableModel.setRowCount(0);
        try {
            List<Item> items = itemDAO.findAll();
            for (Item item : items) {
                int totalStock = stockBatchDAO.getTotalQuantity(item);
                itemsTableModel.addRow(new Object[] {
                        item.getItemName(),
                        totalStock
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchItems() {
        String searchTerm = searchField.getText().trim();
        itemsTableModel.setRowCount(0);

        try {
            List<Item> items;
            if (searchTerm.isEmpty()) {
                items = itemDAO.findAll();
            } else {
                items = itemDAO.searchItems(searchTerm);
            }

            for (Item item : items) {
                int totalStock = stockBatchDAO.getTotalQuantity(item);
                itemsTableModel.addRow(new Object[] {
                        item.getItemName(),
                        totalStock
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBatchesForSelectedItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        batchesTableModel.setRowCount(0);

        try {
            String itemName = (String) itemsTableModel.getValueAt(selectedRow, 0);
            int totalStock = (Integer) itemsTableModel.getValueAt(selectedRow, 1);

            Item item = itemDAO.findByName(itemName);
            if (item == null) {
                return;
            }

            // Update header
            selectedItemLabel.setText(itemName);
            totalStockLabel.setText(String.format("Total Stock: %d units", totalStock));

            // Load batches
            List<StockBatch> batches = stockBatchDAO.findByItem(item);
            LocalDate today = LocalDate.now();

            for (StockBatch batch : batches) {
                // Convert LocalDateTime to LocalDate for age calculation
                LocalDate batchDate = batch.getBatchDate().toLocalDate();
                long age = ChronoUnit.DAYS.between(batchDate, today);

                batchesTableModel.addRow(new Object[] {
                        batch.getId(),
                        batchDate.toString(),
                        batch.getBatchReference() != null ? batch.getBatchReference() : "-",
                        batch.getQuantity(),
                        String.format("Rs.%.2f", batch.getBuyPrice()),
                        String.format("Rs.%.2f", batch.getSellPrice()),
                        age
                });
            }

            if (batches.isEmpty()) {
                selectedItemLabel.setText(itemName + " (No batches)");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading batches: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        button.setPreferredSize(new Dimension(100, 40));
        return button;
    }
}
