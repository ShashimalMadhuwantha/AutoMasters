package com.automasters.ui;

import com.automasters.dao.ItemDAO;
import com.automasters.dao.StockBatchDAO;
import com.automasters.entity.Item;
import com.automasters.entity.StockBatch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockOutPanel extends JPanel {

    private JTextField searchField;
    private JComboBox<String> itemComboBox;
    private JLabel totalStockLabel;
    private JTable batchesTable;
    private DefaultTableModel tableModel;
    private ItemDAO itemDAO;
    private StockBatchDAO stockBatchDAO;
    private Item selectedItem;
    private List<StockBatch> currentBatches;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StockOutPanel() {
        itemDAO = new ItemDAO();
        stockBatchDAO = new StockBatchDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("📤 Stock Out");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Search panel
        JPanel searchPanel = createSearchPanel();
        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Batches table
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    // Public method to refresh data from database
    public void refresh() {
        // If item is selected, reload its batches to show updated quantities
        if (selectedItem != null) {
            loadBatches();
        }
        // Don't clear selected item - only refresh when user changes dropdown
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Select Item");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Search Item
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Search Item"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel searchInputPanel = new JPanel(new BorderLayout(5, 0));
        searchInputPanel.setBackground(Color.WHITE);
        searchField = createTextField();
        JButton searchButton = createSmallButton("Search");
        searchButton.addActionListener(e -> searchItem());
        searchInputPanel.add(searchField, BorderLayout.CENTER);
        searchInputPanel.add(searchButton, BorderLayout.EAST);
        fieldsPanel.add(searchInputPanel, gbc);

        // Item Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        fieldsPanel.add(createLabel("Selected Item"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemComboBox.setPreferredSize(new Dimension(0, 40));
        itemComboBox.addActionListener(e -> onItemSelected());
        fieldsPanel.add(itemComboBox, gbc);

        // Total Stock
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(createLabel("Total Available Stock"), gbc);

        gbc.gridx = 1;
        totalStockLabel = createLabel("0");
        totalStockLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalStockLabel.setForeground(new Color(34, 197, 94));
        fieldsPanel.add(totalStockLabel, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Available Stock Batches");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = { "Batch ID", "Batch Date", "Buy Price (Rs.)", "Sell Price (Rs.)", "Available Qty",
                "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Action column
            }
        };

        batchesTable = new JTable(tableModel);
        batchesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        batchesTable.setRowHeight(40);
        batchesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        batchesTable.getTableHeader().setBackground(new Color(241, 245, 249));
        batchesTable.setSelectionBackground(new Color(219, 234, 254));

        // Add button column
        batchesTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        batchesTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(batchesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

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
            List<Item> items = itemDAO.searchItems(searchTerm);
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
                loadBatches();
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
            loadBatches();
        }
    }

    private void loadBatches() {
        tableModel.setRowCount(0);
        if (selectedItem == null) {
            totalStockLabel.setText("0");
            return;
        }

        try {
            currentBatches = stockBatchDAO.findAvailableBatches(selectedItem);
            int totalQty = 0;

            for (StockBatch batch : currentBatches) {
                totalQty += batch.getQuantity();
                tableModel.addRow(new Object[] {
                        batch.getId(),
                        batch.getBatchDate().format(formatter),
                        String.format("%.2f", batch.getBuyPrice()),
                        String.format("%.2f", batch.getSellPrice()),
                        batch.getQuantity(),
                        "Use"
                });
            }

            totalStockLabel.setText(String.valueOf(totalQty));

            if (currentBatches.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No stock available for '" + selectedItem.getItemName() + "'.",
                        "No Stock", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading batches: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void useBatch(int row) {
        if (row < 0 || row >= currentBatches.size()) {
            return;
        }

        StockBatch batch = currentBatches.get(row);

        String input = JOptionPane.showInputDialog(this,
                String.format("Enter quantity to use (Available: %d):", batch.getQuantity()),
                "Use Stock",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return; // User cancelled
        }

        try {
            int qtyToUse = Integer.parseInt(input.trim());

            if (qtyToUse <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (qtyToUse > batch.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        String.format("Cannot use %d units. Only %d available in this batch.",
                                qtyToUse, batch.getQuantity()),
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Calculate remaining before reduction
            int remainingQty = batch.getQuantity() - qtyToUse;

            // Reduce stock
            stockBatchDAO.reduceQuantity(batch, qtyToUse);

            // Log transaction
            com.automasters.entity.StockTransaction transaction = new com.automasters.entity.StockTransaction(
                    selectedItem, "STOCK_OUT", qtyToUse);
            com.automasters.dao.StockTransactionDAO transactionDAO = new com.automasters.dao.StockTransactionDAO();
            transactionDAO.save(transaction);

            JOptionPane.showMessageDialog(this,
                    String.format("Successfully used %d units from batch.\nRemaining: %d",
                            qtyToUse, remainingQty),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            // Reload batches
            loadBatches();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error using stock: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    // Button Renderer and Editor for table
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
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
            setText((value == null) ? "Use" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
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
            label = (value == null) ? "Use" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                useBatch(currentRow);
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
