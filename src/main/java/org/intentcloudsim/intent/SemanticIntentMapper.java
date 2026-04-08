package org.intentcloudsim.intent;

import java.util.*;

/**
 * Semantic intent mapper using sentence embeddings + anchor similarity.
 */
public class SemanticIntentMapper {

    private record AnchorPhrase(String phrase, double weight) {}
    private record AnchorEmbedding(String phrase, double[] vector, double weight) {}

    public record DomainContext(List<String> matchedDomains,
                                Map<String, Double> dimensionBoosts) {}

    private final SentenceBertLikeEncoder encoder;
    private final Map<String, List<AnchorPhrase>> anchors;
    private final Map<String, List<AnchorEmbedding>> anchorEmbeddings;
    private final Map<String, List<String>> domainTriggers;
    private final Map<String, Map<String, Double>> domainDimensionBoosts;

    public SemanticIntentMapper() {
        this.encoder = new SentenceBertLikeEncoder();
        this.anchors = createAnchors();
        this.anchorEmbeddings = precomputeEmbeddings();
        this.domainTriggers = createDomainTriggers();
        this.domainDimensionBoosts = createDomainDimensionBoosts();
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
        DomainContext domainContext = analyzeDomainContext(text);

        for (Map.Entry<String, List<AnchorEmbedding>> entry : anchorEmbeddings.entrySet()) {
            String dim = entry.getKey();
            double best = 0;
            for (AnchorEmbedding anchorEmbedding : entry.getValue()) {
                double semanticMatch = encoder.cosineSimilarity(sentenceEmbedding, anchorEmbedding.vector())
                    * anchorEmbedding.weight();
                best = Math.max(best, semanticMatch);
            }

            // Soft lexical fallback to avoid brittle behavior on short phrases.
            for (AnchorPhrase anchor : anchors.get(dim)) {
                if (text.contains(anchor.phrase())) {
                    double lexicalFloor = 0.70 + (0.25 * anchor.weight());
                    best = Math.max(best, lexicalFloor);
                }
            }

            best = applyNegationHeuristics(dim, text, best);
            double domainBoost = domainContext.dimensionBoosts().getOrDefault(dim, 0.0);
            best = applyDomainBoost(best, domainBoost);
            scores.put(dim, clamp(best));
        }

        return scores;
    }

    public DomainContext analyzeDomainContext(String input) {
        if (input == null || input.isBlank()) {
            return new DomainContext(List.of(), Map.of());
        }

        String text = input.toLowerCase(Locale.ROOT).trim();
        List<String> matchedDomains = new ArrayList<>();
        Map<String, Double> accumulatedBoosts = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : domainTriggers.entrySet()) {
            String domain = entry.getKey();
            if (!containsAny(text, entry.getValue().toArray(new String[0]))) {
                continue;
            }

            matchedDomains.add(domain);
            Map<String, Double> domainBoosts = domainDimensionBoosts.getOrDefault(domain, Map.of());
            for (Map.Entry<String, Double> boost : domainBoosts.entrySet()) {
                accumulatedBoosts.merge(boost.getKey(), boost.getValue(), Double::sum);
            }
        }

        return new DomainContext(List.copyOf(matchedDomains), accumulatedBoosts);
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

    private Map<String, List<AnchorEmbedding>> precomputeEmbeddings() {
        Map<String, List<AnchorEmbedding>> embeddings = new HashMap<>();
        for (Map.Entry<String, List<AnchorPhrase>> entry : anchors.entrySet()) {
            List<AnchorEmbedding> vectors = new ArrayList<>();
            for (AnchorPhrase phrase : entry.getValue()) {
                vectors.add(new AnchorEmbedding(
                    phrase.phrase(),
                    encoder.encode(phrase.phrase()),
                    phrase.weight()
                ));
            }
            embeddings.put(entry.getKey(), vectors);
        }
        return embeddings;
    }

