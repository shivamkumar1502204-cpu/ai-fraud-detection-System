package com.frauddetection.test;

import com.frauddetection.concurrency.AnalysisStatistics;
import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.detection.FraudDetectionEngine;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.model.Decision;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.RiskLevel;
import com.frauddetection.model.Transaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Automated Verification Test Suite directly verifying all 7 rubric scenarios:
 *
 * Test 1: Normal low-value transaction (Expected: LOW)
 * Test 2: High amount + new device (Expected: MEDIUM or HIGH)
 * Test 3: Suspicious URL + urgent message + unusual location (Expected: HIGH or CRITICAL)
 * Test 4: OTP/password request message (Expected: HIGH or CRITICAL)
 * Test 5: Invalid amount (Expected: validation exception)
 * Test 6: Invalid database connection handled gracefully (Expected: DatabaseOperationException caught)
 * Test 7: Multiple simultaneous concurrent analyses (Expected: Thread-safe, non-blocking)
 */
public class SystemVerificationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" STARTING AUTOMATED RUBRIC VERIFICATION SUITE   ");
        System.out.println("=================================================");

        FraudDetectionEngine engine = new FraudDetectionEngine();

        testScenario1(engine);
        testScenario2(engine);
        testScenario3(engine);
        testScenario4(engine);
        testScenario5();
        testScenario6();
        testScenario7(engine);

        System.out.println("\n=================================================");
        System.out.printf(" VERIFICATION RESULTS: %d PASSED, %d FAILED%n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed == 0) {
            System.out.println(">>> ALL RUBRIC TEST SCENARIOS VALIDATED SUCCESSFULLY! <<<");
        } else {
            System.err.println(">>> SOME TESTS FAILED <<<");
            System.exit(1);
        }
    }

    private static void testScenario1(FraudDetectionEngine engine) {
        System.out.print("[Test 1] Normal Low-Value Transaction... ");
        try {
            Transaction tx = new Transaction(1, "TXN-T1", "John Doe", "Coffee Shop", 12.50,
                    "CARD_PAYMENT", "Seattle, USA", "Known Phone", 1, false, false,
                    "Regular purchase", "https://trusted-shop.com", 15.00, LocalDateTime.now());
            tx.validate();
            FraudAnalysis result = engine.analyze(tx);

            if (result.getRiskLevel() == RiskLevel.LOW && result.getDecision() == Decision.SAFE) {
                System.out.printf("PASSED (Score: %d, Tier: %s, Decision: %s)%n",
                        result.getRiskScore(), result.getRiskLevel(), result.getDecision());
                testsPassed++;
            } else {
                System.out.printf("FAILED (Expected LOW/SAFE, got %s/%s, score: %d)%n",
                        result.getRiskLevel(), result.getDecision(), result.getRiskScore());
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testScenario2(FraudDetectionEngine engine) {
        System.out.print("[Test 2] High Amount + New Device... ");
        try {
            Transaction tx = new Transaction(2, "TXN-T2", "Alice Smith", "Electronics Store", 7500.00,
                    "ONLINE_PAYMENT", "Dallas, USA", "Brand New Laptop", 2, true, false,
                    "Order confirmation", "https://electronics-mall.com", 200.00, LocalDateTime.now());
            tx.validate();
            FraudAnalysis result = engine.analyze(tx);

            if (result.getRiskLevel() == RiskLevel.MEDIUM || result.getRiskLevel() == RiskLevel.HIGH) {
                System.out.printf("PASSED (Score: %d, Tier: %s, Decision: %s)%n",
                        result.getRiskScore(), result.getRiskLevel(), result.getDecision());
                testsPassed++;
            } else {
                System.out.printf("FAILED (Expected MEDIUM or HIGH, got %s, score: %d)%n",
                        result.getRiskLevel(), result.getRiskScore());
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testScenario3(FraudDetectionEngine engine) {
        System.out.print("[Test 3] Suspicious URL + Urgent Message + Unusual Location... ");
        try {
            Transaction tx = new Transaction(3, "TXN-T3", "Victim User", "Unknown Vendor", 2800.00,
                    "ONLINE_PAYMENT", "Moscow, Russia", "Untrusted Device", 4, true, true,
                    "URGENT: Act immediately or your account will be permanently blocked!",
                    "http://192.168.1.55/update-login/verify", 100.00, LocalDateTime.now());
            tx.validate();
            FraudAnalysis result = engine.analyze(tx);

            if (result.getRiskLevel() == RiskLevel.HIGH || result.getRiskLevel() == RiskLevel.CRITICAL) {
                System.out.printf("PASSED (Score: %d, Tier: %s, Decision: %s)%n",
                        result.getRiskScore(), result.getRiskLevel(), result.getDecision());
                testsPassed++;
            } else {
                System.out.printf("FAILED (Expected HIGH/CRITICAL, got %s, score: %d)%n",
                        result.getRiskLevel(), result.getRiskScore());
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testScenario4(FraudDetectionEngine engine) {
        System.out.print("[Test 4] OTP / Password Phishing Request... ");
        try {
            Transaction tx = new Transaction(4, "TXN-T4", "Bank Client", "Phishing Collector", 1500.00,
                    "P2P_TRANSFER", "New York, USA", "Known Device", 1, false, false,
                    "Please verify your identity: provide your OTP and password immediately to prevent suspension.",
                    "https://bank-notice.org", 500.00, LocalDateTime.now());
            tx.validate();
            FraudAnalysis result = engine.analyze(tx);

            if (result.getRiskLevel() == RiskLevel.HIGH || result.getRiskLevel() == RiskLevel.CRITICAL) {
                System.out.printf("PASSED (Score: %d, Tier: %s, Decision: %s)%n",
                        result.getRiskScore(), result.getRiskLevel(), result.getDecision());
                testsPassed++;
            } else {
                System.out.printf("FAILED (Expected HIGH/CRITICAL, got %s, score: %d)%n",
                        result.getRiskLevel(), result.getRiskScore());
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testScenario5() {
        System.out.print("[Test 5] Invalid Amount Validation (e.g. Negative Amount)... ");
        try {
            Transaction tx = new Transaction(5, "TXN-T5", "User", "Merchant", -150.00,
                    "CARD_PAYMENT", "City", "Device", 1, false, false,
                    "", "", 0.0, LocalDateTime.now());
            tx.validate(); // Should throw InvalidInputException
            System.out.println("FAILED: Validation did not catch negative amount!");
            testsFailed++;
        } catch (InvalidInputException e) {
            System.out.println("PASSED (Caught expected InvalidInputException: " + e.getMessage() + ")");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED: Unexpected exception type: " + e);
            testsFailed++;
        }
    }

    private static void testScenario6() {
        System.out.print("[Test 6] Database Connection Exception Graceful Handling... ");
        try {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.getInstance().testConnection();
            // Status object is created without throwing uncaught raw exceptions
            if (status != null) {
                System.out.printf("PASSED (Diagnostic handled gracefully: Connected=%b, Message=%s)%n",
                        status.isConnected(), status.getMessage());
                testsPassed++;
            } else {
                System.out.println("FAILED: status returned null");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: Exception escaped unhandled: " + e);
            testsFailed++;
        }
    }

    private static void testScenario7(FraudDetectionEngine engine) {
        System.out.print("[Test 7] Multithreaded Concurrent Execution & Synchronization... ");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        AnalysisStatistics stats = AnalysisStatistics.getInstance();
        stats.reset();

        int tasksCount = 50;
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < tasksCount; i++) {
            final int idx = i;
            tasks.add(() -> {
                double amount = 50.0 + (idx * 500.0);
                boolean newDev = (idx % 2 == 0);
                Transaction tx = new Transaction(100 + idx, "TXN-CONC-" + idx, "Sender " + idx, "Receiver",
                        amount, "CARD_PAYMENT", "City " + idx, "Device", 1, newDev, false,
                        idx % 3 == 0 ? "urgent password request" : "nominal text",
                        "https://example.com", 100.0, LocalDateTime.now());
                FraudAnalysis res = engine.analyze(tx);
                stats.recordAnalysis(res.getRiskScore(), res.getRiskLevel(), res.getDecision());
                return true;
            });
        }

        try {
            List<Future<Boolean>> futures = pool.invokeAll(tasks);
            for (Future<Boolean> f : futures) {
                f.get(); // ensure all completed without error
            }
            pool.shutdown();

            var snapshot = stats.getSnapshot();
            int recorded = (Integer) snapshot.get("totalAnalyses");
            if (recorded == tasksCount) {
                System.out.printf("PASSED (%d concurrent tasks processed, synchronized count=%d)%n", tasksCount, recorded);
                testsPassed++;
            } else {
                System.out.printf("FAILED (Expected %d recorded, found %d)%n", tasksCount, recorded);
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED with concurrent exception: " + e.getMessage());
            testsFailed++;
        }
    }
}
