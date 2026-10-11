package com.frauddetection.service;

import com.frauddetection.dao.FraudAnalysisDAO;
import com.frauddetection.dao.FraudIndicatorDAO;
import com.frauddetection.dao.TransactionDAO;
import com.frauddetection.exception.DatabaseOperationException;
import com.frauddetection.model.Decision;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.RiskLevel;
import com.frauddetection.model.Transaction;
import com.frauddetection.util.AuditLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing transactional history, retrieval, and multifaceted filtering.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO;
    private final FraudAnalysisDAO analysisDAO;
    private final FraudIndicatorDAO indicatorDAO;
    private final AuditLogger auditLogger;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.analysisDAO = new FraudAnalysisDAO();
        this.indicatorDAO = new FraudIndicatorDAO();
        this.auditLogger = AuditLogger.getInstance();
    }

    public List<Transaction> findAllTransactions() throws DatabaseOperationException {
        return transactionDAO.findAll();
    }

    public List<Transaction> findRecentTransactions(int limit) throws DatabaseOperationException {
        return transactionDAO.findRecentTransactions(limit);
    }

    public Optional<Transaction> findById(int id) throws DatabaseOperationException {
        return transactionDAO.findById(id);
    }

    public Optional<Transaction> findByCode(String code) throws DatabaseOperationException {
        return transactionDAO.findByTransactionId(code);
    }

    public Optional<FraudAnalysis> findAnalysisForTransaction(int transactionId) throws DatabaseOperationException {
        Optional<FraudAnalysis> analysisOpt = analysisDAO.findByTransactionId(transactionId);
        if (analysisOpt.isPresent()) {
            FraudAnalysis fa = analysisOpt.get();
            List<FraudIndicator> indicators = indicatorDAO.findByAnalysisId(fa.getId());
            fa.setIndicators(indicators);
        }
        return analysisOpt;
    }

    public boolean deleteTransaction(int id, String currentUsername) throws DatabaseOperationException {
        Optional<Transaction> tx = transactionDAO.findById(id);
        boolean deleted = transactionDAO.delete(id);
        if (deleted && tx.isPresent()) {
            auditLogger.log(currentUsername, "DELETE_TRANSACTION",
                    "Deleted transaction: " + tx.get().getTransactionId() + " ($" + tx.get().getAmount() + ")");
        }
        return deleted;
    }

    /**
     * DTO uniting Transaction and FraudAnalysis for presentation in history tables.
     */
    public static class TransactionHistoryItem {
        private final Transaction transaction;
        private final FraudAnalysis analysis;

        public TransactionHistoryItem(Transaction transaction, FraudAnalysis analysis) {
            this.transaction = transaction;
            this.analysis = analysis;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public FraudAnalysis getAnalysis() {
            return analysis;
        }
    }

    /**
     * Retrieves combined transaction history items with their respective fraud analyses.
     * Fulfills Collections and Streams requirement.
     */
    public List<TransactionHistoryItem> getCombinedHistory() throws DatabaseOperationException {
        List<Transaction> transactions = transactionDAO.findAll();
        List<FraudAnalysis> analyses = analysisDAO.findAll();

        // Create lookup index
        java.util.Map<Integer, FraudAnalysis> analysisMap = analyses.stream()
                .collect(Collectors.toMap(FraudAnalysis::getTransactionId, a -> a, (k1, k2) -> k1));

        List<TransactionHistoryItem> items = new ArrayList<>();
        for (Transaction tx : transactions) {
            FraudAnalysis fa = analysisMap.get(tx.getId());
            items.add(new TransactionHistoryItem(tx, fa));
        }
        return items;
    }

    /**
     * Filters transaction history dynamically by search keyword, risk level, and operational decision.
     */
    public List<TransactionHistoryItem> filterHistory(List<TransactionHistoryItem> items,
                                                      String searchKeyword,
                                                      String riskFilter,
                                                      String decisionFilter) {
        if (items == null) {
            return new ArrayList<>();
        }

        final String term = searchKeyword != null ? searchKeyword.trim().toLowerCase(Locale.ROOT) : "";

        return items.stream().filter(item -> {
            Transaction tx = item.getTransaction();
            FraudAnalysis fa = item.getAnalysis();

            // Search Keyword match
            if (!term.isEmpty()) {
                boolean matchesCode = tx.getTransactionId().toLowerCase(Locale.ROOT).contains(term);
                boolean matchesSender = tx.getSenderName().toLowerCase(Locale.ROOT).contains(term);
                boolean matchesReceiver = tx.getReceiverName().toLowerCase(Locale.ROOT).contains(term);
                boolean matchesType = tx.getTransactionType().toLowerCase(Locale.ROOT).contains(term);
                if (!matchesCode && !matchesSender && !matchesReceiver && !matchesType) {
                    return false;
                }
            }

            // Risk Level Filter
            if (riskFilter != null && !riskFilter.equalsIgnoreCase("ALL")) {
                if (fa == null || !fa.getRiskLevel().name().equalsIgnoreCase(riskFilter)) {
                    return false;
                }
            }

            // Decision Filter
            if (decisionFilter != null && !decisionFilter.equalsIgnoreCase("ALL")) {
                if (fa == null || !fa.getDecision().name().equalsIgnoreCase(decisionFilter)) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }
}
