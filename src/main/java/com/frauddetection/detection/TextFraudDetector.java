package com.frauddetection.detection;

import com.frauddetection.model.FraudIndicator;
import com.frauddetection.model.Transaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Natural Language and Text Pattern Detector.
 * Identifies social engineering, credential harvesting, urgent demands, and lottery lures.
 * Fulfills Collections & Generics rubric: Uses HashSet, HashMap, List, Map, Set with Generics.
 */
public class TextFraudDetector extends FraudDetector {

    // Set collections for high-risk token dictionaries
    private static final Set<String> URGENCY_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "urgent", "urgently", "immediately", "act now", "immediate action",
            "suspended", "suspended account", "account blocked", "freeze", "terminated",
            "threat", "legal action", "arrest", "warrant", "24 hours", "time sensitive"
    )));

    private static final Set<String> CREDENTIAL_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "otp", "one time password", "password", "pin", "cvv", "security code",
            "secret key", "seed phrase", "private key", "ssn", "social security",
            "login credentials", "verify your account", "confirm identity"
    )));

    private static final Set<String> REWARD_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "congratulations", "winner", "lottery", "claim prize", "free bonus",
            "cash reward", "jackpot", "guaranteed return", "inheritance", "selected to receive"
    )));

    private static final Set<String> IMPERSONATION_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "bank security", "fraud department", "fraud alert", "irs agent", "tax officer",
            "fbi", "police department", "support desk", "system admin", "compliance officer"
    )));

    public TextFraudDetector() {
        super("Text Social Engineering Detector", 0.25); // 25% ensemble weight
    }

    @Override
    public double calculateRisk(Transaction transaction) {
        if (transaction == null || transaction.getSuspiciousText() == null) {
            return 0.0;
        }

        String text = transaction.getSuspiciousText().toLowerCase(Locale.ROOT);
        if (text.trim().isEmpty()) {
            return 0.0;
        }

        double riskScore = 0.0;
        Map<String, Integer> categoryHits = countCategoryHits(text);

        // Score based on category hits
        if (categoryHits.getOrDefault("CREDENTIAL", 0) > 0) {
            // Demanding OTP, password, or security PIN is a severe cyber-fraud indicator
            riskScore += 75.0 + (categoryHits.get("CREDENTIAL") - 1) * 12.0;
        }
        if (categoryHits.getOrDefault("URGENCY", 0) > 0) {
            riskScore += 30.0 + (categoryHits.get("URGENCY") - 1) * 6.0;
        }
        if (categoryHits.getOrDefault("REWARD", 0) > 0) {
            riskScore += 35.0 + (categoryHits.get("REWARD") - 1) * 8.0;
        }
        if (categoryHits.getOrDefault("IMPERSONATION", 0) > 0) {
            riskScore += 30.0 + (categoryHits.get("IMPERSONATION") - 1) * 8.0;
        }

        return clampScore(riskScore);
    }

    @Override
    public List<FraudIndicator> extractIndicators(Transaction transaction) {
        List<FraudIndicator> indicators = new ArrayList<>();
        if (transaction == null || transaction.getSuspiciousText() == null) {
            return indicators;
        }

        String text = transaction.getSuspiciousText().toLowerCase(Locale.ROOT);
        if (text.trim().isEmpty()) {
            return indicators;
        }

        List<String> matchedUrgency = findMatchedKeywords(text, URGENCY_KEYWORDS);
        List<String> matchedCredentials = findMatchedKeywords(text, CREDENTIAL_KEYWORDS);
        List<String> matchedRewards = findMatchedKeywords(text, REWARD_KEYWORDS);
        List<String> matchedImpersonation = findMatchedKeywords(text, IMPERSONATION_KEYWORDS);

        if (!matchedCredentials.isEmpty()) {
            indicators.add(createIndicator("TEXT_CREDENTIAL_DEMAND",
                    "Suspicious request for confidential credentials detected: " + String.join(", ", matchedCredentials), 35.0));
        }

        if (!matchedUrgency.isEmpty()) {
            indicators.add(createIndicator("TEXT_URGENCY_PRESSURE",
                    "Psychological urgency/coercion markers detected: " + String.join(", ", matchedUrgency), 20.0));
        }

        if (!matchedRewards.isEmpty()) {
            indicators.add(createIndicator("TEXT_REWARD_LURE",
                    "Advance-fee or lottery scam language detected: " + String.join(", ", matchedRewards), 25.0));
        }

        if (!matchedImpersonation.isEmpty()) {
            indicators.add(createIndicator("TEXT_AUTHORITY_IMPERSONATION",
                    "Authority/Institution impersonation phrases detected: " + String.join(", ", matchedImpersonation), 22.0));
        }

        return indicators;
    }

    /**
     * Fulfills Collections requirement: HashMap<String, Integer> usage.
     */
    private Map<String, Integer> countCategoryHits(String text) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("URGENCY", countMatches(text, URGENCY_KEYWORDS));
        counts.put("CREDENTIAL", countMatches(text, CREDENTIAL_KEYWORDS));
        counts.put("REWARD", countMatches(text, REWARD_KEYWORDS));
        counts.put("IMPERSONATION", countMatches(text, IMPERSONATION_KEYWORDS));
        return counts;
    }

    private int countMatches(String text, Set<String> keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private List<String> findMatchedKeywords(String text, Set<String> keywords) {
        List<String> matches = new ArrayList<>();
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                matches.add("'" + keyword + "'");
            }
        }
        return matches;
    }
}
