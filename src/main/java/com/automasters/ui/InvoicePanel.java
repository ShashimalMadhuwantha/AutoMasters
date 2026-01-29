package com.automasters.ui;

import com.automasters.dao.InvoiceDAO;
import com.automasters.entity.Invoice;
import com.automasters.entity.InvoiceItem;
import com.automasters.util.ReceiptPrinter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InvoicePanel extends JPanel {

    private JTextField invoiceNumberField;
    private JTextField dateField;
    private JTextField customerNameField;
    private JTextField contactNumberField;
    private JTextField vehicleNumberField;
    private JTextField mileageField;
    private JTextField descriptionField;
    private JTextField priceField;
    private JLabel totalLabel;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private InvoiceDAO invoiceDAO;
    private int serialCounter = 1;

    public InvoicePanel() {
        invoiceDAO = new InvoiceDAO();
        initializeUI();
        generateNewInvoice();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("Create New Invoice");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(new Color(30, 41, 59));
        add(headerLabel, BorderLayout.NORTH);

        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(245, 247, 250));

        // Customer details card
        JPanel customerCard = createCustomerDetailsCard();
        contentPanel.add(customerCard, BorderLayout.NORTH);

        // Services section
        JPanel servicesPanel = createServicesPanel();
        contentPanel.add(servicesPanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Footer with total and save button
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createCustomerDetailsCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);

        // Row 1: Invoice Number and Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(createLabel("Invoice Number"), gbc);
        gbc.gridx = 1;
        invoiceNumberField = createTextField(true);
        card.add(invoiceNumberField, gbc);

        gbc.gridx = 2;
        card.add(createLabel("Date & Time"), gbc);
        gbc.gridx = 3;
        dateField = createTextField(false);
        card.add(dateField, gbc);

        // Row 2: Customer Name and Contact
        gbc.gridx = 0;
        gbc.gridy = 1;
        card.add(createLabel("Customer Name"), gbc);
        gbc.gridx = 1;
        customerNameField = createTextField(true);
        card.add(customerNameField, gbc);

        gbc.gridx = 2;
        card.add(createLabel("Contact Number"), gbc);
        gbc.gridx = 3;
        contactNumberField = createTextField(true);
        card.add(contactNumberField, gbc);

        // Row 3: Vehicle Number and Mileage
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(createLabel("Vehicle Number"), gbc);
        gbc.gridx = 1;
        vehicleNumberField = createTextField(true);
        card.add(vehicleNumberField, gbc);

        gbc.gridx = 2;
        card.add(createLabel("Current Mileage (km)"), gbc);
        gbc.gridx = 3;
        mileageField = createTextField(true);
        card.add(mileageField, gbc);

        return card;
    }

    private JPanel createServicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(245, 247, 250));

        // Add service form
        JPanel addServiceCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        addServiceCard.setBackground(Color.WHITE);
        addServiceCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 20, 15, 20)));

        addServiceCard.add(createLabel("Description"));
        descriptionField = new JTextField(25);
        styleTextField(descriptionField);
        descriptionField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    addService();
                }
            }
        });
        addServiceCard.add(descriptionField);

        addServiceCard.add(Box.createHorizontalStrut(10));
        addServiceCard.add(createLabel("Price (Rs.)"));
        priceField = new JTextField(10);
        styleTextField(priceField);
        priceField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    addService();
                }
            }
        });
        addServiceCard.add(priceField);

        addServiceCard.add(Box.createHorizontalStrut(10));
        JButton addButton = createButton("+ Add Service", new Color(34, 197, 94));
        addButton.addActionListener(e -> addService());
        addServiceCard.add(addButton);

        panel.add(addServiceCard, BorderLayout.NORTH);

        // Services table
        String[] columns = { "Srl No", "Description", "Price (Rs.)", "Action" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        itemsTable = new JTable(tableModel);
        itemsTable.setRowHeight(40);
        itemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemsTable.setSelectionBackground(new Color(219, 234, 254));
        itemsTable.setSelectionForeground(new Color(30, 41, 59));
        itemsTable.setGridColor(new Color(226, 232, 240));
        itemsTable.setShowGrid(true);

        // Header styling
        JTableHeader header = itemsTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));

        // Column widths
        itemsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        itemsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        itemsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        itemsTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Delete button in table
        itemsTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        itemsTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), this::removeService));

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        // Total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        totalPanel.setBackground(Color.WHITE);
        totalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 25, 15, 25)));

        JLabel totalTextLabel = new JLabel("Total Amount:");
        totalTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalTextLabel.setForeground(new Color(30, 41, 59));
        totalPanel.add(totalTextLabel);

        totalLabel = new JLabel("Rs. 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(new Color(34, 197, 94));
        totalPanel.add(totalLabel);

        footer.add(totalPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonsPanel.setBackground(new Color(245, 247, 250));

        JButton clearButton = createButton("Clear All", new Color(239, 68, 68));
        clearButton.addActionListener(e -> clearForm());
        buttonsPanel.add(clearButton);

        JButton saveButton = createButton("Save Invoice", new Color(59, 130, 246));
        saveButton.setPreferredSize(new Dimension(150, 40));
        saveButton.addActionListener(e -> saveInvoice());
        buttonsPanel.add(saveButton);

        JButton savePrintButton = createButton("Save & Print", new Color(16, 185, 129));
        savePrintButton.setPreferredSize(new Dimension(150, 40));
        savePrintButton.addActionListener(e -> saveAndPrintInvoice());
        buttonsPanel.add(savePrintButton);

        footer.add(buttonsPanel, BorderLayout.SOUTH);

        return footer;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(71, 85, 105));
        return label;
    }

    private JTextField createTextField(boolean editable) {
        JTextField field = new JTextField(15);
        styleTextField(field);
        field.setEditable(editable);
        if (!editable) {
            field.setBackground(new Color(248, 250, 252));
        }
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 12, 8, 12)));
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 38));
        return button;
    }

    // Public method to refresh data from database
    public void refresh() {
        generateNewInvoice();
    }

    private void generateNewInvoice() {
        String invoiceNumber = invoiceDAO.generateNextInvoiceNumber();
        invoiceNumberField.setText(invoiceNumber);

        // Make invoice number editable only for first invoice
        boolean isFirst = invoiceDAO.isFirstInvoice();
        invoiceNumberField.setEditable(isFirst);
        if (!isFirst) {
            invoiceNumberField.setBackground(new Color(248, 250, 252));
        }

        updateDateTime();
        serialCounter = 1;
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        dateField.setText(LocalDateTime.now().format(formatter));
    }

    private void addService() {
        // Validate all customer fields are filled first
        if (!validateCustomerFields()) {
            return;
        }

        String description = descriptionField.getText().trim();
        String priceText = priceField.getText().trim();

        if (description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service description.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        if (description.length() < 3) {
            JOptionPane.showMessageDialog(this, "Description must be at least 3 characters.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            descriptionField.requestFocus();
            return;
        }

        if (priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the service price.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                JOptionPane.showMessageDialog(this, "Price must be greater than zero.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                priceField.requestFocus();
                return;
            }
            tableModel.addRow(new Object[] { serialCounter++, description, String.format("%.2f", price), "Remove" });
            descriptionField.setText("");
            priceField.setText("");
            descriptionField.requestFocus();
            updateTotal();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric price.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            priceField.requestFocus();
        }
    }

    private void removeService(int row) {
        if (row >= 0 && row < tableModel.getRowCount()) {
            tableModel.removeRow(row);
            // Renumber serial numbers
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(i + 1, i, 0);
            }
            serialCounter = tableModel.getRowCount() + 1;
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            total += Double.parseDouble(tableModel.getValueAt(i, 2).toString());
        }
        totalLabel.setText(String.format("Rs. %.2f", total));
    }

    private Invoice createInvoiceFromForm() {
        String invoiceNumber = invoiceNumberField.getText().trim();
        String customerName = customerNameField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String vehicleNumber = vehicleNumberField.getText().trim();
        String mileageText = mileageField.getText().trim();

        // Validate invoice number format
        if (!validateInvoiceNumber(invoiceNumber)) {
            return null;
        }

        // Validate all required fields
        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return null;
        }

        if (contactNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter contact number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            contactNumberField.requestFocus();
            return null;
        }

        // Validate contact number format (10 digits)
        if (!contactNumber.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Contact number must be exactly 10 digits.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            contactNumberField.requestFocus();
            return null;
        }

        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter vehicle number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            vehicleNumberField.requestFocus();
            return null;
        }

        // Mileage is now required
        if (mileageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter current mileage.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            mileageField.requestFocus();
            return null;
        }

        Integer currentMileage = null;
        try {
            currentMileage = Integer.parseInt(mileageText);
            if (currentMileage < 0) {
                JOptionPane.showMessageDialog(this, "Mileage must be a positive number.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                mileageField.requestFocus();
                return null;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid mileage value.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            mileageField.requestFocus();
            return null;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Please add at least one service.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Invoice invoice = new Invoice(
                invoiceNumber,
                customerName,
                contactNumber,
                vehicleNumber.toUpperCase(),
                currentMileage);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int srlNo = (int) tableModel.getValueAt(i, 0);
            String description = tableModel.getValueAt(i, 1).toString();
            double price = Double.parseDouble(tableModel.getValueAt(i, 2).toString());
            invoice.addItem(new InvoiceItem(srlNo, description, price));
        }

        return invoice;
    }

    private void saveInvoice() {
        Invoice invoice = createInvoiceFromForm();
        if (invoice == null)
            return;

        try {
            invoiceDAO.save(invoice);

            JOptionPane.showMessageDialog(this,
                    "Invoice " + invoice.getInvoiceNumber() + " saved successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving invoice: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAndPrintInvoice() {
        Invoice invoice = createInvoiceFromForm();
        if (invoice == null)
            return;

        try {
            invoiceDAO.save(invoice);

            // Print receipt
            ReceiptPrinter printer = new ReceiptPrinter();
            printer.printInvoice(invoice);

            JOptionPane.showMessageDialog(this,
                    "Invoice " + invoice.getInvoiceNumber() + " saved and printed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving invoice: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        customerNameField.setText("");
        contactNumberField.setText("");
        vehicleNumberField.setText("");
        mileageField.setText("");
        descriptionField.setText("");
        priceField.setText("");
        tableModel.setRowCount(0);
        totalLabel.setText("Rs. 0.00");
        generateNewInvoice();
    }

    // Validation Methods
    private boolean validateCustomerFields() {
        String invoiceNumber = invoiceNumberField.getText().trim();
        String customerName = customerNameField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String vehicleNumber = vehicleNumberField.getText().trim();
        String mileageText = mileageField.getText().trim();

        if (!validateInvoiceNumber(invoiceNumber)) {
            return false;
        }

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            customerNameField.requestFocus();
            return false;
        }

        if (contactNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter contact number first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            contactNumberField.requestFocus();
            return false;
        }

        if (!contactNumber.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Contact number must be exactly 10 digits.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            contactNumberField.requestFocus();
            return false;
        }

        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter vehicle number first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            vehicleNumberField.requestFocus();
            return false;
        }

        if (mileageText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter current mileage first.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            mileageField.requestFocus();
            return false;
        }

        try {
            int mileage = Integer.parseInt(mileageText);
            if (mileage < 0) {
                JOptionPane.showMessageDialog(this, "Mileage must be a positive number.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                mileageField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid mileage value.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            mileageField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invoice number cannot be empty.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            invoiceNumberField.requestFocus();
            return false;
        }

        // Validate format: INV-XXXXXXX (7 digits)
        if (!invoiceNumber.matches("INV-\\d{7}")) {
            JOptionPane.showMessageDialog(this,
                    "Invoice number must be in format INV-XXXXXXX (7 digits).\nExample: INV-0000001",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            invoiceNumberField.requestFocus();
            return false;
        }

        return true;
    }

    // Button Renderer for table
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setForeground(Color.WHITE);
            setBackground(new Color(239, 68, 68));
            setBorderPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Remove");
            return this;
        }
    }

    // Button Editor for table
    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;
        private java.util.function.IntConsumer deleteAction;

        public ButtonEditor(JCheckBox checkBox, java.util.function.IntConsumer deleteAction) {
            super(checkBox);
            this.deleteAction = deleteAction;
            button = new JButton("Remove");
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(239, 68, 68));
            button.setBorderPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            SwingUtilities.invokeLater(() -> deleteAction.accept(currentRow));
            return "Remove";
        }
    }
}
