package com.frauddetection.gui;

import com.frauddetection.dao.AuditLogDAO;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.AuditLog;
import com.frauddetection.util.DateTimeUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Compliance Audit Trail Panel.
 * Displays immutable security logs recorded in the audit_logs database table.
 */
public class AuditLogPanel extends JPanel {

    private final AuditLogDAO auditLogDAO;
    private DefaultTableModel model;
    private JTable table;
    private JLabel statusLabel;

    public AuditLogPanel() {
        this.auditLogDAO = new AuditLogDAO();

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initUI();
        refreshLogsAsync();
    }

    private void initUI() {
        // Top Toolbar
        JPanel top = UITheme.createCardPanel();
        top.setLayout(new BorderLayout());

        JLabel title = new JLabel("SECURITY & REGULATORY AUDIT LOGS");
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Logs");
        refreshBtn.addActionListener(e -> refreshLogsAsync());

        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center Table Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"Log ID", "Username", "Action", "Details", "Timestamp"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(UITheme.REGULAR_FONT);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(UITheme.BOLD_FONT);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(450);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        card.add(scroll, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading audit logs from MySQL...");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        card.add(statusLabel, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    public void refreshLogsAsync() {
        new Thread(() -> {
            try {
                List<AuditLog> logs = auditLogDAO.findRecentLogs(150);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    for (AuditLog l : logs) {
                        model.addRow(new Object[]{
                                l.getId(),
                                l.getUsername(),
                                l.getAction(),
                                l.getDetails(),
                                DateTimeUtil.format(l.getTimestamp())
                        });
                    }
                    statusLabel.setText("Showing latest " + logs.size() + " immutable compliance log records.");
                });
            } catch (DatabaseOperationException e) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to load audit logs: " + e.getMessage()));
            }
        }, "AuditLogLoader-Thread").start();
    }
}
