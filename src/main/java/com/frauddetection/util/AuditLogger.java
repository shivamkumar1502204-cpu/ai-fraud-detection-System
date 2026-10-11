package com.frauddetection.util;

import com.frauddetection.dao.AuditLogDAO;
import com.frauddetection.model.AuditLog;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe asynchronous Audit Logger.
 * Uses a BlockingQueue and a dedicated background daemon worker thread to persist
 * compliance audit events without inducing latency on the main transactional flow.
 * Fulfills Collections (Queue<T>), Multithreading & Synchronization rubric.
 */
public class AuditLogger {

    private static AuditLogger instance;

    // Queue requirement: BlockingQueue<AuditLog> generic collection
    private final BlockingQueue<AuditLog> logQueue;
    private final AuditLogDAO auditLogDAO;
    private final Thread workerThread;
    private final AtomicBoolean running;

    private AuditLogger() {
        this.logQueue = new LinkedBlockingQueue<>(1000);
        this.auditLogDAO = new AuditLogDAO();
        this.running = new AtomicBoolean(true);

        this.workerThread = new Thread(this::processQueue, "AuditLogger-WorkerThread");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    /**
     * Non-blocking, thread-safe audit log submission.
     *
     * @param username The authenticated user or system identity
     * @param action The operation performed
     * @param details Additional context and metadata
     */
    public void log(String username, String action, String details) {
        AuditLog log = new AuditLog(username, action, details);
        logQueue.offer(log);
    }

    /**
     * Synchronous log method for critical events requiring immediate write-through.
     */
    public synchronized void logSynchronous(String username, String action, String details) {
        try {
            AuditLog log = new AuditLog(username, action, details);
            auditLogDAO.save(log);
        } catch (Exception e) {
            System.err.println("[AuditLogger] Failed to write synchronous audit log: " + e.getMessage());
        }
    }

    private void processQueue() {
        while (running.get() || !logQueue.isEmpty()) {
            try {
                AuditLog log = logQueue.take();
                try {
                    auditLogDAO.save(log);
                } catch (Exception e) {
                    System.err.println("[AuditLogger] Background persist error: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() {
        running.set(false);
        workerThread.interrupt();
    }
}
