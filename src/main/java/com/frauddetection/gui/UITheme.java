package com.frauddetection.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Styling and Design System utility for the Swing GUI.
 * Provides a clean, modern aesthetic with consistent color palettes,
 * typography, and rounded card containers.
 */
public final class UITheme {

    // Color Palette
    public static final Color PRIMARY_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color SIDEBAR_BG = new Color(30, 41, 59);        // Slate 800
    public static final Color APP_BG = new Color(248, 250, 252);         // Slate 50
    public static final Color CARD_BG = new Color(255, 255, 255);        // Pure White
    public static final Color BORDER_COLOR = new Color(226, 232, 240);    // Slate 200
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // Slate 500
    public static final Color ACCENT_BLUE = new Color(37, 99, 235);      // Blue 600
    public static final Color ACCENT_HOVER = new Color(29, 78, 216);     // Blue 700
    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);   // Emerald 500
    public static final Color WARNING_AMBER = new Color(245, 158, 11);   // Amber 500
    public static final Color DANGER_RED = new Color(239, 68, 68);       // Red 500

    // Typography
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {
    }

    /**
     * Creates a styled modern action button.
     */
    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(BOLD_FONT);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, ACCENT_BLUE, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS_GREEN, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER_RED, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(REGULAR_FONT);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a styled card container panel with rounded borders and padding.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return card;
    }

    /**
     * Creates a KPI metric card panel displaying a key performance indicator.
     */
    public static JPanel createMetricCard(String title, String value, String subtext, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                // Accent top bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 4, 12, 12);

                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        card.setPreferredSize(new Dimension(170, 95));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(SMALL_FONT);
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtext);
        subLbl.setFont(SMALL_FONT);
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl);
        card.add(javax.swing.Box.createRigidArea(new Dimension(0, 4)));
        card.add(valLbl);
        card.add(javax.swing.Box.createRigidArea(new Dimension(0, 2)));
        card.add(subLbl);

        // Store value label in client property for dynamic updates
        card.putClientProperty("valueLabel", valLbl);
        card.putClientProperty("subLabel", subLbl);

        return card;
    }
}
