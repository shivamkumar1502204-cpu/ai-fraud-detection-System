package com.frauddetection.gui;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.model.User;
import com.frauddetection.service.FraudAnalysisService;
import com.frauddetection.service.TransactionService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * Main application window and unified workspace.
 * Integrates dashboard statistics, live transaction evaluation, historical audit tables,
 * compliance logs, and university rubric documentation.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private final FraudAnalysisService analysisService;
    private final TransactionService transactionService;

    private StatisticsPanel statisticsPanel;
    private TransactionAnalysisPanel analysisPanel;
    private TransactionHistoryPanel historyPanel;
    private AuditLogPanel auditLogPanel;
    private AboutPanel aboutPanel;
    private JLabel dbStatusLabel;

    public DashboardFrame(User currentUser) {
        this.currentUser = currentUser;
        this.analysisService = new FraudAnalysisService();
        this.transactionService = new TransactionService();

        initUI();
    }

    private void initUI() {
        setTitle("AI Powered Fraud Detection System - Enterprise Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.APP_BG);
        setLayout(new BorderLayout());

        // Top Navigation Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UITheme.PRIMARY_DARK);
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        // Brand & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🛡️");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel("AI FRAUD DETECTION SYSTEM");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel badge = new JLabel(" v1.0 ");
        badge.setFont(UITheme.SMALL_FONT);
        badge.setOpaque(true);
        badge.setBackground(UITheme.ACCENT_BLUE);
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        brandPanel.add(logoLabel);
        brandPanel.add(titleLabel);
        brandPanel.add(badge);

        // User & DB Status & Logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        dbStatusLabel = new JLabel("● Checking DB...");
        dbStatusLabel.setFont(UITheme.SMALL_FONT);
        dbStatusLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel userLabel = new JLabel("Operator: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(UITheme.BOLD_FONT);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UITheme.createSecondaryButton("Sign Out");
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(dbStatusLabel);
        userPanel.add(userLabel);
        userPanel.add(logoutBtn);

        topHeader.add(brandPanel, BorderLayout.WEST);
        topHeader.add(userPanel, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        // Main Tabbed Workspace
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(UITheme.BOLD_FONT);
        tabbedPane.setBackground(UITheme.CARD_BG);

        // Instantiate Panels
        statisticsPanel = new StatisticsPanel();
        historyPanel = new TransactionHistoryPanel(currentUser, transactionService, this::onHistoryDataChanged);
        analysisPanel = new TransactionAnalysisPanel(currentUser, analysisService, this::onNewAnalysisCreated);
        auditLogPanel = new AuditLogPanel();
        aboutPanel = new AboutPanel();

        tabbedPane.addTab("📊 Dashboard & Statistics", statisticsPanel);
        tabbedPane.addTab("⚡ Analyze Transaction", analysisPanel);
        tabbedPane.addTab("📜 Transaction History", historyPanel);
        tabbedPane.addTab("📋 Security Audit Logs", auditLogPanel);
        tabbedPane.addTab("ℹ️ Rubric & Architecture", aboutPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Check DB Status
        checkDbStatus();
    }

    private void checkDbStatus() {
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.getInstance().testConnection();
            SwingUtilities.invokeLater(() -> {
                if (status.isConnected()) {
                    dbStatusLabel.setText("● MySQL Connected");
                    dbStatusLabel.setForeground(UITheme.SUCCESS_GREEN);
                } else {
                    dbStatusLabel.setText("● MySQL Offline");
                    dbStatusLabel.setForeground(UITheme.DANGER_RED);
                }
            });
        }).start();
    }

    /**
     * Cross-panel notification callback invoked when a new analysis is submitted and saved.
     */
    private void onNewAnalysisCreated() {
        statisticsPanel.refreshStatisticsAsync();
        historyPanel.refreshHistoryAsync();
        auditLogPanel.refreshLogsAsync();
    }

    /**
     * Cross-panel notification callback invoked when a transaction record is deleted.
     */
    private void onHistoryDataChanged() {
        statisticsPanel.refreshStatisticsAsync();
        auditLogPanel.refreshLogsAsync();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }
}
