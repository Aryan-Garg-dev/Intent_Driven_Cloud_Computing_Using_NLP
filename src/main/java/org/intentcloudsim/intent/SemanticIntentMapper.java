package org.intentcloudsim.intent;

import java.util.*;

/**
 * Semantic intent mapper using sentence embeddings + anchor similarity.
 */
public class SemanticIntentMapper {

    private final SentenceBertLikeEncoder encoder;
    private final Map<String, List<String>> anchors;
    private final Map<String, List<double[]>> anchorEmbeddings;

    public SemanticIntentMapper() {
        this.encoder = new SentenceBertLikeEncoder();
        this.anchors = createAnchors();
        this.anchorEmbeddings = precomputeEmbeddings();
    }

    public Map<String, Double> extractScores(String input) {
        Map<String, Double> scores = new HashMap<>();
        if (input == null || input.isBlank()) {
            scores.put("cost", 0.5);
            scores.put("latency", 0.5);
            scores.put("security", 0.5);
            scores.put("carbon", 0.3);
            return scores;
        }

        String text = input.toLowerCase(Locale.ROOT).trim();
        double[] sentenceEmbedding = encoder.encode(text);

        for (Map.Entry<String, List<double[]>> entry : anchorEmbeddings.entrySet()) {
            String dim = entry.getKey();
            double best = 0;
            for (double[] anchorEmbedding : entry.getValue()) {
                best = Math.max(best, encoder.cosineSimilarity(sentenceEmbedding, anchorEmbedding));
            }

            // Soft lexical fallback to avoid brittle behavior on short phrases.
            for (String phrase : anchors.get(dim)) {
                if (text.contains(phrase)) {
                    best = Math.max(best, 0.82);
                }
            }

            best = applyNegationHeuristics(dim, text, best);
            scores.put(dim, clamp(best));
        }

        return scores;
    }

    public Intent toIntent(String input) {
        Map<String, Double> scores = extractScores(input);
        return new Intent(
            scores.getOrDefault("cost", 0.5),
            scores.getOrDefault("latency", 0.5),
            scores.getOrDefault("security", 0.5),
            scores.getOrDefault("carbon", 0.3)
        );
    }

    private Map<String, List<double[]>> precomputeEmbeddings() {
        Map<String, List<double[]>> embeddings = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : anchors.entrySet()) {
            List<double[]> vectors = new ArrayList<>();
            for (String phrase : entry.getValue()) {
                vectors.add(encoder.encode(phrase));
            }
            embeddings.put(entry.getKey(), vectors);
        }
        return embeddings;
    }

    private Map<String, List<String>> createAnchors() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("cost", Arrays.asList(
            "cheap", "low cost", "affordable", "budget friendly",
            "economical", "save money", "cost effective"
        ));
        map.put("latency", Arrays.asList(
            "fast", "low latency", "quick response", "real time",
            "high performance", "responsive"
        ));
        map.put("security", Arrays.asList(
            "secure", "safe", "protected", "encrypted",
            "compliant", "private"
        ));
        map.put("carbon", Arrays.asList(
            "green", "sustainable", "low carbon", "eco friendly",
            "renewable energy", "carbon neutral"
        ));
        return map;
    }

    private double applyNegationHeuristics(String dimension, String text, double current) {
        if (dimension.equals("cost")) {
            if (containsAny(text, "not expensive", "not costly", "without high cost")) {
                current = Math.max(current, 0.78);
            }
            if (containsAny(text, "not cheap", "expensive is fine", "money is no object")) {
                current = Math.min(current, 0.25);
            }
        }

        if (dimension.equals("latency")) {
            if (containsAny(text, "not slow", "must be fast", "no lag")) {
                current = Math.max(current, 0.80);
            }
            if (containsAny(text, "latency doesn't matter", "speed doesn't matter", "not fast")) {
                current = Math.min(current, 0.20);
            }
        }

        if (dimension.equals("security")) {
            if (containsAny(text, "not insecure", "must be secure", "highly secure")) {
                current = Math.max(current, 0.82);
            }
        }

        if (dimension.equals("carbon")) {
            if (containsAny(text, "eco friendly", "environment friendly", "reduce carbon")) {
                current = Math.max(current, 0.75);
            }
        }

        return current;
    }

    private boolean containsAny(String text, String... options) {
        for (String option : options) {
            if (text.contains(option)) return true;
        }
        return false;
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
