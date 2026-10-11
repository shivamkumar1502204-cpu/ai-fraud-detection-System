package com.frauddetection.gui;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.model.User;
import com.frauddetection.service.UserService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Authentication and Entry point GUI for the Fraud Detection System.
 */
public class LoginFrame extends JFrame {

    private final UserService userService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        this.userService = new UserService();
        initUI();
        checkDatabaseStatus();
    }

    private void initUI() {
        setTitle("AI Fraud Detection System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.APP_BG);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UITheme.PRIMARY_DARK);
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel titleLabel = new JLabel("AI FRAUD DETECTION SYSTEM");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Enterprise Risk Intelligence & Compliance Platform");
        subLabel.setFont(UITheme.SMALL_FONT);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        subLabel.setAlignmentX(CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(javax.swing.Box.createRigidArea(new Dimension(0, 6)));
        headerPanel.add(subLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center Card Panel
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(UITheme.APP_BG);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(390, 320));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DB Status Indicator
        statusLabel = new JLabel("Checking MySQL connection...", SwingConstants.CENTER);
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(statusLabel, gbc);

        // Username
        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        card.add(uLbl, gbc);

        usernameField = new JTextField("admin", 18);
        usernameField.setFont(UITheme.REGULAR_FONT);
        gbc.gridy = 2;
        card.add(usernameField, gbc);

        // Password
        JLabel pLbl = new JLabel("Password:");
        pLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridy = 3;
        card.add(pLbl, gbc);

        passwordField = new JPasswordField("admin123", 18);
        passwordField.setFont(UITheme.REGULAR_FONT);
        gbc.gridy = 4;
        card.add(passwordField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setOpaque(false);

        JButton loginBtn = UITheme.createPrimaryButton("Login to System");
        loginBtn.addActionListener(e -> performLogin());

        JButton regBtn = UITheme.createSecondaryButton("Register New User");
        regBtn.addActionListener(e -> openRegistration());

        btnPanel.add(loginBtn);
        btnPanel.add(regBtn);

        gbc.gridy = 5;
        card.add(btnPanel, gbc);

        // Preset Quick Fill Helpers (Helpful for university viva)
        JPanel helperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        helperPanel.setOpaque(false);

        JButton fillAdminBtn = new JButton("Admin Creds");
        fillAdminBtn.setFont(UITheme.SMALL_FONT);
        fillAdminBtn.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("admin123");
        });

        JButton fillOfficerBtn = new JButton("Officer Creds");
        fillOfficerBtn.setFont(UITheme.SMALL_FONT);
        fillOfficerBtn.addActionListener(e -> {
            usernameField.setText("officer");
            passwordField.setText("user123");
        });

        helperPanel.add(fillAdminBtn);
        helperPanel.add(fillOfficerBtn);

        gbc.gridy = 6;
        card.add(helperPanel, gbc);

        centerContainer.add(card);
        add(centerContainer, BorderLayout.CENTER);

        // Footer
        JLabel footerLabel = new JLabel("University Java GUI Project • Rubric Presentation Ready", SwingConstants.CENTER);
        footerLabel.setFont(UITheme.SMALL_FONT);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 12, 0));
        add(footerLabel, BorderLayout.SOUTH);

        // Enter key submits login
        getRootPane().setDefaultButton(loginBtn);
    }

    private void checkDatabaseStatus() {
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.getInstance().testConnection();
            SwingUtilities.invokeLater(() -> {
                if (status.isConnected()) {
                    statusLabel.setText("● " + status.getMessage());
                    statusLabel.setForeground(UITheme.SUCCESS_GREEN);
                } else {
                    statusLabel.setText("● MySQL Offline (Run schema.sql)");
                    statusLabel.setForeground(UITheme.DANGER_RED);
                }
            });
        }).start();
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = userService.authenticate(username, password);
            // Open Dashboard
            DashboardFrame dashboard = new DashboardFrame(user);
            dashboard.setVisible(true);
            dispose();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this,
                    "Database Connection Error:\n" + e.getMessage() +
                            "\n\nPlease verify MySQL is running and schema.sql has been executed.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegistration() {
        RegisterFrame regFrame = new RegisterFrame(this);
        regFrame.setVisible(true);
    }
}
