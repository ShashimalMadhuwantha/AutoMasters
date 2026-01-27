package com.automasters.ui;

import com.automasters.dao.ItemDAO;
import com.automasters.entity.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ItemManagementPanel extends JPanel {

    private JTextField itemNameField;
    private JTextField descriptionField;
    private JTextField searchField;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private ItemDAO itemDAO;

    public ItemManagementPanel() {
        itemDAO = new ItemDAO();
        initializeUI();
        loadAllItems();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("📦 Item Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Add item form
        JPanel formPanel = createFormPanel();
        contentPanel.add(formPanel, BorderLayout.NORTH);

        // Items table
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Add New Item");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Item Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Item Name *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        itemNameField = createTextField();
        fieldsPanel.add(itemNameField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Description"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        descriptionField = createTextField();
        fieldsPanel.add(descriptionField, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Add button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton addButton = createStyledButton("Add Item", new Color(59, 130, 246));
        addButton.addActionListener(e -> addItem());
        buttonPanel.add(addButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search Items:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(searchLabel, BorderLayout.WEST);

        searchField = createTextField();
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchItems();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Item Name", "Description", "Created Date", "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Action column
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemsTable.setRowHeight(35);
        itemsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        itemsTable.setSelectionBackground(new Color(219, 234, 254));

        // Add delete button column
        itemsTable.getColumn("Action").setCellRenderer(new DeleteButtonRenderer());
        itemsTable.getColumn("Action").setCellEditor(new DeleteButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addItem() {
        String itemName = itemNameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (itemName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter item name.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            itemNameField.requestFocus();
            return;
        }

        // Minimum length validation
        if (itemName.length() < 2) {
            JOptionPane.showMessageDialog(this, "Item name must be at least 2 characters.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            itemNameField.requestFocus();
            return;
        }

        // Check for exact duplicate (case-insensitive)
        if (itemDAO.existsByName(itemName)) {
            JOptionPane.showMessageDialog(this,
                    "Item '" + itemName + "' already exists!\nItem names are case-insensitive.",
                    "Duplicate Item", JOptionPane.WARNING_MESSAGE);
            itemNameField.requestFocus();
            return;
        }

        // Check for similar items (fuzzy matching to catch typos)
        java.util.List<Item> similarItems = itemDAO.findSimilarItems(itemName, 75.0);

        if (!similarItems.isEmpty()) {
            // Build message showing similar items
            StringBuilder message = new StringBuilder();
            message.append("Similar items found! Did you mean one of these?\n\n");

            for (Item similar : similarItems) {
                message.append("• ").append(similar.getItemName());
                if (similar.getDescription() != null && !similar.getDescription().isEmpty()) {
                    message.append(" (").append(similar.getDescription()).append(")");
                }
                message.append("\n");
            }

            message.append("\nAre you sure you want to add '").append(itemName).append("' as a new item?");

            int choice = JOptionPane.showConfirmDialog(this,
                    message.toString(),
                    "Similar Items Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                itemNameField.requestFocus();
                return;
            }
        }

        try {
            Item item = new Item(itemName, description);
            itemDAO.save(item);

            JOptionPane.showMessageDialog(this,
                    "Item '" + itemName + "' added successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear form
            itemNameField.setText("");
            descriptionField.setText("");
            itemNameField.requestFocus();

            // Reload table
            loadAllItems();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error adding item: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchItems() {
        String searchTerm = searchField.getText().trim();
        tableModel.setRowCount(0);

        try {
            java.util.List<Item> items;
            if (searchTerm.isEmpty()) {
                items = itemDAO.findAll();
            } else {
                items = itemDAO.searchItems(searchTerm);
            }

            for (Item item : items) {
                tableModel.addRow(new Object[] {
                        item.getId(),
                        item.getItemName(),
                        item.getDescription() != null ? item.getDescription() : "",
                        item.getCreatedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        "Delete"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error searching items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Public method to refresh data from database
    public void refresh() {
        loadAllItems();
    }

    private void loadAllItems() {
        searchField.setText("");
        searchItems();
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

    private void deleteItem(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return;
        }

        Long itemId = (Long) tableModel.getValueAt(row, 0);
        String itemName = (String) tableModel.getValueAt(row, 1);

        // Check if item has stock
        Item item = itemDAO.findById(itemId);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Item not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        com.automasters.dao.StockBatchDAO stockBatchDAO = new com.automasters.dao.StockBatchDAO();
        int totalStock = stockBatchDAO.getTotalQuantity(item);

        if (totalStock > 0) {
            JOptionPane.showMessageDialog(this,
                    String.format(
                            "Cannot delete '%s'!\nThis item has %d units in stock.\nPlease use all stock before deleting.",
                            itemName, totalStock),
                    "Cannot Delete",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + itemName + "'?\nThis action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                itemDAO.delete(item);
                JOptionPane.showMessageDialog(this,
                        "Item '" + itemName + "' deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllItems();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting item: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Button Renderer for Delete column
    private class DeleteButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public DeleteButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setBackground(new Color(239, 68, 68));
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Delete" : value.toString());
            return this;
        }
    }

    private class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(239, 68, 68));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "Delete" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                deleteItem(currentRow);
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
