package org.intentcloudsim.intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PATENT IDEA 19: Learning User Intent Patterns Over Time
 *
 * Tracks what users have requested historically and predicts
 * their future intents based on patterns.
 *
 * NOVELTY: No cloud system learns user intent patterns for
 * proactive resource allocation.
 */
public class IntentHistoryLearner {

    // Store history of intents for each user
    private Map<String, List<Intent>> userHistory;

    // How many recent intents to consider for prediction
    private int windowSize;

    public IntentHistoryLearner() {
        this.userHistory = new HashMap<>();
        this.windowSize = 10; // consider last 10 intents
    }

    /**
     * Record a new intent for a user.
     */
    public void learn(String userId, Intent intent) {
        intent.setUserId(userId);

        // Create list if first time seeing this user
        if (!userHistory.containsKey(userId)) {
            userHistory.put(userId, new ArrayList<>());
        }

        userHistory.get(userId).add(intent);

        System.out.println("[IntentLearner] Learned intent for user: "
                           + userId);
        System.out.println("[IntentLearner] Total history for " + userId
                           + ": " + userHistory.get(userId).size()
                           + " intents");
    }

    /**
     * Predict what a user will likely want next,
     * based on their history.
     *
     * Uses weighted average of recent intents
     * (more recent = higher weight).
     */
    public Intent predict(String userId) {
        if (!userHistory.containsKey(userId) ||
            userHistory.get(userId).isEmpty()) {
            System.out.println("[IntentLearner] No history for " + userId
                               + ", returning default");
            return new Intent(); // default
        }

        List<Intent> history = userHistory.get(userId);

        // Get the most recent intents (up to windowSize)
        int start = Math.max(0, history.size() - windowSize);
        List<Intent> recent = history.subList(start, history.size());

        // Weighted average (recent intents get higher weight)
        double totalWeight = 0;
        double costSum = 0, latencySum = 0;
        double securitySum = 0, carbonSum = 0;

        for (int i = 0; i < recent.size(); i++) {
            // Weight increases linearly: 1, 2, 3, ...
            double weight = i + 1;
            Intent intent = recent.get(i);

            costSum += intent.getCostPriority() * weight;
            latencySum += intent.getLatencyPriority() * weight;
            securitySum += intent.getSecurityPriority() * weight;
            carbonSum += intent.getCarbonPriority() * weight;
            totalWeight += weight;
        }

        Intent predicted = new Intent(
            costSum / totalWeight,
            latencySum / totalWeight,
            securitySum / totalWeight,
            carbonSum / totalWeight
        );
        predicted.setUserId(userId);

        System.out.println("[IntentLearner] Predicted for " + userId
                           + ": " + predicted);

        return predicted;
    }

    /**
     * Get how many intents we've recorded for a user.
     */
    public int getHistorySize(String userId) {
        if (!userHistory.containsKey(userId)) return 0;
        return userHistory.get(userId).size();
    }

    /**
     * Check if user's intents are consistent
     * (low variance = predictable user).
     */
    public double getConsistencyScore(String userId) {
        if (!userHistory.containsKey(userId) ||
            userHistory.get(userId).size() < 2) {
            return 0.0;
        }

        List<Intent> history = userHistory.get(userId);
        Intent predicted = predict(userId);

        double totalVariance = 0;
        for (Intent intent : history) {
            totalVariance += Math.pow(
                intent.getCostPriority() - predicted.getCostPriority(), 2);
            totalVariance += Math.pow(
                intent.getLatencyPriority() - predicted.getLatencyPriority(), 2);
        }

        totalVariance /= history.size();

        // Convert variance to consistency (lower variance = higher consistency)
        return Math.max(0, 1.0 - totalVariance);
    }
}

