package com.frauddetection.gui;

import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import com.frauddetection.service.TransactionService;
import com.frauddetection.service.TransactionService.TransactionHistoryItem;
import com.frauddetection.util.DateTimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Transaction History Audit Panel.
 * Demonstrates Collections (List<TransactionHistoryItem>), JDBC Data Retrieval,
 * Dynamic filtering, and background loading.
 */
public class TransactionHistoryPanel extends JPanel {

    private final User currentUser;
    private final TransactionService transactionService;
    private final Runnable onDataChangedCallback;

    private JTextField searchField;
    private JComboBox<String> riskFilterCombo;
    private JComboBox<String> decisionFilterCombo;
    private DefaultTableModel tableModel;
    private JTable historyTable;
    private JLabel countLabel;

    private List<TransactionHistoryItem> cachedItems = new ArrayList<>();

    public TransactionHistoryPanel(User currentUser, TransactionService transactionService, Runnable onDataChangedCallback) {
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.onDataChangedCallback = onDataChangedCallback;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initUI();
        refreshHistoryAsync();
    }

    private void initUI() {
        // Top Toolbar: Search & Filters
        JPanel topPanel = UITheme.createCardPanel();
        topPanel.setLayout(new BorderLayout(10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        filterBar.add(new JLabel("Search:"));
        searchField = new JTextField(14);
        searchField.setFont(UITheme.REGULAR_FONT);
        searchField.addActionListener(e -> applyFilters());
        filterBar.add(searchField);

        filterBar.add(new JLabel("Risk Level:"));
        riskFilterCombo = new JComboBox<>(new String[]{"ALL", "LOW", "MEDIUM", "HIGH", "CRITICAL"});
        riskFilterCombo.addActionListener(e -> applyFilters());
        filterBar.add(riskFilterCombo);

        filterBar.add(new JLabel("Decision:"));
        decisionFilterCombo = new JComboBox<>(new String[]{"ALL", "SAFE", "SUSPICIOUS", "FRAUDULENT"});
        decisionFilterCombo.addActionListener(e -> applyFilters());
        filterBar.add(decisionFilterCombo);

        JButton applyBtn = UITheme.createPrimaryButton("Filter");
        applyBtn.addActionListener(e -> applyFilters());
        filterBar.add(applyBtn);

        JButton resetBtn = UITheme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> resetFilters());
        filterBar.add(resetBtn);

        JPanel actionBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnBar.setOpaque(false);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> refreshHistoryAsync());

        JButton viewBtn = UITheme.createPrimaryButton("👁 View Details");
        viewBtn.addActionListener(e -> viewSelectedDetails());

        JButton deleteBtn = UITheme.createDangerButton("🗑 Delete");
        deleteBtn.addActionListener(e -> deleteSelectedTransaction());

        actionBtnBar.add(refreshBtn);
        actionBtnBar.add(viewBtn);
        actionBtnBar.add(deleteBtn);

        topPanel.add(filterBar, BorderLayout.WEST);
        topPanel.add(actionBtnBar, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Table Container
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {"ID", "Txn Code", "Sender", "Receiver", "Amount ($)", "Risk Score", "Risk Tier", "Decision", "Timestamp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setFont(UITheme.REGULAR_FONT);
        historyTable.setRowHeight(26);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setFont(UITheme.BOLD_FONT);

        // Custom Cell Renderer for coloring Risk Tier & Decision
        historyTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setForeground(UITheme.TEXT_PRIMARY);
                    if (column == 6) { // Risk Tier
                        String tier = String.valueOf(value);
                        if ("LOW".equals(tier)) c.setForeground(UITheme.SUCCESS_GREEN);
                        else if ("MEDIUM".equals(tier)) c.setForeground(UITheme.WARNING_AMBER);
                        else if ("HIGH".equals(tier) || "CRITICAL".equals(tier)) c.setForeground(UITheme.DANGER_RED);
                    } else if (column == 7) { // Decision
                        String dec = String.valueOf(value);
                        if ("SAFE".equals(dec)) c.setForeground(UITheme.SUCCESS_GREEN);
                        else if ("SUSPICIOUS".equals(dec)) c.setForeground(UITheme.WARNING_AMBER);
                        else if ("FRAUDULENT".equals(dec)) c.setForeground(UITheme.DANGER_RED);
                    }
                }
                return c;
            }
        });

        // Column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(105);
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(null);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom status count
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setOpaque(false);
        countLabel = new JLabel("Loading transaction records from MySQL...");
        countLabel.setFont(UITheme.SMALL_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(countLabel);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshHistoryAsync() {
        new Thread(() -> {
            try {
                List<TransactionHistoryItem> items = transactionService.getCombinedHistory();
                SwingUtilities.invokeLater(() -> {
                    cachedItems = items;
                    applyFilters();
                });
            } catch (DatabaseOperationException e) {
                SwingUtilities.invokeLater(() -> {
                    countLabel.setText("Database error: " + e.getMessage());
                });
            }
        }, "HistoryLoader-Thread").start();
    }

    private void applyFilters() {
        String keyword = searchField.getText();
        String risk = (String) riskFilterCombo.getSelectedItem();
        String decision = (String) decisionFilterCombo.getSelectedItem();

        List<TransactionHistoryItem> filtered = transactionService.filterHistory(cachedItems, keyword, risk, decision);

        tableModel.setRowCount(0);
        for (TransactionHistoryItem item : filtered) {
            Transaction tx = item.getTransaction();
            FraudAnalysis fa = item.getAnalysis();

            tableModel.addRow(new Object[]{
                    tx.getId(),
                    tx.getTransactionId(),
                    tx.getSenderName(),
                    tx.getReceiverName(),
                    String.format("$%,.2f", tx.getAmount()),
                    fa != null ? (fa.getRiskScore() + "/100") : "N/A",
                    fa != null ? fa.getRiskLevel().name() : "N/A",
                    fa != null ? fa.getDecision().name() : "N/A",
                    DateTimeUtil.format(tx.getCreatedAt())
            });
        }
        countLabel.setText("Displaying " + filtered.size() + " of " + cachedItems.size() + " transactions from MySQL database.");
    }

    private void resetFilters() {
        searchField.setText("");
        riskFilterCombo.setSelectedIndex(0);
        decisionFilterCombo.setSelectedIndex(0);
        applyFilters();
    }

    private void viewSelectedDetails() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to inspect.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int txId = (Integer) tableModel.getValueAt(row, 0);
        openDetailsDialog(txId);
    }

    private void deleteSelectedTransaction() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int txId = (Integer) tableModel.getValueAt(row, 0);
        String code = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete transaction " + code + "?\nThis will also remove its associated fraud analysis.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = transactionService.deleteTransaction(txId, currentUser.getUsername());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Transaction " + code + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    refreshHistoryAsync();
                    if (onDataChangedCallback != null) {
                        onDataChangedCallback.run();
                    }
                }
            } catch (DatabaseOperationException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDetailsDialog(int txId) {
        try {
            var txOpt = transactionService.findById(txId);
            var faOpt = transactionService.findAnalysisForTransaction(txId);
            if (txOpt.isPresent()) {
                FraudDetailsPanel detailsDialog = new FraudDetailsPanel(
                        (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this),
                        txOpt.get(),
                        faOpt.orElse(null)
                );
                detailsDialog.setVisible(true);
            }
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this, "Error fetching transaction details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
