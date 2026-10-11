package com.frauddetection.concurrency;

import com.frauddetection.dao.FraudAnalysisDAO;
import com.frauddetection.dao.FraudIndicatorDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.database.DatabaseConnection;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Concurrent Callable task handling transactional persistence across transactions,
 * fraud_analysis, and fraud_indicators tables.
 * Fulfills Multithreading & JDBC Transaction Management requirements.
 */
public class DatabaseSaveTask implements Callable<FraudAnalysis> {

    private final Transaction transaction;
    private final FraudAnalysis analysis;
    private final TransactionDAO transactionDAO;
    private final FraudAnalysisDAO analysisDAO;
    private final FraudIndicatorDAO indicatorDAO;
    private final DatabaseConnection db;

    public DatabaseSaveTask(Transaction transaction, FraudAnalysis analysis) {
        this.transaction = transaction;
        this.analysis = analysis;
        this.transactionDAO = new TransactionDAO();
        this.analysisDAO = new FraudAnalysisDAO();
        this.indicatorDAO = new FraudIndicatorDAO();
        this.db = DatabaseConnection.getInstance();
    }

    @Override
    public FraudAnalysis call() throws DatabaseOperationException {
        try (Connection conn = db.getConnection()) {
            // Begin manual transaction
            conn.setAutoCommit(false);
            try {
                // 1. Save Transaction
                transactionDAO.save(transaction, conn);

                // 2. Link Transaction ID to Analysis
                analysis.setTransactionId(transaction.getId());

                // 3. Save Fraud Analysis
                analysisDAO.save(analysis, conn);

                // 4. Batch Save Indicators
                indicatorDAO.saveBatch(analysis.getId(), analysis.getIndicators(), conn);

                // Commit Transaction
                conn.commit();
                return analysis;

            } catch (SQLException ex) {
                conn.rollback();
                throw new DatabaseOperationException("Transactional persistence failed. Rolled back changes.", ex.getSQLState(), ex.getErrorCode(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database connection error during save: " + e.getMessage(), e);
        }
    }
}
