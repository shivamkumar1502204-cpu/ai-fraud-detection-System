package com.frauddetection.gui;

import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import com.frauddetection.util.DateTimeUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Deep-dive inspection dialog showing complete explainable fraud reasoning,
 * raw telemetry parameters, and algorithmic indicators for an individual transaction.
 */
public class FraudDetailsPanel extends JDialog {

    private final Transaction transaction;
    private final FraudAnalysis analysis;

    public FraudDetailsPanel(JFrame parent, Transaction transaction, FraudAnalysis analysis) {
        super(parent, "Transaction Details & AI Risk Audit", true);
        this.transaction = transaction;
        this.analysis = analysis;

        setSize(780, 680);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UITheme.APP_BG);
        setLayout(new BorderLayout(12, 12));

        initUI();
    }

    private void initUI() {
        // Top Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("RISK INSPECTION AUDIT: " + transaction.getTransactionId());
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Logged At: " + DateTimeUtil.format(transaction.getCreatedAt()));
        sub.setFont(UITheme.SMALL_FONT);
        sub.setForeground(UITheme.TEXT_MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(sub, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center Content (Scrollable)
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. AI Decision Summary Bar
        JPanel summaryCard = UITheme.createCardPanel();
        summaryCard.setLayout(new GridLayout(1, 3, 12, 0));

        int score = analysis != null ? analysis.getRiskScore() : 0;
        String level = analysis != null ? analysis.getRiskLevel().name() : "N/A";
        String decision = analysis != null ? analysis.getDecision().name() : "N/A";
        Color accent = analysis != null ? analysis.getRiskLevel().getColor() : UITheme.TEXT_MUTED;

        summaryCard.add(UITheme.createMetricCard("RISK SCORE", score + " / 100", "Composite AI Score", accent));
        summaryCard.add(UITheme.createMetricCard("RISK TIER", level, "Threat Classification", accent));
        summaryCard.add(UITheme.createMetricCard("DECISION", decision, "Recommended Action", accent));
        content.add(summaryCard);
        content.add(javax.swing.Box.createRigidArea(new Dimension(0, 12)));

        // 2. Transaction Telemetry Details Card
        JPanel metaCard = UITheme.createCardPanel();
        metaCard.setLayout(new GridBagLayout());
        metaCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR), "Transaction Telemetry & Context"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 10, 4, 10);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        addDetailRow(metaCard, g, 0, "Sender:", transaction.getSenderName(), "Receiver:", transaction.getReceiverName());
        addDetailRow(metaCard, g, 1, "Amount ($):", String.format("$%,.2f", transaction.getAmount()), "Prev Baseline ($):", String.format("$%,.2f", transaction.getPreviousAmount()));
        addDetailRow(metaCard, g, 2, "Payment Type:", transaction.getTransactionType(), "Velocity (txns/hr):", String.valueOf(transaction.getTransactionFrequency()));
        addDetailRow(metaCard, g, 3, "Location:", transaction.getLocation(), "Unusual Location?:", transaction.isUnusualLocation() ? "YES (Flagged)" : "No");
        addDetailRow(metaCard, g, 4, "Device:", transaction.getDevice(), "New Hardware?:", transaction.isNewDevice() ? "YES (Flagged)" : "No");
        addDetailRow(metaCard, g, 5, "URL Link:", transaction.getUrl() != null ? transaction.getUrl() : "None", "", "");

        // Suspicious text row
        g.gridx = 0; g.gridy = 6; g.gridwidth = 1;
        JLabel txtLbl = new JLabel("Attached Text:");
        txtLbl.setFont(UITheme.BOLD_FONT);
        metaCard.add(txtLbl, g);

        g.gridx = 1; g.gridwidth = 3;
        JTextArea txtArea = new JTextArea(transaction.getSuspiciousText() != null ? transaction.getSuspiciousText() : "None provided");
        txtArea.setEditable(false);
        txtArea.setOpaque(false);
        txtArea.setLineWrap(true);
        txtArea.setFont(UITheme.REGULAR_FONT);
        metaCard.add(txtArea, g);

        content.add(metaCard);
        content.add(javax.swing.Box.createRigidArea(new Dimension(0, 12)));

        // 3. Recommendation & Explanations Card
        JPanel recCard = UITheme.createCardPanel();
        recCard.setLayout(new BorderLayout(6, 6));
        recCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR), "System Recommendation & Action Policy"));

        JTextArea recArea = new JTextArea(analysis != null ? analysis.getRecommendation() : "No recommendation available.");
        recArea.setFont(UITheme.REGULAR_FONT);
        recArea.setLineWrap(true);
        recArea.setWrapStyleWord(true);
        recArea.setEditable(false);
        recArea.setOpaque(false);
        recCard.add(recArea, BorderLayout.CENTER);
        content.add(recCard);
        content.add(javax.swing.Box.createRigidArea(new Dimension(0, 12)));

        // 4. Detected Indicators Table
        JPanel indCard = UITheme.createCardPanel();
        indCard.setLayout(new BorderLayout(6, 6));
        indCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR), "Granular Explainable AI Indicators"));

        String[] cols = {"Indicator Type", "Description", "Severity Weight"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        if (analysis != null && !analysis.getIndicators().isEmpty()) {
            for (FraudIndicator ind : analysis.getIndicators()) {
                model.addRow(new Object[]{
                        ind.getIndicatorType(),
                        ind.getIndicatorDescription(),
                        String.format("+%.1f pts", ind.getSeverityWeight())
                });
            }
        } else {
            model.addRow(new Object[]{"BENIGN_TELEMETRY", "Telemetry conforms to nominal profile. No risk indicators triggered.", "0.0"});
        }

        JTable table = new JTable(model);
        table.setFont(UITheme.REGULAR_FONT);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(700, 120));
        indCard.add(scroll, BorderLayout.CENTER);
        content.add(indCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        footer.setBackground(UITheme.APP_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton closeBtn = UITheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addDetailRow(JPanel p, GridBagConstraints g, int row, String lbl1, String val1, String lbl2, String val2) {
        g.gridy = row;
        g.gridwidth = 1;

        g.gridx = 0;
        JLabel l1 = new JLabel(lbl1);
        l1.setFont(UITheme.BOLD_FONT);
        p.add(l1, g);

        g.gridx = 1;
        JLabel v1 = new JLabel(val1);
        v1.setFont(UITheme.REGULAR_FONT);
        p.add(v1, g);

        g.gridx = 2;
        JLabel l2 = new JLabel(lbl2);
        l2.setFont(UITheme.BOLD_FONT);
        p.add(l2, g);

        g.gridx = 3;
        JLabel v2 = new JLabel(val2);
        v2.setFont(UITheme.REGULAR_FONT);
        p.add(v2, g);
    }
}
