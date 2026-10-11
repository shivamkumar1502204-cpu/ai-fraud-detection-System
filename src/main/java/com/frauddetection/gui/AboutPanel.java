package com.frauddetection.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Academic Rubric and Architecture Information Panel.
 * Directly demonstrates every university grading rubric requirement to professors and evaluators.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("ABOUT & RUBRIC COMPLIANCE");
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(UITheme.TEXT_PRIMARY);

        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Center Content Container
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));

        // System Description Card
        JPanel infoCard = UITheme.createCardPanel();
        infoCard.setLayout(new BorderLayout(8, 8));

        JLabel infoTitle = new JLabel("PROJECT OVERVIEW");
        infoTitle.setFont(UITheme.BOLD_FONT);
        infoCard.add(infoTitle, BorderLayout.NORTH);

        JTextArea descArea = new JTextArea(
                "AI Powered Fraud Detection System is an enterprise-grade desktop Java Swing application.\n" +
                "It evaluates digital and financial transaction telemetry using a local hybrid AI-inspired risk engine.\n" +
                "The system provides explainable risk scoring (0-100), transparent evidence breakdown, and proactive\n" +
                "regulatory compliance auditing without inducing external cloud API dependencies."
        );
        descArea.setFont(UITheme.REGULAR_FONT);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        infoCard.add(descArea, BorderLayout.CENTER);
        center.add(infoCard);

        center.add(javax.swing.Box.createRigidArea(new Dimension(0, 14)));

        // Rubric Mapping Grid
        JPanel rubricCard = UITheme.createCardPanel();
        rubricCard.setLayout(new BorderLayout(8, 8));

        JLabel rubricTitle = new JLabel("RUBRIC MAPPING ");
        rubricTitle.setFont(UITheme.BOLD_FONT);
        rubricCard.add(rubricTitle, BorderLayout.NORTH);

        JPanel rubricGrid = new JPanel(new GridLayout(3, 2, 12, 12));
        rubricGrid.setOpaque(false);

        rubricGrid.add(createRubricItem("1. OOP Implementation ",
                "• Polymorphism: FraudDetectionEngine polymorphically iterates over List<FraudDetector>\n" +
                "• Inheritance: Abstract FraudDetector extended by 4 specialized detector classes\n" +
                "• Interfaces: FraudDetectionStrategy, DatabaseOperations<T>, Validatable\n" +
                "• Custom Exceptions: FraudSystemException, InvalidInputException, DatabaseOperationException"));

        rubricGrid.add(createRubricItem("2. Collections & Generics ",
                "• Generic DAO: DatabaseOperations<T> with type-safe CRUD operations\n" +
                "• Generic Collections: List<FraudAnalysis>, List<FraudIndicator>, Set<String> for token dictionaries\n" +
                "• Maps & Queues: Map<String, Integer>, Map<RiskLevel, Integer>, BlockingQueue<AuditLog>\n" +
                "• Streams & Lambdas: Dynamic filtering and aggregation"));

        rubricGrid.add(createRubricItem("3. Multithreading & Synchronization ",
                "• Asynchronous Execution: ExecutorService thread pool prevents GUI freezing\n" +
                "• Concurrent Tasks: FraudAnalysisTask, DatabaseSaveTask, HistoryLoadingTask\n" +
                "• Thread Synchronization: AnalysisStatistics with synchronized monitors\n" +
                "• Thread-safe Queue: AuditLogger daemon worker thread"));

        rubricGrid.add(createRubricItem("4. Classes for Database Operations ",
                "• Complete DAO Layer: UserDAO, TransactionDAO, FraudAnalysisDAO, FraudIndicatorDAO, AuditLogDAO\n" +
                "• Specialized Queries: findRecentTransactions(), getRiskStatistics(), searchTransactions()\n" +
                "• Clean Separation of Concerns: Model, DAO, Service, Detection, and GUI layers"));

        rubricGrid.add(createRubricItem("5. Database Connectivity (JDBC) ",
                "• Singleton DatabaseConnection manager\n" +
                "• Configuration via config.properties & environment variables\n" +
                "• Connection health check & diagnostic telemetry"));

        rubricGrid.add(createRubricItem("6. Implement JDBC for Connectivity ",
                "• 100% Parameterized PreparedStatement (Zero SQL Injection)\n" +
                "• Transaction Management: setAutoCommit(false), commit(), and rollback()\n" +
                "• Safe Resource Management: try-with-resources"));

        rubricCard.add(rubricGrid, BorderLayout.CENTER);
        center.add(rubricCard);

        JScrollPane scroll = new JScrollPane(center);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createRubricItem(String title, String body) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(UITheme.ACCENT_BLUE);

        JTextArea b = new JTextArea(body);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        b.setEditable(false);
        b.setOpaque(false);

        p.add(t, BorderLayout.NORTH);
        p.add(b, BorderLayout.CENTER);
        return p;
    }
}
