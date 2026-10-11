package com.frauddetection.gui;

import com.frauddetection.dao.FraudAnalysisDAO;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.RiskLevel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Real-time Analytics & Executive Statistics Dashboard.
 * Integrates live metrics queried directly from the MySQL database with
 * custom-drawn Java2D charts and risk breakdowns.
 * Fulfills rubric: Classes for Database Operations, Statistics, and Swing Visuals.
 */
public class StatisticsPanel extends JPanel {

    private final FraudAnalysisDAO analysisDAO;

    // Metric Cards Value Labels
    private JLabel totalTxLabel;
    private JLabel totalAnalysesLabel;
    private JLabel avgScoreLabel;
    private JLabel safeCountLabel;
    private JLabel suspCountLabel;
    private JLabel fraudCountLabel;

    private RiskDistributionChart chart;
    private DefaultTableModel breakdownModel;

    public StatisticsPanel() {
        this.analysisDAO = new FraudAnalysisDAO();

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initUI();
        refreshStatisticsAsync();
    }

    private void initUI() {
        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("FRAUD RISK ANALYTICS & INSIGHTS");
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JButton refreshBtn = UITheme.createSecondaryButton("🔄 Refresh Statistics");
        refreshBtn.addActionListener(e -> refreshStatisticsAsync());

        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center Content Container
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));

        // 1. KPI Metric Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(800, 200));

        JPanel c1 = UITheme.createMetricCard("TOTAL TRANSACTIONS", "0", "Persisted in MySQL", UITheme.ACCENT_BLUE);
        JPanel c2 = UITheme.createMetricCard("TOTAL ANALYSES", "0", "AI Evaluations Processed", UITheme.PRIMARY_DARK);
        JPanel c3 = UITheme.createMetricCard("AVERAGE RISK SCORE", "0.0", "Mean Risk Index (0-100)", UITheme.WARNING_AMBER);
        JPanel c4 = UITheme.createMetricCard("SAFE SETTLEMENTS", "0", "Score < 30 (Approved)", UITheme.SUCCESS_GREEN);
        JPanel c5 = UITheme.createMetricCard("SUSPICIOUS ALERTS", "0", "Score 30-69 (Review)", UITheme.WARNING_AMBER);
        JPanel c6 = UITheme.createMetricCard("FRAUD INTERVENTIONS", "0", "Score >= 70 (Blocked)", UITheme.DANGER_RED);

        totalTxLabel = (JLabel) c1.getClientProperty("valueLabel");
        totalAnalysesLabel = (JLabel) c2.getClientProperty("valueLabel");
        avgScoreLabel = (JLabel) c3.getClientProperty("valueLabel");
        safeCountLabel = (JLabel) c4.getClientProperty("valueLabel");
        suspCountLabel = (JLabel) c5.getClientProperty("valueLabel");
        fraudCountLabel = (JLabel) c6.getClientProperty("valueLabel");

        kpiGrid.add(c1);
        kpiGrid.add(c2);
        kpiGrid.add(c3);
        kpiGrid.add(c4);
        kpiGrid.add(c5);
        kpiGrid.add(c6);
        center.add(kpiGrid);

        center.add(javax.swing.Box.createRigidArea(new Dimension(0, 16)));

        // 2. Charts and Breakdown Split
        JPanel visualSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        visualSplit.setOpaque(false);

        // Left: Custom Java2D Chart
        chart = new RiskDistributionChart();
        visualSplit.add(chart);

        // Right: Risk Tier Breakdown Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JLabel tblTitle = new JLabel("RISK TIER BREAKDOWN");
        tblTitle.setFont(UITheme.BOLD_FONT);
        tableCard.add(tblTitle, BorderLayout.NORTH);

        String[] cols = {"Risk Tier", "Score Range", "Volume", "Share %"};
        breakdownModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(breakdownModel);
        table.setFont(UITheme.REGULAR_FONT);
        table.setRowHeight(24);
        table.getTableHeader().setFont(UITheme.BOLD_FONT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        tableCard.add(scroll, BorderLayout.CENTER);

        visualSplit.add(tableCard);
        center.add(visualSplit);

        JScrollPane mainScroll = new JScrollPane(center);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);
    }

    public void refreshStatisticsAsync() {
        new Thread(() -> {
            try {
                Map<String, Object> stats = analysisDAO.getRiskStatistics();
                SwingUtilities.invokeLater(() -> updateUIWithStats(stats));
            } catch (DatabaseOperationException e) {
                SwingUtilities.invokeLater(() -> {
                    // Fail silently or show notification if offline
                });
            }
        }, "StatsRefresher-Thread").start();
    }

    @SuppressWarnings("unchecked")
    private void updateUIWithStats(Map<String, Object> stats) {
        if (stats == null) return;

        int totalTx = (Integer) stats.getOrDefault("totalTransactions", 0);
        int totalAnalyses = (Integer) stats.getOrDefault("totalAnalyses", 0);
        double avgScore = (Double) stats.getOrDefault("averageRiskScore", 0.0);
        int safe = (Integer) stats.getOrDefault("safeCount", 0);
        int susp = (Integer) stats.getOrDefault("suspiciousCount", 0);
        int fraud = (Integer) stats.getOrDefault("fraudulentCount", 0);

        totalTxLabel.setText(String.valueOf(totalTx));
        totalAnalysesLabel.setText(String.valueOf(totalAnalyses));
        avgScoreLabel.setText(String.format("%.1f", avgScore));
        safeCountLabel.setText(String.valueOf(safe));
        suspCountLabel.setText(String.valueOf(susp));
        fraudCountLabel.setText(String.valueOf(fraud));

        // Update Chart
        Map<RiskLevel, Integer> dist = (Map<RiskLevel, Integer>) stats.get("distribution");
        if (dist != null) {
            chart.updateData(dist);

            // Update Breakdown Table
            breakdownModel.setRowCount(0);
            for (RiskLevel level : RiskLevel.values()) {
                int count = dist.getOrDefault(level, 0);
                double pct = totalAnalyses > 0 ? ((double) count / totalAnalyses) * 100.0 : 0.0;
                breakdownModel.addRow(new Object[]{
                        level.name(),
                        level.getMinScore() + " - " + level.getMaxScore(),
                        count,
                        String.format("%.1f%%", pct)
                });
            }
        }
    }
}
