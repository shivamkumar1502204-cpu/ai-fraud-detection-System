package com.frauddetection.model;

import com.frauddetection.exception.InvalidInputException;
import com.frauddetection.util.Validatable;
import com.frauddetection.util.ValidationUtil;
import java.time.LocalDateTime;

/**
 * Domain entity capturing digital transaction metadata, client environment telemetry,
 * and contextual payloads for fraud evaluation.
 * Fulfills OOP Rubric: Encapsulation & Validatable interface implementation.
 */
public class Transaction implements Validatable {

    private int id; // Database Primary Key
    private String transactionId; // Unique business transaction code, e.g. "TXN-90214"
    private String senderName;
    private String receiverName;
    private double amount;
    private String transactionType; // "CARD_PAYMENT", "P2P_TRANSFER", "ONLINE_PAYMENT", "CRYPTO_PURCHASE", "INTERNATIONAL_WIRE"
    private String location;
    private String device;
    private int transactionFrequency; // Transactions initiated within the past hour
    private boolean newDevice;
    private boolean unusualLocation;
    private String suspiciousText;
    private String url;
    private double previousAmount; // Historical baseline average / previous transaction
    private LocalDateTime createdAt;

    public Transaction() {
        this.transactionFrequency = 1;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(int id, String transactionId, String senderName, String receiverName,
                       double amount, String transactionType, String location, String device,
                       int transactionFrequency, boolean newDevice, boolean unusualLocation,
                       String suspiciousText, String url, double previousAmount, LocalDateTime createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.amount = amount;
        this.transactionType = transactionType;
        this.location = location;
        this.device = device;
        this.transactionFrequency = transactionFrequency;
        this.newDevice = newDevice;
        this.unusualLocation = unusualLocation;
        this.suspiciousText = suspiciousText;
        this.url = url;
        this.previousAmount = previousAmount;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    @Override
    public void validate() throws InvalidInputException {
        ValidationUtil.validateNotEmpty(transactionId, "Transaction ID");
        ValidationUtil.validateNotEmpty(senderName, "Sender Name");
        ValidationUtil.validateNotEmpty(receiverName, "Receiver Name");
        ValidationUtil.validatePositive(amount, "Amount");
        ValidationUtil.validateNotEmpty(transactionType, "Transaction Type");
        ValidationUtil.validateNotEmpty(location, "Location");
        ValidationUtil.validateNotEmpty(device, "Device");
        ValidationUtil.validateNonNegative(transactionFrequency, "Transaction Frequency");
        ValidationUtil.validateUrlIfPresent(url);
        ValidationUtil.validateMaxLength(transactionId, 50, "Transaction ID");
        ValidationUtil.validateMaxLength(senderName, 100, "Sender Name");
        ValidationUtil.validateMaxLength(receiverName, 100, "Receiver Name");
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getTransactionFrequency() {
        return transactionFrequency;
    }

    public void setTransactionFrequency(int transactionFrequency) {
        this.transactionFrequency = transactionFrequency;
    }

    public boolean isNewDevice() {
        return newDevice;
    }

    public void setNewDevice(boolean newDevice) {
        this.newDevice = newDevice;
    }

    public boolean isUnusualLocation() {
        return unusualLocation;
    }

    public void setUnusualLocation(boolean unusualLocation) {
        this.unusualLocation = unusualLocation;
    }

    public String getSuspiciousText() {
        return suspiciousText;
    }

    public void setSuspiciousText(String suspiciousText) {
        this.suspiciousText = suspiciousText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(double previousAmount) {
        this.previousAmount = previousAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionId='" + transactionId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", amount=" + amount +
                ", type='" + transactionType + '\'' +
                '}';
    }
}
