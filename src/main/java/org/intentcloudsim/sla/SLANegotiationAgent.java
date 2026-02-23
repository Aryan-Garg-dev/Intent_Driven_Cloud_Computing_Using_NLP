package org.intentcloudsim.sla;

import org.intentcloudsim.intent.Intent;

/**
 * PATENT IDEA 17: Intent-Aware SLA Negotiation
 *
 * Automatically negotiates SLA contracts based on user intent.
 * The agent translates intent priorities into concrete SLA parameters.
 *
 * NOVELTY: No existing system auto-generates SLA terms from
 * natural language intent analysis.
 */
public class SLANegotiationAgent {

    // Provider's constraints (what the cloud provider can offer)
    private double providerMinLatency = 10.0;    // ms - best possible
    private double providerMaxLatency = 200.0;   // ms - worst acceptable
    private double providerMinCost = 0.50;       // $/hr - cheapest tier
    private double providerMaxCost = 20.0;       // $/hr - premium tier
    private double providerBaseAvailability = 95.0; // % - base level

    /**
     * Negotiate an SLA contract based on user intent.
     *
     * Higher latency priority → stricter latency requirements
     * Higher cost priority → lower cost limits
     * These can conflict, so negotiation finds a balance.
     */
    public SLAContract negotiate(Intent intent) {
        System.out.println("\n===== SLA NEGOTIATION =====");
        System.out.println("[Negotiation] User intent: " + intent);

        // === LATENCY NEGOTIATION ===
        // High latency priority → low max latency (strict)
        double maxLatency = providerMaxLatency -
            (intent.getLatencyPriority() *
             (providerMaxLatency - providerMinLatency));

        // === COST NEGOTIATION ===
        // High cost priority → low max cost (budget-friendly)
        double maxCost = providerMaxCost -
            (intent.getCostPriority() *
             (providerMaxCost - providerMinCost));

        // === CONFLICT RESOLUTION ===
        // User wants BOTH cheap AND fast? That's hard.
        // If both priorities are high, we need to compromise.
        if (intent.getCostPriority() > 0.7 &&
            intent.getLatencyPriority() > 0.7) {
            System.out.println("[Negotiation] CONFLICT: User wants cheap AND fast");
            System.out.println("[Negotiation] Compromising...");

            // Relax both slightly
            maxLatency *= 1.2;  // Allow 20% more latency
            maxCost *= 1.15;    // Allow 15% more cost
        }

        // === AVAILABILITY ===
        // Higher security → higher availability requirement
        double minAvailability = providerBaseAvailability +
            (intent.getSecurityPriority() * 4.9); // up to 99.9%

        // === SECURITY LEVEL ===
        double securityLevel = intent.getSecurityPriority() * 10.0;

        // === CARBON ===
        double maxCarbon = 100.0 - (intent.getCarbonPriority() * 80.0);

        // Create the contract
        SLAContract contract = new SLAContract(
            maxLatency, maxCost, minAvailability,
            securityLevel, maxCarbon
        );

        // Provider auto-accepts if terms are within range
        if (maxLatency >= providerMinLatency &&
            maxCost >= providerMinCost) {
            contract.accept();
            System.out.println("[Negotiation] Contract ACCEPTED by provider");
        } else {
            System.out.println("[Negotiation] Contract REJECTED - " +
                             "terms too strict for provider");
        }

        System.out.println("[Negotiation] Final SLA: " + contract);
        System.out.println("===========================\n");

        return contract;
    }

    /**
     * Re-negotiate if SLA is being violated.
     */
    public SLAContract renegotiate(SLAContract currentSla,
                                    double currentLatency,
                                    double currentCost) {
        System.out.println("[Negotiation] Re-negotiating SLA...");

        double newMaxLatency = currentSla.getMaxLatencyMs();
        double newMaxCost = currentSla.getMaxCostPerHour();

        // If latency is being violated, relax it
        if (currentLatency > currentSla.getMaxLatencyMs()) {
            newMaxLatency = currentLatency * 1.1; // 10% buffer
            System.out.println("[Negotiation] Relaxed latency to: "
                               + newMaxLatency);
        }

        // If cost is being violated, increase budget
        if (currentCost > currentSla.getMaxCostPerHour()) {
            newMaxCost = currentCost * 1.1;
            System.out.println("[Negotiation] Relaxed cost to: "
                               + newMaxCost);
        }

        SLAContract newContract = new SLAContract(newMaxLatency, newMaxCost);
        newContract.accept();
        return newContract;
    }
}