    private Map<String, List<AnchorPhrase>> createAnchors() {
        Map<String, List<AnchorPhrase>> map = new HashMap<>();
        map.put("cost", Arrays.asList(
            ap("cheap", 0.98), ap("low cost", 1.00), ap("affordable", 0.90), ap("budget friendly", 0.95),
            ap("economical", 0.88), ap("save money", 0.94), ap("cost effective", 0.92), ap("cost efficient", 0.92),
            ap("minimize spend", 1.00), ap("reduce spend", 0.96), ap("optimize spend", 0.96), ap("cost control", 0.94),
            ap("budget cap", 0.96), ap("tight budget", 0.98), ap("stay within budget", 0.96), ap("price sensitive", 0.92),
            ap("lowest price", 0.98), ap("lowest cost", 1.00), ap("value for money", 0.88), ap("finops", 0.95),
            ap("cost optimization", 0.95), ap("optimize cloud bill", 0.95), ap("billing optimization", 0.92),
            ap("opex reduction", 0.90), ap("tco reduction", 0.92), ap("capex avoidance", 0.84), ap("rightsizing", 0.90),
            ap("spot instances", 0.80), ap("reserved instances", 0.82), ap("savings plan", 0.82)
        ));
        map.put("latency", Arrays.asList(
            ap("fast", 0.96), ap("low latency", 1.00), ap("quick response", 0.96), ap("real time", 0.96),
            ap("high performance", 0.95), ap("responsive", 0.92), ap("instant response", 0.98),
            ap("ultra low latency", 1.00), ap("near realtime", 0.95), ap("interactive", 0.90), ap("snappy", 0.88),
            ap("high throughput", 0.90), ap("performance critical", 0.96), ap("performance sensitive", 0.94),
            ap("p99 latency", 0.98), ap("p95 latency", 0.94), ap("tail latency", 0.96), ap("jitter", 0.92),
            ap("frame time", 0.92), ap("high fps", 0.96), ap("multiplayer gaming", 0.98), ap("gaming server", 0.98),
            ap("tick rate", 0.96), ap("low lag", 0.98), ap("no lag", 0.98), ap("streaming quality", 0.90),
            ap("high concurrency", 0.88), ap("burst traffic", 0.86), ap("autoscaling performance", 0.90)
        ));
        map.put("security", Arrays.asList(
            ap("secure", 0.96), ap("security", 0.98), ap("high security", 1.00), ap("very high security", 1.00),
            ap("enterprise security", 0.98), ap("zero trust", 1.00), ap("defense in depth", 0.96),
            ap("safe", 0.84), ap("protected", 0.90), ap("encrypted", 0.94), ap("encryption at rest", 0.96),
            ap("encryption in transit", 0.96), ap("compliant", 0.90), ap("compliance", 0.92), ap("private", 0.88),
            ap("data protection", 0.95), ap("data privacy", 0.95), ap("sensitive data", 0.94), ap("confidential", 0.92),
            ap("iam", 0.92), ap("least privilege", 0.96), ap("role based access", 0.92), ap("rbac", 0.92),
            ap("mfa", 0.92), ap("key management", 0.94), ap("kms", 0.92), ap("hsm", 0.90), ap("audit logging", 0.92),
            ap("threat detection", 0.90), ap("waf", 0.90), ap("ddos protection", 0.94), ap("network isolation", 0.96),
            ap("private subnet", 0.92), ap("segmentation", 0.92), ap("soc2", 0.96), ap("iso 27001", 0.96),
            ap("pci dss", 0.98), ap("hipaa", 0.98), ap("gdpr", 0.98)
        ));
        map.put("carbon", Arrays.asList(
            ap("green", 0.96), ap("sustainable", 0.96), ap("low carbon", 1.00), ap("eco friendly", 0.94),
            ap("renewable energy", 1.00), ap("carbon neutral", 1.00), ap("net zero", 1.00),
            ap("energy efficient", 0.96), ap("power efficient", 0.92), ap("reduce emissions", 0.96),
            ap("carbon footprint", 0.96), ap("low power", 0.90), ap("sustainability goals", 0.90),
            ap("green datacenter", 0.96), ap("renewable powered", 0.92), ap("esg", 0.90), ap("climate impact", 0.88)
        ));
        return map;
    }

    private Map<String, List<String>> createDomainTriggers() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("gaming", Arrays.asList(
            "gaming", "multiplayer", "matchmaking", "tick rate", "fps", "latency", "lag"
        ));
        map.put("fintech", Arrays.asList(
            "banking", "payments", "trading", "pci", "fraud", "financial", "fintech"
        ));
        map.put("healthcare", Arrays.asList(
            "healthcare", "ehr", "emr", "patient", "hipaa", "clinical"
        ));
        map.put("analytics", Arrays.asList(
            "batch", "analytics", "etl", "warehouse", "reporting", "overnight"
        ));
        map.put("sustainability", Arrays.asList(
            "sustainable", "green", "carbon", "esg", "net zero", "renewable"
        ));
        return map;
    }

    private Map<String, Map<String, Double>> createDomainDimensionBoosts() {
        Map<String, Map<String, Double>> map = new HashMap<>();

        map.put("gaming", Map.of(
            "latency", 0.18,
            "security", 0.04
        ));
        map.put("fintech", Map.of(
            "security", 0.22,
            "latency", 0.07,
            "cost", 0.03
        ));
        map.put("healthcare", Map.of(
            "security", 0.20,
            "cost", 0.04
        ));
        map.put("analytics", Map.of(
            "cost", 0.14,
            "carbon", 0.05
        ));
        map.put("sustainability", Map.of(
            "carbon", 0.24,
            "cost", 0.04
        ));

        return map;
    }

    private AnchorPhrase ap(String phrase, double weight) {
        return new AnchorPhrase(phrase, clamp(weight));
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
            if (containsAny(text, "not insecure", "must be secure", "highly secure", "high security", "very high security")) {
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

    private double applyDomainBoost(double score, double boost) {
        if (boost <= 0.0) return score;
        double boundedBoost = Math.min(0.35, boost);
        return score + (boundedBoost * (1.0 - score));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
