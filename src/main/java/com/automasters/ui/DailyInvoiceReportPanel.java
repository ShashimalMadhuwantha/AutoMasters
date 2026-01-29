package com.automasters.ui;

import com.automasters.dao.InvoiceDAO;
import com.automasters.entity.Invoice;
import com.automasters.util.PDFReportGenerator;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class DailyInvoiceReportPanel extends JPanel {

    private final InvoiceDAO invoiceDAO;
    private JDateChooser dateChooser;
    private JTable invoiceTable;
    private DefaultTableModel tableModel;
    private JLabel totalIncomeLabel;
    private JButton exportButton;

    public DailyInvoiceReportPanel() {
        this.invoiceDAO = new InvoiceDAO();
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Center Content (Table)
        add(createTableSection(), BorderLayout.CENTER);

        // Sidebar/Filters
        add(createSidebar(), BorderLayout.EAST);

        // Initial load
        refreshData();
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Daily Invoice Report");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(30, 41, 59));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        sidebar.setPreferredSize(new Dimension(300, 0));

        // Date Filter Section
        JLabel filterLabel = new JLabel("Select Date");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date()); // Default to today
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(250, 35));
        dateChooser.setMaximumSize(new Dimension(250, 35));
        dateChooser.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Auto-refresh when date changes
        dateChooser.addPropertyChangeListener("date", evt -> refreshData());

        // Summary Section
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(new Color(241, 245, 249));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        summaryPanel.setMaximumSize(new Dimension(250, 100));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summaryTitle = new JLabel("Total Income");
        summaryTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryTitle.setForeground(new Color(100, 116, 139));

        totalIncomeLabel = new JLabel("LKR 0.00");
        totalIncomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalIncomeLabel.setForeground(new Color(15, 23, 42));

        summaryPanel.add(summaryTitle);
        summaryPanel.add(Box.createVerticalStrut(5));
        summaryPanel.add(totalIncomeLabel);

        // Export Button
        exportButton = new JButton("Export PDF Report");
        exportButton.setBackground(new Color(16, 185, 129)); // Green
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setBorderPainted(false); // Fix for Windows L&F background color
        exportButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportButton.setMaximumSize(new Dimension(250, 40));
        exportButton.addActionListener(e -> exportPDF());

        // Add components to sidebar
        sidebar.add(filterLabel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(dateChooser);
        sidebar.add(Box.createVerticalStrut(30));
        sidebar.add(summaryPanel);
        sidebar.add(Box.createVerticalGlue()); // Push export button to bottom
        sidebar.add(exportButton);

        return sidebar;
    }

    private JPanel createTableSection() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        String[] columnNames = { "Invoice #", "Time", "Customer", "Vehicle", "Amount (LKR)" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        invoiceTable = new JTable(tableModel);
        invoiceTable.setRowHeight(35);
        invoiceTable.setShowVerticalLines(false);
        invoiceTable.setIntercellSpacing(new Dimension(0, 0));
        invoiceTable.getTableHeader().setBackground(new Color(248, 250, 252));
        invoiceTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        invoiceTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        // Right align amount column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        invoiceTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scrollPane = new JScrollPane(invoiceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    public void refreshData() {
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null)
            return;

        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Invoice> invoices = invoiceDAO.findByDateRange(date);
        double totalIncome = invoiceDAO.calculateTotalIncome(date);

        // Update Table
        tableModel.setRowCount(0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Invoice inv : invoices) {
            Object[] row = {
                    inv.getInvoiceNumber(),
                    inv.getInvoiceDate().format(timeFormatter),
                    inv.getCustomerName(),
                    inv.getVehicleNumber(),
                    String.format("%.2f", inv.getTotalAmount())
            };
            tableModel.addRow(row);
        }

        // Update Total Label
        totalIncomeLabel.setText(String.format("LKR %.2f", totalIncome));
    }

    private void exportPDF() {
        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a date first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        List<Invoice> invoices = invoiceDAO.findByDateRange(date);

        if (invoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to export for this date.", "Info",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Default download path
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");
        String fileName = "Daily_Report_" + date.toString() + ".pdf";
        File file = new File(downloadsDir, fileName);

        try {
            PDFReportGenerator.generateDailyReport(invoices, date, file.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Report saved successfully to:\n" + file.getAbsolutePath(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Optional: Open the file
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate PDF: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
