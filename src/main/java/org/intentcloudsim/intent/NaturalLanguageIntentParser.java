package org.intentcloudsim.intent;

import java.util.HashMap;
import java.util.Map;

/**
 * PATENT IDEA 16: Natural Language to Cloud Intent Parser
 *
 * Converts human-readable text like "I want fast and cheap servers"
 * into an Intent vector that the cloud system can understand.
 *
 * NOVELTY: No existing cloud system converts natural language
 * directly into virtualization parameters.
 */
public class NaturalLanguageIntentParser {

    // Keywords that indicate cost priority
    private static final Map<String, Double> COST_KEYWORDS = new HashMap<>();

    // Keywords that indicate latency/speed priority
    private static final Map<String, Double> LATENCY_KEYWORDS = new HashMap<>();

    // Keywords that indicate security priority
    private static final Map<String, Double> SECURITY_KEYWORDS = new HashMap<>();

    // Keywords that indicate carbon/green priority
    private static final Map<String, Double> CARBON_KEYWORDS = new HashMap<>();

    // Initialize all keywords and their weights
    static {
        // Cost-related keywords
        COST_KEYWORDS.put("cheap", 0.9);
        COST_KEYWORDS.put("budget", 0.85);
        COST_KEYWORDS.put("affordable", 0.8);
        COST_KEYWORDS.put("economical", 0.8);
        COST_KEYWORDS.put("low cost", 0.9);
        COST_KEYWORDS.put("save money", 0.85);
        COST_KEYWORDS.put("inexpensive", 0.8);
        COST_KEYWORDS.put("cost-effective", 0.75);
        COST_KEYWORDS.put("minimize cost", 0.9);
        COST_KEYWORDS.put("free tier", 0.95);

        // Speed/latency-related keywords
        LATENCY_KEYWORDS.put("fast", 0.9);
        LATENCY_KEYWORDS.put("quick", 0.85);
        LATENCY_KEYWORDS.put("rapid", 0.85);
        LATENCY_KEYWORDS.put("low latency", 0.95);
        LATENCY_KEYWORDS.put("real-time", 0.95);
        LATENCY_KEYWORDS.put("responsive", 0.8);
        LATENCY_KEYWORDS.put("high performance", 0.9);
        LATENCY_KEYWORDS.put("speed", 0.85);
        LATENCY_KEYWORDS.put("instant", 0.9);
        LATENCY_KEYWORDS.put("gaming", 0.85);
        LATENCY_KEYWORDS.put("streaming", 0.8);

        // Security-related keywords
        SECURITY_KEYWORDS.put("secure", 0.9);
        SECURITY_KEYWORDS.put("encrypted", 0.85);
        SECURITY_KEYWORDS.put("private", 0.8);
        SECURITY_KEYWORDS.put("confidential", 0.85);
        SECURITY_KEYWORDS.put("compliant", 0.8);
        SECURITY_KEYWORDS.put("hipaa", 0.95);
        SECURITY_KEYWORDS.put("gdpr", 0.9);
        SECURITY_KEYWORDS.put("isolated", 0.85);
        SECURITY_KEYWORDS.put("protected", 0.8);
        SECURITY_KEYWORDS.put("banking", 0.9);
        SECURITY_KEYWORDS.put("healthcare", 0.9);

        // Carbon/sustainability keywords
        CARBON_KEYWORDS.put("green", 0.9);
        CARBON_KEYWORDS.put("sustainable", 0.85);
        CARBON_KEYWORDS.put("eco", 0.85);
        CARBON_KEYWORDS.put("carbon neutral", 0.95);
        CARBON_KEYWORDS.put("renewable", 0.9);
        CARBON_KEYWORDS.put("environment", 0.8);
        CARBON_KEYWORDS.put("low carbon", 0.9);
        CARBON_KEYWORDS.put("energy efficient", 0.85);
    }

    /**
     * Parse a natural language string into an Intent object.
     *
     * @param input The user's natural language request
     * @return An Intent object with appropriate priorities
     */
    public static Intent parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("[IntentParser] Empty input, returning default intent");
            return new Intent(); // default balanced intent
        }

        String lowerInput = input.toLowerCase().trim();

        double costScore = calculateScore(lowerInput, COST_KEYWORDS);
        double latencyScore = calculateScore(lowerInput, LATENCY_KEYWORDS);
        double securityScore = calculateScore(lowerInput, SECURITY_KEYWORDS);
        double carbonScore = calculateScore(lowerInput, CARBON_KEYWORDS);

        // If no keywords matched, use moderate defaults
        if (costScore == 0 && latencyScore == 0 &&
            securityScore == 0 && carbonScore == 0) {
            costScore = 0.5;
            latencyScore = 0.5;
            securityScore = 0.3;
            carbonScore = 0.2;
        }

        Intent intent = new Intent(costScore, latencyScore,
                                    securityScore, carbonScore);

        System.out.println("[IntentParser] Input: \"" + input + "\"");
        System.out.println("[IntentParser] Parsed: " + intent);

        return intent;
    }

    /**
     * Calculate how strongly the input matches a keyword category.
     */
    private static double calculateScore(String input,
                                          Map<String, Double> keywords) {
        double maxScore = 0.0;
        int matchCount = 0;

        for (Map.Entry<String, Double> entry : keywords.entrySet()) {
            if (input.contains(entry.getKey())) {
                maxScore = Math.max(maxScore, entry.getValue());
                matchCount++;
            }
        }

        // Bonus for multiple keyword matches (shows strong intent)
        if (matchCount > 1) {
            maxScore = Math.min(1.0, maxScore + 0.05 * (matchCount - 1));
        }

        return maxScore;
    }

    /**
     * Get a human-readable explanation of the parsing.
     */
    public static String explain(String input) {
        Intent intent = parse(input);
        StringBuilder sb = new StringBuilder();
        sb.append("=== Intent Parsing Explanation ===\n");
        sb.append("Input: \"").append(input).append("\"\n");
        sb.append("Cost Priority: ").append(
            String.format("%.0f%%", intent.getCostPriority() * 100)).append("\n");
        sb.append("Latency Priority: ").append(
            String.format("%.0f%%", intent.getLatencyPriority() * 100)).append("\n");
        sb.append("Security Priority: ").append(
            String.format("%.0f%%", intent.getSecurityPriority() * 100)).append("\n");
        sb.append("Carbon Priority: ").append(
            String.format("%.0f%%", intent.getCarbonPriority() * 100)).append("\n");
        return sb.toString();
    }
}

