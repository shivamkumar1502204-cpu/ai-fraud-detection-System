package com.frauddetection.gui;

import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import com.frauddetection.model.User;
import com.frauddetection.service.FraudAnalysisService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Transaction Analysis Input and Live AI Evaluation Screen.
 * Demonstrates Multithreading (asynchronous background evaluation without freezing UI),
 * Pre-set test scenario population, and explainable AI presentation.
 */
public class TransactionAnalysisPanel extends JPanel {

    private final User currentUser;
    private final FraudAnalysisService analysisService;
    private final Runnable onAnalysisCompleteCallback;

    // Form inputs
    private JTextField txIdField;
    private JTextField senderField;
    private JTextField receiverField;
    private JTextField amountField;
    private JTextField prevAmountField;
    private JComboBox<String> typeCombo;
    private JTextField locationField;
    private JTextField deviceField;
    private JSpinner freqSpinner;
    private JComboBox<String> newDeviceCombo;
    private JComboBox<String> unusualLocationCombo;
    private JTextArea suspiciousTextArea;
    private JTextField urlField;

    // Async UI components
    private JButton analyzeBtn;
    private JProgressBar progressBar;
    private JLabel progressStatusLabel;

    // Result components
    private JPanel resultCard;
    private JLabel scoreLabel;
    private JLabel levelBadge;
    private JLabel decisionBadge;
    private JTextArea recommendationArea;
    private DefaultTableModel indicatorsModel;
    private JTable indicatorsTable;

    public TransactionAnalysisPanel(User currentUser, FraudAnalysisService analysisService, Runnable onAnalysisCompleteCallback) {
        this.currentUser = currentUser;
        this.analysisService = analysisService;
        this.onAnalysisCompleteCallback = onAnalysisCompleteCallback;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initUI();
    }

    private void initUI() {
        // Top Toolbar: Scenario Presets & Auto Generator
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setOpaque(false);

        JLabel headerTitle = new JLabel("ANALYZE FINANCIAL TRANSACTION");
        headerTitle.setFont(UITheme.SUBTITLE_FONT);
        headerTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel presetBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        presetBar.setOpaque(false);

        JButton safePreset = new JButton("Load Safe Test");
        safePreset.setFont(UITheme.SMALL_FONT);
        safePreset.addActionListener(e -> loadSafePreset());

        JButton suspPreset = new JButton("Load Suspicious Test");
        suspPreset.setFont(UITheme.SMALL_FONT);
        suspPreset.addActionListener(e -> loadSuspiciousPreset());

        JButton fraudPreset = new JButton("Load Fraud Test");
        fraudPreset.setFont(UITheme.SMALL_FONT);
        fraudPreset.addActionListener(e -> loadFraudulentPreset());

        JButton clearBtn = new JButton("Clear Form");
        clearBtn.setFont(UITheme.SMALL_FONT);
        clearBtn.addActionListener(e -> clearForm());

        presetBar.add(new JLabel("Viva Presets:"));
        presetBar.add(safePreset);
        presetBar.add(suspPreset);
        presetBar.add(fraudPreset);
        presetBar.add(clearBtn);

        topToolbar.add(headerTitle, BorderLayout.WEST);
        topToolbar.add(presetBar, BorderLayout.EAST);
        add(topToolbar, BorderLayout.NORTH);

        // Center Split: Left = Input Form, Right = Live AI Results
        JPanel splitPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        splitPanel.setOpaque(false);

        // Left Form Container (Scrollable)
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout());
        JPanel formInner = createFormFieldsPanel();
        JScrollPane formScroll = new JScrollPane(formInner);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formCard.add(formScroll, BorderLayout.CENTER);

        // Right Result Container
        resultCard = createResultPanel();

        splitPanel.add(formCard);
        splitPanel.add(resultCard);
        add(splitPanel, BorderLayout.CENTER);

