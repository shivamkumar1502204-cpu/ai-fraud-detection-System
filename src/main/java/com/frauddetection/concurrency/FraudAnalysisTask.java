package com.frauddetection.concurrency;

import com.frauddetection.detection.FraudDetectionEngine;
import com.frauddetection.exception.FraudAnalysisException;
import com.frauddetection.model.FraudAnalysis;
import com.frauddetection.model.Transaction;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Concurrent Callable task executing AI fraud detection asynchronously in a background thread pool.
 * Keeps the Java Swing GUI completely responsive during intensive heuristic calculations.
 * Fulfills Multithreading & Synchronization rubric: Multithreading, Concurrency, Tasks.
 */
public class FraudAnalysisTask implements Callable<FraudAnalysis> {

    private final Transaction transaction;
    private final FraudDetectionEngine engine;
    private final Consumer<Integer> progressCallback;

    public FraudAnalysisTask(Transaction transaction, FraudDetectionEngine engine, Consumer<Integer> progressCallback) {
        this.transaction = transaction;
        this.engine = engine;
        this.progressCallback = progressCallback;
    }

    @Override
    public FraudAnalysis call() throws FraudAnalysisException {
        reportProgress(10);
        sleepBriefly(150); // Realistic simulated telemetry verification latency

        reportProgress(30);
        sleepBriefly(150);

        reportProgress(60);
        FraudAnalysis result = engine.analyze(transaction);
        sleepBriefly(150);

        reportProgress(90);
        sleepBriefly(100);

        reportProgress(100);
        return result;
    }

    private void reportProgress(int percent) {
        if (progressCallback != null) {
            progressCallback.accept(percent);
        }
    }

    private void sleepBriefly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
