package com.automasters.ui;

import com.automasters.dao.ItemDAO;
import com.automasters.dao.StockTransactionDAO;
import com.automasters.entity.Item;
import com.automasters.entity.StockTransaction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StockHistoryPanel extends JPanel {

    private JTextField searchField;
    private JComboBox<String> itemComboBox;
    private JComboBox<String> typeComboBox;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private StockTransactionDAO transactionDAO;
    private ItemDAO itemDAO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StockHistoryPanel() {
        transactionDAO = new StockTransactionDAO();
        itemDAO = new ItemDAO();
        initializeUI();
        loadAllTransactions();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("📊 Stock History");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Filter panel
        JPanel filterPanel = createFilterPanel();
        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // History table
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Filter Transactions");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Filter fields
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Search Item
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        fieldsPanel.add(createLabel("Search Item"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchField = createTextField();
        JButton searchButton = createSmallButton("Search");
        searchButton.addActionListener(e -> searchItem());
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        fieldsPanel.add(searchPanel, gbc);

        // Item Selection
        gbc.gridx = 2;
        gbc.weightx = 0.2;
        fieldsPanel.add(createLabel("Item"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        itemComboBox = new JComboBox<>();
        itemComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        itemComboBox.setPreferredSize(new Dimension(0, 35));
        itemComboBox.addItem("All Items");
        itemComboBox.addActionListener(e -> filterTransactions());
        fieldsPanel.add(itemComboBox, gbc);

        // Transaction Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        fieldsPanel.add(createLabel("Type"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        typeComboBox = new JComboBox<>();
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeComboBox.setPreferredSize(new Dimension(0, 35));
        typeComboBox.addItem("All");
        typeComboBox.addItem("STOCK_IN");
        typeComboBox.addItem("STOCK_OUT");
        typeComboBox.addActionListener(e -> filterTransactions());
        fieldsPanel.add(typeComboBox, gbc);

        // Reset button
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        JButton resetButton = createStyledButton("Reset Filters", new Color(100, 116, 139));
        resetButton.addActionListener(e -> resetFilters());
        fieldsPanel.add(resetButton, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = { "Date", "Item", "Type", "Quantity", "Buy Price", "Sell Price", "Batch Ref" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(35);
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(241, 245, 249));
        historyTable.setSelectionBackground(new Color(219, 234, 254));

        // Color code transaction types
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String type = (String) table.getValueAt(row, 2);
                    if ("STOCK_IN".equals(type)) {
                        c.setBackground(new Color(220, 252, 231)); // Light green
                    } else if ("STOCK_OUT".equals(type)) {
                        c.setBackground(new Color(254, 226, 226)); // Light red
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void searchItem() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            return;
        }

        try {
            List<Item> items = itemDAO.searchItems(searchTerm);
            itemComboBox.removeAllItems();
            itemComboBox.addItem("All Items");

            for (Item item : items) {
                itemComboBox.addItem(item.getItemName());
            }

            if (!items.isEmpty()) {
                itemComboBox.setSelectedIndex(1); // Select first found item
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching items: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTransactions() {
        tableModel.setRowCount(0);

        try {
            List<StockTransaction> transactions;
            String selectedItem = (String) itemComboBox.getSelectedItem();
            String selectedType = (String) typeComboBox.getSelectedItem();

            if (selectedItem != null && !selectedItem.equals("All Items")) {
                Item item = itemDAO.findByName(selectedItem);
                if (item != null) {
                    if (selectedType != null && !selectedType.equals("All")) {
                        transactions = transactionDAO.findByItemAndType(item, selectedType);
                    } else {
                        transactions = transactionDAO.findByItem(item);
                    }
                } else {
                    transactions = transactionDAO.findAll();
                }
            } else if (selectedType != null && !selectedType.equals("All")) {
                transactions = transactionDAO.findByType(selectedType);
            } else {
                transactions = transactionDAO.findAll();
            }

            displayTransactions(transactions);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error filtering transactions: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAllTransactions() {
        tableModel.setRowCount(0);
        try {
            List<StockTransaction> transactions = transactionDAO.findAll();
            displayTransactions(transactions);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayTransactions(List<StockTransaction> transactions) {
        for (StockTransaction tx : transactions) {
            tableModel.addRow(new Object[] {
                    tx.getTransactionDate().format(formatter),
                    tx.getItem().getItemName(),
                    tx.getTransactionType(),
                    tx.getQuantity(),
                    tx.getBuyPrice() != null ? String.format("Rs.%.2f", tx.getBuyPrice()) : "-",
                    tx.getSellPrice() != null ? String.format("Rs.%.2f", tx.getSellPrice()) : "-",
                    tx.getBatchReference() != null ? tx.getBatchReference() : "-"
            });
        }
    }

    private void resetFilters() {
        searchField.setText("");
        itemComboBox.setSelectedIndex(0);
        typeComboBox.setSelectedIndex(0);
        loadAllTransactions();
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
        button.setPreferredSize(new Dimension(80, 35));
        return button;
    }
}