        // Pre-fill initial safe sample
        loadSafePreset();
    }

    private JPanel createFormFieldsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Transaction ID
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        panel.add(new JLabel("Transaction ID:"), g);
        txIdField = new JTextField("TXN-" + (10000 + new Random().nextInt(90000)));
        g.gridx = 1; panel.add(txIdField, g);

        // Sender
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Sender Name:"), g);
        senderField = new JTextField("Alice Walker");
        g.gridx = 1; panel.add(senderField, g);

        // Receiver
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Receiver Name:"), g);
        receiverField = new JTextField("Target Superstore");
        g.gridx = 1; panel.add(receiverField, g);

        // Amount & Previous Amount
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Amount ($):"), g);
        amountField = new JTextField("48.50");
        g.gridx = 1; panel.add(amountField, g);

        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Prev Baseline Amount ($):"), g);
        prevAmountField = new JTextField("55.00");
        g.gridx = 1; panel.add(prevAmountField, g);

        // Transaction Type
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Transaction Type:"), g);
        typeCombo = new JComboBox<>(new String[]{
                "CARD_PAYMENT", "P2P_TRANSFER", "ONLINE_PAYMENT",
                "CRYPTO_PURCHASE", "INTERNATIONAL_WIRE"
        });
        g.gridx = 1; panel.add(typeCombo, g);

        // Location & Device
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Location / City:"), g);
        locationField = new JTextField("Boston, MA, USA");
        g.gridx = 1; panel.add(locationField, g);

        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Device Fingerprint:"), g);
        deviceField = new JTextField("Apple iPhone 15 Pro");
        g.gridx = 1; panel.add(deviceField, g);

        // Frequency
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Velocity (txns/hr):"), g);
        freqSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        g.gridx = 1; panel.add(freqSpinner, g);

        // Anomaly Flags
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("New Device?"), g);
        newDeviceCombo = new JComboBox<>(new String[]{"NO", "YES"});
        g.gridx = 1; panel.add(newDeviceCombo, g);

        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Unusual Location?"), g);
        unusualLocationCombo = new JComboBox<>(new String[]{"NO", "YES"});
        g.gridx = 1; panel.add(unusualLocationCombo, g);

        // Suspicious Message
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Suspicious Text:"), g);
        suspiciousTextArea = new JTextArea(3, 16);
        suspiciousTextArea.setLineWrap(true);
        suspiciousTextArea.setWrapStyleWord(true);
        g.gridx = 1; panel.add(new JScrollPane(suspiciousTextArea), g);

        // URL
        row++;
        g.gridx = 0; g.gridy = row;
        panel.add(new JLabel("Context URL:"), g);
        urlField = new JTextField("https://target.com/checkout");
        g.gridx = 1; panel.add(urlField, g);

        // Progress Bar & Action Button
        row++;
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panel.add(progressBar, g);

        row++;
        progressStatusLabel = new JLabel("Ready for analysis", SwingConstants.CENTER);
        progressStatusLabel.setFont(UITheme.SMALL_FONT);
        progressStatusLabel.setForeground(UITheme.TEXT_MUTED);
        panel.add(progressStatusLabel, g);

        row++;
        analyzeBtn = UITheme.createPrimaryButton("⚡ Analyze Transaction");
        analyzeBtn.setPreferredSize(new Dimension(200, 40));
        analyzeBtn.addActionListener(e -> executeAnalysisAsync());
        panel.add(analyzeBtn, g);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Result Header
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT));
        head.setOpaque(false);
        JLabel title = new JLabel("FRAUD RISK ASSESSMENT RESULT");
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);
        head.add(title);
        card.add(head, BorderLayout.NORTH);

        // Center Content
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new javax.swing.BoxLayout(body, javax.swing.BoxLayout.Y_AXIS));

        // Score summary box
        JPanel scoreBox = new JPanel(new GridLayout(1, 3, 10, 0));
        scoreBox.setOpaque(false);
        scoreBox.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        JPanel p1 = UITheme.createMetricCard("RISK SCORE", "0 / 100", "Composite Index", UITheme.SUCCESS_GREEN);
        JPanel p2 = UITheme.createMetricCard("RISK TIER", "LOW", "Standard Baseline", UITheme.SUCCESS_GREEN);
        JPanel p3 = UITheme.createMetricCard("DECISION", "SAFE", "Triage Clearance", UITheme.SUCCESS_GREEN);

        scoreLabel = (JLabel) p1.getClientProperty("valueLabel");
        levelBadge = (JLabel) p2.getClientProperty("valueLabel");
        decisionBadge = (JLabel) p3.getClientProperty("valueLabel");

        scoreBox.add(p1);
        scoreBox.add(p2);
        scoreBox.add(p3);
        body.add(scoreBox);

        // Recommendation Box
        JPanel recPanel = new JPanel(new BorderLayout());
        recPanel.setOpaque(false);
        recPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR), "System Recommendation"));

        recommendationArea = new JTextArea("Submit a transaction to evaluate risk score.");
        recommendationArea.setFont(UITheme.REGULAR_FONT);
        recommendationArea.setLineWrap(true);
        recommendationArea.setWrapStyleWord(true);
        recommendationArea.setEditable(false);
        recommendationArea.setOpaque(false);
        recommendationArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        recPanel.add(recommendationArea, BorderLayout.CENTER);
        body.add(recPanel);

        body.add(javax.swing.Box.createRigidArea(new Dimension(0, 10)));

        // Detected Indicators Table
        JLabel indTitle = new JLabel("Detected Explainable Indicators:");
        indTitle.setFont(UITheme.BOLD_FONT);
        body.add(indTitle);
        body.add(javax.swing.Box.createRigidArea(new Dimension(0, 6)));

        String[] cols = {"Type", "Indicator Description", "Severity"};
        indicatorsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        indicatorsTable = new JTable(indicatorsModel);
        indicatorsTable.setFont(UITheme.REGULAR_FONT);
        indicatorsTable.setRowHeight(22);
        indicatorsTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        indicatorsTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        indicatorsTable.getColumnModel().getColumn(2).setPreferredWidth(70);

        JScrollPane indScroll = new JScrollPane(indicatorsTable);
        indScroll.setPreferredSize(new Dimension(420, 160));
        body.add(indScroll);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void executeAnalysisAsync() {
        // Collect form data
        String txId = txIdField.getText().trim();
        String sender = senderField.getText().trim();
        String receiver = receiverField.getText().trim();
        String amountStr = amountField.getText().trim();
        String prevAmountStr = prevAmountField.getText().trim();
        String txType = (String) typeCombo.getSelectedItem();
        String location = locationField.getText().trim();
        String device = deviceField.getText().trim();
        int freq = (Integer) freqSpinner.getValue();
        boolean newDev = "YES".equalsIgnoreCase((String) newDeviceCombo.getSelectedItem());
        boolean unusualLoc = "YES".equalsIgnoreCase((String) unusualLocationCombo.getSelectedItem());
        String suspText = suspiciousTextArea.getText().trim();
        String url = urlField.getText().trim();

        double amount;
        double prevAmount = 0.0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (!prevAmountStr.isEmpty()) {
                prevAmount = Double.parseDouble(prevAmountStr);
            }
        } catch (NumberFormatException ignored) {}

        Transaction tx = new Transaction(0, txId, sender, receiver, amount, txType, location, device,
                freq, newDev, unusualLoc, suspText, url, prevAmount, LocalDateTime.now());

        // Prepare Async UI State
        analyzeBtn.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressStatusLabel.setText("Running background heuristic engines...");

        // Multithreaded Execution via FraudAnalysisService
        analysisService.analyzeAndPersistAsync(
                tx,
                progress -> {
                    progressBar.setValue(progress);
                    if (progress < 50) {
                        progressStatusLabel.setText("Extracting URL & text features (" + progress + "%)...");
                    } else if (progress < 85) {
                        progressStatusLabel.setText("Synthesizing hybrid risk score (" + progress + "%)...");
                    } else {
                        progressStatusLabel.setText("Persisting results to MySQL (" + progress + "%)...");
                    }
                },
                analysis -> {
                    // Success Callback on EDT
                    displayAnalysisResult(analysis);
                    progressBar.setVisible(false);
                    progressStatusLabel.setText("✓ Analysis complete & saved to MySQL database!");
                    analyzeBtn.setEnabled(true);

                    // Notify parent to refresh history & stats
                    if (onAnalysisCompleteCallback != null) {
                        onAnalysisCompleteCallback.run();
                    }
                },
                error -> {
                    // Error Callback on EDT
                    progressBar.setVisible(false);
                    progressStatusLabel.setText("⚠ Analysis encountered an error.");
                    analyzeBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                            "Analysis Error: " + error.getMessage(),
                            "Evaluation Exception", JOptionPane.ERROR_MESSAGE);
                },
                currentUser.getUsername()
        );
    }

    private void displayAnalysisResult(FraudAnalysis analysis) {
        scoreLabel.setText(analysis.getRiskScore() + " / 100");
        levelBadge.setText(analysis.getRiskLevel().name());
        decisionBadge.setText(analysis.getDecision().name());

        // Color badge updates
        scoreLabel.setForeground(analysis.getRiskLevel().getColor());
        levelBadge.setForeground(analysis.getRiskLevel().getColor());
        decisionBadge.setForeground(analysis.getDecision().getColor());

        recommendationArea.setText(analysis.getRecommendation());

        // Update Indicators Table
        indicatorsModel.setRowCount(0);
        if (analysis.getIndicators().isEmpty()) {
            indicatorsModel.addRow(new Object[]{"BENIGN_TELEMETRY", "No anomalous risk factors identified", "0.0"});
        } else {
            for (FraudIndicator ind : analysis.getIndicators()) {
                indicatorsModel.addRow(new Object[]{
                        ind.getIndicatorType(),
                        ind.getIndicatorDescription(),
                        String.format("+%.1f", ind.getSeverityWeight())
                });
            }
        }
    }

    private void loadSafePreset() {
        txIdField.setText("TXN-SAFE-" + (100 + new Random().nextInt(900)));
        senderField.setText("Sarah Jenkins");
        receiverField.setText("Starbucks Coffee #402");
        amountField.setText("18.75");
        prevAmountField.setText("22.50");
        typeCombo.setSelectedItem("CARD_PAYMENT");
        locationField.setText("Austin, TX, USA");
        deviceField.setText("Samsung Galaxy S23 (Known)");
        freqSpinner.setValue(1);
        newDeviceCombo.setSelectedItem("NO");
        unusualLocationCombo.setSelectedItem("NO");
        suspiciousTextArea.setText("Morning coffee & pastry purchase on registered card.");
        urlField.setText("https://starbucks.com/pay");
    }

    private void loadSuspiciousPreset() {
        txIdField.setText("TXN-SUSP-" + (100 + new Random().nextInt(900)));
        senderField.setText("Marcus Vance");
        receiverField.setText("Dubai Premier Electronics");
        amountField.setText("4950.00");
        prevAmountField.setText("420.00");
        typeCombo.setSelectedItem("INTERNATIONAL_WIRE");
        locationField.setText("Dubai, UAE");
        deviceField.setText("MacBook Pro 14");
        freqSpinner.setValue(4);
        newDeviceCombo.setSelectedItem("YES");
        unusualLocationCombo.setSelectedItem("YES");
        suspiciousTextArea.setText("High value international order for luxury electronics clearance.");
        urlField.setText("http://dubai-premier-gadgets.net/checkout");
    }

    private void loadFraudulentPreset() {
        txIdField.setText("TXN-FRAUD-" + (100 + new Random().nextInt(900)));
        senderField.setText("Arthur Pendelton");
        receiverField.setText("Crypto Cold Storage 0x3F");
        amountField.setText("24800.00");
        prevAmountField.setText("150.00");
        typeCombo.setSelectedItem("CRYPTO_PURCHASE");
        locationField.setText("Lagos, Nigeria");
        deviceField.setText("Rooted Virtual Linux Emulator");
        freqSpinner.setValue(14);
        newDeviceCombo.setSelectedItem("YES");
        unusualLocationCombo.setSelectedItem("YES");
        suspiciousTextArea.setText("URGENT: Your account is suspended! Send your OTP and password immediately to unlock funds or face permanent termination.");
        urlField.setText("http://192.168.1.105/paypal-secure-verify/account-update");
    }

    private void clearForm() {
        txIdField.setText("TXN-" + (10000 + new Random().nextInt(90000)));
        senderField.setText("");
        receiverField.setText("");
        amountField.setText("");
        prevAmountField.setText("0.00");
        typeCombo.setSelectedIndex(0);
        locationField.setText("");
        deviceField.setText("");
        freqSpinner.setValue(1);
        newDeviceCombo.setSelectedIndex(0);
        unusualLocationCombo.setSelectedIndex(0);
        suspiciousTextArea.setText("");
        urlField.setText("");
    }
}
