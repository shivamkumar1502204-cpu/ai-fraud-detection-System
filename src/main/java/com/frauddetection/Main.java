package com.frauddetection;

import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.gui.LoginFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point.
 * Initializes desktop Look and Feel, verifies database readiness,
 * and launches the authentication GUI.
 */
public class Main {

    public static void main(String[] args) {
        // Configure high-DPI and smooth font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Attempt modern FlatLaf Look and Feel with seamless native fallback
        try {
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLafClass.getMethod("setup").invoke(null);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Default Java Look and Feel
            }
        }

        // Diagnostic startup logging
        System.out.println("=================================================");
        System.out.println(" AI POWERED FRAUD DETECTION SYSTEM (v1.0.0)      ");
        System.out.println(" University Java GUI Project Presentation Build ");
        System.out.println("=================================================");

        // Test database connectivity asynchronously on launch
        new Thread(() -> {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.getInstance().testConnection();
            if (status.isConnected()) {
                System.out.println("[Main] MySQL Database Status: CONNECTED (" + status.getMessage() + ")");
            } else {
                System.out.println("[Main] MySQL Database Status: OFFLINE (" + status.getMessage() + ")");
                System.out.println("[Main] NOTE: Ensure MySQL is running on localhost:3306 and schema.sql is executed.");
            }
        }, "Startup-DB-Check").start();

        // Launch Login GUI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
