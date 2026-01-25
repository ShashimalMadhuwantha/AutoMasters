package com.automasters.ui;

import com.automasters.dao.InvoiceDAO;
import com.automasters.entity.Invoice;
import com.automasters.entity.InvoiceItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistoryPanel extends JPanel {

    private JTextField searchField;
    private JTable invoicesTable;
    private JTable detailsTable;
    private DefaultTableModel invoicesTableModel;
    private DefaultTableModel detailsTableModel;
    private InvoiceDAO invoiceDAO;
    private JLabel customerInfoLabel;
    private JLabel totalAmountLabel;

    public HistoryPanel() {
        invoiceDAO = new InvoiceDAO();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("Vehicle Service History");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Search panel
        JPanel searchCard = createSearchPanel();
        contentPanel.add(searchCard, BorderLayout.NORTH);

        // Split pane for invoices and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(245, 247, 250));

        // Invoices list
        JPanel invoicesPanel = createInvoicesPanel();
        splitPane.setTopComponent(invoicesPanel);

        // Invoice details
        JPanel detailsPanel = createDetailsPanel();
        splitPane.setBottomComponent(detailsPanel);

        contentPanel.add(splitPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel searchLabel = new JLabel("Search Vehicle Number:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(new Color(71, 85, 105));
        card.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        searchField.addActionListener(e -> searchVehicle());
        card.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(new Color(59, 130, 246));
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(120, 40));
        searchButton.addActionListener(e -> searchVehicle());
        card.add(searchButton);

        return card;
    }

    private JPanel createInvoicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel("Invoices");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Invoice No", "Date & Time", "Customer Name", "Contact", "Vehicle No", "Total (Rs.)"};
        invoicesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoicesTable = new JTable(invoicesTableModel);
        invoicesTable.setRowHeight(35);
        invoicesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        invoicesTable.setSelectionBackground(new Color(219, 234, 254));
        invoicesTable.setSelectionForeground(new Color(30, 41, 59));
        invoicesTable.setGridColor(new Color(226, 232, 240));
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = invoicesTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = invoicesTable.getSelectedRow();
                if (row >= 0) {
                    showInvoiceDetails(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(invoicesTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Header with customer info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Invoice Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        customerInfoLabel = new JLabel("Select an invoice to view details");
        customerInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customerInfoLabel.setForeground(new Color(100, 116, 139));
        headerPanel.add(customerInfoLabel, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Services table
        String[] columns = {"Srl No", "Description", "Price (Rs.)"};
        detailsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        detailsTable = new JTable(detailsTableModel);
        detailsTable.setRowHeight(35);
        detailsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsTable.setGridColor(new Color(226, 232, 240));

        JTableHeader header = detailsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        detailsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        detailsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        detailsTable.getColumnModel().getColumn(2).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(detailsTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Total amount footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        totalAmountLabel = new JLabel("Total: Rs. 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalAmountLabel.setForeground(new Color(34, 197, 94));
        footerPanel.add(totalAmountLabel);

        panel.add(footerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchVehicle() {
        String vehicleNumber = searchField.getText().trim();

        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a vehicle number to search.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Invoice> invoices = invoiceDAO.findByVehicleNumber(vehicleNumber);

            invoicesTableModel.setRowCount(0);
            detailsTableModel.setRowCount(0);
            customerInfoLabel.setText("Select an invoice to view details");
            totalAmountLabel.setText("Total: Rs. 0.00");

            if (invoices.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No invoices found for vehicle: " + vehicleNumber,
                        "No Results", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            for (Invoice invoice : invoices) {
                invoicesTableModel.addRow(new Object[]{
                        invoice.getInvoiceNumber(),
                        invoice.getInvoiceDate().format(formatter),
                        invoice.getCustomerName(),
                        invoice.getContactNumber(),
                        invoice.getVehicleNumber(),
                        String.format("%.2f", invoice.getTotalAmount())
                });
            }

            // Store invoices for detail lookup
            invoicesTable.putClientProperty("invoices", invoices);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void showInvoiceDetails(int row) {
        List<Invoice> invoices = (List<Invoice>) invoicesTable.getClientProperty("invoices");
        if (invoices == null || row >= invoices.size()) return;

        Invoice invoice = invoices.get(row);

        customerInfoLabel.setText(String.format("%s | %s | %s",
                invoice.getInvoiceNumber(),
                invoice.getCustomerName(),
                invoice.getVehicleNumber()));

        detailsTableModel.setRowCount(0);

        for (InvoiceItem item : invoice.getItems()) {
            detailsTableModel.addRow(new Object[]{
                    item.getSerialNumber(),
                    item.getDescription(),
                    String.format("%.2f", item.getPrice())
            });
        }

        totalAmountLabel.setText(String.format("Total: Rs. %.2f", invoice.getTotalAmount()));
    }
}
