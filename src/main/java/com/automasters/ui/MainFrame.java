package com.automasters.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private InvoicePanel invoicePanel;
    private HistoryPanel historyPanel;

    public MainFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("AutoMasters - Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        // Main container
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(245, 247, 250));

        // Sidebar
        JPanel sidebar = createSidebar();
        container.add(sidebar, BorderLayout.WEST);

        // Main content area with CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(245, 247, 250));

        invoicePanel = new InvoicePanel();
        historyPanel = new HistoryPanel();

        mainPanel.add(invoicePanel, "INVOICE");
        mainPanel.add(historyPanel, "HISTORY");

        container.add(mainPanel, BorderLayout.CENTER);

        add(container);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(30, 41, 59));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Logo/Title
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(30, 41, 59));
        logoPanel.setMaximumSize(new Dimension(220, 100));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel titleLabel = new JLabel("AutoMasters");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Billing System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(titleLabel);
        logoPanel.add(Box.createVerticalStrut(5));
        logoPanel.add(subtitleLabel);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(20));

        // Navigation buttons
        JButton invoiceBtn = createNavButton("+ New Invoice", true);
        JButton historyBtn = createNavButton("⌕ Vehicle History", false);

        invoiceBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "INVOICE");
            updateButtonStyles(invoiceBtn, historyBtn);
        });

        historyBtn.addActionListener(e -> {
            cardLayout.show(mainPanel, "HISTORY");
            updateButtonStyles(historyBtn, invoiceBtn);
        });

        sidebar.add(invoiceBtn);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(historyBtn);

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JLabel footerLabel = new JLabel("© 2026 AutoMasters");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(100, 116, 139));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.add(footerLabel);

        return sidebar;
    }

    private JButton createNavButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(isActive ? new Color(59, 130, 246) : new Color(30, 41, 59));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(220, 45));
        button.setPreferredSize(new Dimension(220, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(59, 130, 246))) {
                    button.setBackground(new Color(51, 65, 85));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(59, 130, 246))) {
                    button.setBackground(new Color(30, 41, 59));
                }
            }
        });

        return button;
    }

    private void updateButtonStyles(JButton active, JButton inactive) {
        active.setBackground(new Color(59, 130, 246));
        inactive.setBackground(new Color(30, 41, 59));
    }
}
