package com.frauddetection.detection;

import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Anomaly and Telemetry Deviation Detector.
 * Identifies hardware fingerprint anomalies, uncharacteristic geographic locations,
 * and correlation risk multipliers.
 * Fulfills OOP Rubric: Inheritance.
 */
public class AnomalyFraudDetector extends FraudDetector {

    public AnomalyFraudDetector() {
        super("Device & Geolocation Anomaly Detector", 0.20); // 20% ensemble weight
    }

    @Override
    public double calculateRisk(Transaction transaction) {
        if (transaction == null) {
            return 0.0;
        }

        double riskScore = 0.0;
        boolean newDevice = transaction.isNewDevice();
        boolean unusualLocation = transaction.isUnusualLocation();
        String device = transaction.getDevice() != null ? transaction.getDevice().toLowerCase(Locale.ROOT) : "";
        String location = transaction.getLocation() != null ? transaction.getLocation().toLowerCase(Locale.ROOT) : "";

        // 1. Standalone Flag Checks
        if (newDevice) {
            riskScore += 25.0;
        }
        if (unusualLocation) {
            riskScore += 25.0;
        }

        // 2. Correlation Multiplier: Both new device AND unusual location at same time
        if (newDevice && unusualLocation) {
            riskScore += 30.0; // High likelihood of account takeover / credential stuffing
        }

        // 3. Suspicious Device Fingerprints (Emulators, Headless browsers, Terminals)
        if (device.contains("emulator") || device.contains("virtual") || device.contains("bot") ||
                device.contains("headless") || device.contains("rooted") || device.contains("jailbroken")) {
            riskScore += 35.0;
        } else if (device.contains("unknown") || device.contains("terminal")) {
            riskScore += 15.0;
        }

        // 4. Anonymous Proxy / High-Risk Network Keywords in Location
        if (location.contains("proxy") || location.contains("vpn") || location.contains("tor") || location.contains("unknown")) {
            riskScore += 20.0;
        }

        return clampScore(riskScore);
    }

    @Override
    public List<FraudIndicator> extractIndicators(Transaction transaction) {
        List<FraudIndicator> indicators = new ArrayList<>();
        if (transaction == null) {
            return indicators;
        }

        boolean newDevice = transaction.isNewDevice();
        boolean unusualLocation = transaction.isUnusualLocation();
        String device = transaction.getDevice() != null ? transaction.getDevice() : "Unknown";
        String location = transaction.getLocation() != null ? transaction.getLocation() : "Unknown";

        if (newDevice) {
            indicators.add(createIndicator("ANOMALY_NEW_HARDWARE",
                    "Unrecognized client hardware fingerprint detected: " + device, 20.0));
        }

        if (unusualLocation) {
            indicators.add(createIndicator("ANOMALY_GEOLOCATION_MISMATCH",
                    "Transaction initiated outside customer historical geofence: " + location, 20.0));
        }

        if (newDevice && unusualLocation) {
            indicators.add(createIndicator("ANOMALY_COMPOUND_TAKEOVER",
                    "Severe Compound Anomaly: Concurrently new device and unfamiliar geographical origin", 30.0));
        }

        String lowerDevice = device.toLowerCase(Locale.ROOT);
        if (lowerDevice.contains("emulator") || lowerDevice.contains("virtual") || lowerDevice.contains("rooted")) {
            indicators.add(createIndicator("ANOMALY_HOSTILE_ENVIRONMENT",
                    "Potentially automated or tampered environment signature detected: " + device, 30.0));
        }

        return indicators;
    }
}
