package com.frauddetection.service;

import com.frauddetection.concurrency.AnalysisStatistics;
import com.frauddetection.concurrency.DatabaseSaveTask;
import com.frauddetection.concurrency.FraudAnalysisTask;
import com.frauddetection.dao.FraudAnalysisDAO;
import com.frauddetection.detection.FraudDetectionEngine;
import com.frauddetection.exception.FraudAnalysisException;
import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.Transaction;
import com.frauddetection.util.AuditLogger;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Core Service orchestrating hybrid AI fraud analysis, asynchronous thread execution,
 * atomic JDBC persistence, and thread-safe metrics synchronization.
 * Fulfills Multithreading, Synchronization, and Service Architecture requirements.
 */
public class FraudAnalysisService {

    private final FraudDetectionEngine engine;
    private final FraudAnalysisDAO analysisDAO;
    private final AnalysisStatistics statistics;
    private final AuditLogger auditLogger;
    private final ExecutorService threadPool;

    public FraudAnalysisService() {
        this.engine = new FraudDetectionEngine();
        this.analysisDAO = new FraudAnalysisDAO();
        this.statistics = AnalysisStatistics.getInstance();
        this.auditLogger = AuditLogger.getInstance();
        // Fixed thread pool for concurrent asynchronous processing
        this.threadPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "FraudAnalysis-Worker");
            t.setDaemon(true);
            return t;
        });

        // Initialize statistics from database if available
        refreshStatisticsFromDb();
    }

    /**
     * Executes the end-to-end analysis and database save asynchronously in a background thread.
     * Prevents GUI freezing and ensures UI updates are dispatched safely to the Swing Event Dispatch Thread (EDT).
     *
     * @param transaction Transaction to evaluate
     * @param progressCallback Callback reporting completion percentage (0 - 100)
     * @param onSuccess Callback executed on EDT when analysis and save succeed
     * @param onError Callback executed on EDT when an exception occurs
     * @param currentUsername Authenticated user invoking the analysis
     */
    public void analyzeAndPersistAsync(Transaction transaction,
                                      Consumer<Integer> progressCallback,
                                      Consumer<FraudAnalysis> onSuccess,
                                      Consumer<Exception> onError,
                                      String currentUsername) {
        threadPool.submit(() -> {
            try {
                // 1. Validation
                transaction.validate();

                // 2. Multithreaded Fraud Analysis Task
                FraudAnalysisTask analysisTask = new FraudAnalysisTask(transaction, engine, pct -> {
                    SwingUtilities.invokeLater(() -> progressCallback.accept(pct));
                });
                FraudAnalysis analysis = analysisTask.call();

                // 3. Multithreaded Database Persistence Task
                DatabaseSaveTask saveTask = new DatabaseSaveTask(transaction, analysis);
                FraudAnalysis savedAnalysis = saveTask.call();

                // 4. Update Thread-Safe Synchronized Statistics
                statistics.recordAnalysis(savedAnalysis.getRiskScore(), savedAnalysis.getRiskLevel(), savedAnalysis.getDecision());

                // 5. Asynchronous Audit Logging
                auditLogger.log(currentUsername, "ANALYZE_TRANSACTION",
                        String.format("Evaluated %s: Score %d (%s, %s)",
                                transaction.getTransactionId(),
                                savedAnalysis.getRiskScore(),
                                savedAnalysis.getRiskLevel(),
                                savedAnalysis.getDecision()));

                // 6. Return Result to GUI on Event Dispatch Thread
                SwingUtilities.invokeLater(() -> onSuccess.accept(savedAnalysis));

            } catch (Exception e) {
                auditLogger.log(currentUsername, "ANALYSIS_FAILED",
                        "Failed evaluation for " + transaction.getTransactionId() + ": " + e.getMessage());
                SwingUtilities.invokeLater(() -> onError.accept(e));
            }
        });
    }

    /**
     * Synchronously runs analysis for automated testing or batch operations.
     */
    public FraudAnalysis analyzeSync(Transaction transaction) throws Exception {
        transaction.validate();
        return engine.analyze(transaction);
    }

    /**
     * Refreshes in-memory statistics from the MySQL database.
     */
    public void refreshStatisticsFromDb() {
        try {
            Map<String, Object> dbStats = analysisDAO.getRiskStatistics();
            statistics.syncFromDatabase(dbStats);
        } catch (Exception e) {
            System.err.println("[FraudAnalysisService] Note: Could not query initial DB statistics: " + e.getMessage());
        }
    }

    public Map<String, Object> getLiveStatistics() {
        return statistics.getSnapshot();
    }

    public FraudDetectionEngine getEngine() {
        return engine;
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}
