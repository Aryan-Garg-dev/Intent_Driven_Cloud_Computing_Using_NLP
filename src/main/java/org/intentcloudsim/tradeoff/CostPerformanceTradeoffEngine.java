package org.intentcloudsim.tradeoff;

import org.intentcloudsim.intent.Intent;
import org.intentcloudsim.sla.SLAContract;

/**
 * PATENT IDEA 18: Cost vs Performance Tradeoff Based on Intent
 *
 * Calculates optimal resource configurations by balancing
 * cost against performance based on user intent.
 *
 * NOVELTY: Intent-weighted multi-objective optimization for
 * cloud resource selection.
 */
public class CostPerformanceTradeoffEngine {

    /**
     * Score a resource configuration based on how well it
     * matches the user's intent.
     *
     * Higher score = better match.
     *
     * @param cost The cost of this configuration ($/hr)
     * @param latency The latency of this configuration (ms)
     * @param securityLevel Security level (0-10)
     * @param carbonEmission Carbon emission (grams/hr)
     * @param intent The user's intent
     * @return A score (higher = better match)
     */
    public double score(double cost, double latency,
                        double securityLevel, double carbonEmission,
                        Intent intent) {

        // Avoid division by zero
        if (cost <= 0) cost = 0.01;
        if (latency <= 0) latency = 0.01;

        // Cost component: lower cost → higher score
        // Weighted by how much user cares about cost
        double costScore = intent.getCostPriority() * (1.0 / cost) * 10.0;

        // Latency component: lower latency → higher score
        double latencyScore = intent.getLatencyPriority() *
                              (1.0 / latency) * 1000.0;

        // Security component: higher security → higher score
        double securityScore = intent.getSecurityPriority() *
                               securityLevel;

        // Carbon component: lower carbon → higher score
        double carbonScore = intent.getCarbonPriority() *
                             (1.0 / Math.max(carbonEmission, 0.01)) * 100.0;

        double totalScore = costScore + latencyScore +
                            securityScore + carbonScore;

        return totalScore;
    }

    /**
     * Simplified score with just cost and latency.
     */
    public double score(double cost, double latency, Intent intent) {
        return score(cost, latency, 5.0, 50.0, intent);
    }

    /**
     * Find the best option from multiple candidates.
     */
    public int findBestOption(double[] costs, double[] latencies,
                               Intent intent) {
        int bestIndex = 0;
        double bestScore = -1;

        System.out.println("\n===== TRADEOFF ANALYSIS =====");

        for (int i = 0; i < costs.length; i++) {
            double s = score(costs[i], latencies[i], intent);
            System.out.printf("[Tradeoff] Option %d: cost=$%.2f, " +
                            "latency=%.1fms → score=%.2f%n",
                            i, costs[i], latencies[i], s);

            if (s > bestScore) {
                bestScore = s;
                bestIndex = i;
            }
        }

        System.out.println("[Tradeoff] BEST: Option " + bestIndex +
                           " (score=" + String.format("%.2f", bestScore) + ")");
        System.out.println("=============================\n");

        return bestIndex;
    }

    /**
     * Check if a configuration satisfies the SLA.
     */
    public boolean meetsSLA(double cost, double latency,
                            SLAContract sla) {
        return cost <= sla.getMaxCostPerHour() &&
               latency <= sla.getMaxLatencyMs();
    }

    /**
     * Calculate Pareto efficiency between cost and performance.
     */
    public double paretoScore(double cost, double latency) {
        // Normalize both to 0-1 range
        double normalizedCost = 1.0 / (1.0 + cost);
        double normalizedLatency = 1.0 / (1.0 + latency / 100.0);

        // Pareto = geometric mean (balanced optimization)
        return Math.sqrt(normalizedCost * normalizedLatency);
    }
}

