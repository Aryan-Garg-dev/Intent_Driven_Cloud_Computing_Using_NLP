package org.intentcloudsim.intent;

import java.util.*;

/**
 * Lightweight sentence encoder that mimics Sentence-BERT style
 * semantic behavior using local hashed embeddings.
 *
 * NOTE: This is intentionally dependency-light and can be replaced by a true
 * SBERT backend later without changing callers.
 */
public class SentenceBertLikeEncoder {

    private static final int VECTOR_SIZE = 256;

    private static final Map<String, String> SYNONYM_CANONICAL = new HashMap<>();

    static {
        // Cost
        SYNONYM_CANONICAL.put("cheap", "cost");
        SYNONYM_CANONICAL.put("affordable", "cost");
        SYNONYM_CANONICAL.put("budget", "cost");
        SYNONYM_CANONICAL.put("economical", "cost");
        SYNONYM_CANONICAL.put("inexpensive", "cost");
        SYNONYM_CANONICAL.put("expensive", "high_cost");

        // Latency/performance
        SYNONYM_CANONICAL.put("fast", "latency");
        SYNONYM_CANONICAL.put("quick", "latency");
        SYNONYM_CANONICAL.put("rapid", "latency");
        SYNONYM_CANONICAL.put("responsive", "latency");
        SYNONYM_CANONICAL.put("realtime", "latency");
        SYNONYM_CANONICAL.put("slow", "high_latency");

        // Security
        SYNONYM_CANONICAL.put("secure", "security");
        SYNONYM_CANONICAL.put("safe", "security");
        SYNONYM_CANONICAL.put("protected", "security");
        SYNONYM_CANONICAL.put("encrypted", "security");
        SYNONYM_CANONICAL.put("private", "security");

        // Carbon
        SYNONYM_CANONICAL.put("green", "carbon");
        SYNONYM_CANONICAL.put("sustainable", "carbon");
        SYNONYM_CANONICAL.put("eco", "carbon");
        SYNONYM_CANONICAL.put("renewable", "carbon");
    }

    public double[] encode(String text) {
        double[] vector = new double[VECTOR_SIZE];
        if (text == null || text.isBlank()) {
            return vector;
        }

        List<String> tokens = tokenize(text.toLowerCase(Locale.ROOT));
        if (tokens.isEmpty()) {
            return vector;
        }

        // unigram features
        for (String token : tokens) {
            addFeature(vector, token, 1.0);

            String canonical = SYNONYM_CANONICAL.get(token);
            if (canonical != null) {
                addFeature(vector, canonical, 0.8);
            }
        }

        // bigram features for phrase semantics
        for (int i = 0; i < tokens.size() - 1; i++) {
            String bigram = tokens.get(i) + "_" + tokens.get(i + 1);
            addFeature(vector, bigram, 0.9);
        }

        normalize(vector);
        return vector;
    }

    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void addFeature(double[] vector, String feature, double weight) {
        int idx = Math.floorMod(feature.hashCode(), VECTOR_SIZE);
        vector[idx] += weight;
    }

    private List<String> tokenize(String text) {
        String cleaned = text.replaceAll("[^a-z0-9\\s]", " ").trim();
        if (cleaned.isBlank()) {
            return Collections.emptyList();
        }

        String[] raw = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String token : raw) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private void normalize(double[] vector) {
        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm == 0) return;

        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }
}
