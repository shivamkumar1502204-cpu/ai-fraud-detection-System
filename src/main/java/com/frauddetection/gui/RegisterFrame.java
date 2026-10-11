package com.frauddetection.gui;

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Dialog frame for registering a new user in the fraud detection system.
 */
public class RegisterFrame extends JDialog {

    private final UserService userService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;

    public RegisterFrame(JFrame parent) {
        super(parent, "Register New User", true);
        this.userService = new UserService();
        initUI();
    }

    private void initUI() {
        setSize(420, 440);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.APP_BG);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        header.setBackground(UITheme.PRIMARY_DARK);
        JLabel title = new JLabel("CREATE COMPLIANCE ACCOUNT");
        title.setFont(UITheme.SUBTITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Form Card
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(UITheme.APP_BG);

        JPanel card = UITheme.createCardPanel();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(360, 290));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(uLbl, gbc);

        usernameField = new JTextField(16);
        gbc.gridy = 1;
        card.add(usernameField, gbc);

        // Password
        JLabel pLbl = new JLabel("Password (min 6 chars):");
        pLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridy = 2;
        card.add(pLbl, gbc);

        passwordField = new JPasswordField(16);
        gbc.gridy = 3;
        card.add(passwordField, gbc);

        // Confirm Password
        JLabel cpLbl = new JLabel("Confirm Password:");
        cpLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridy = 4;
        card.add(cpLbl, gbc);

        confirmPasswordField = new JPasswordField(16);
        gbc.gridy = 5;
        card.add(confirmPasswordField, gbc);

        // Role
        JLabel rLbl = new JLabel("Assigned Role:");
        rLbl.setFont(UITheme.BOLD_FONT);
        gbc.gridy = 6;
        card.add(rLbl, gbc);

        roleCombo = new JComboBox<>(new String[]{"USER", "ADMIN"});
        gbc.gridy = 7;
        card.add(roleCombo, gbc);

        center.add(card);
        add(center, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        btnPanel.setBackground(UITheme.APP_BG);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton submitBtn = UITheme.createPrimaryButton("Create Account");
        submitBtn.addActionListener(e -> performRegistration());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void performRegistration() {
        String username = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        String confirmPass = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();

        if (!pass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User newUser = userService.register(username, pass, role);
            JOptionPane.showMessageDialog(this,
                    "Account '" + newUser.getUsername() + "' created successfully with role: " + newUser.getRole(),
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (DatabaseOperationException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
