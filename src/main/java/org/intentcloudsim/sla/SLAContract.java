package org.intentcloudsim.sla;

/**
 * Represents a Service Level Agreement generated from user intent.
 *
 * PATENT CLAIM: Automatically generated SLA contracts from
 * parsed natural language intent vectors.
 */
public class SLAContract {

    private double maxLatencyMs;        // Maximum allowed latency in ms
    private double maxCostPerHour;      // Maximum cost per hour in dollars
    private double minAvailability;     // Minimum uptime percentage (e.g., 99.9)
    private double minSecurityLevel;    // Security level 0-10
    private double maxCarbonGrams;      // Max carbon emission per hour
    private boolean isAccepted;         // Whether both parties accepted
    private String contractId;          // Unique identifier
    private long createdAt;             // When contract was created

    public SLAContract(double maxLatencyMs, double maxCostPerHour,
                       double minAvailability, double minSecurityLevel,
                       double maxCarbonGrams) {
        this.maxLatencyMs = maxLatencyMs;
        this.maxCostPerHour = maxCostPerHour;
        this.minAvailability = minAvailability;
        this.minSecurityLevel = minSecurityLevel;
        this.maxCarbonGrams = maxCarbonGrams;
        this.isAccepted = false;
        this.contractId = "SLA-" + System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Simple constructor for basic SLA.
     */
    public SLAContract(double maxLatencyMs, double maxCostPerHour) {
        this(maxLatencyMs, maxCostPerHour, 99.0, 5.0, 100.0);
    }

    /**
     * Check if actual performance meets SLA requirements.
     */
    public boolean isSatisfied(double actualLatency, double actualCost,
                                double actualAvailability) {
        boolean latencyOk = actualLatency <= maxLatencyMs;
        boolean costOk = actualCost <= maxCostPerHour;
        boolean availOk = actualAvailability >= minAvailability;

        System.out.println("[SLA Check] Latency: " + actualLatency
            + " <= " + maxLatencyMs + " ? " + latencyOk);
        System.out.println("[SLA Check] Cost: " + actualCost
            + " <= " + maxCostPerHour + " ? " + costOk);
        System.out.println("[SLA Check] Availability: " + actualAvailability
            + " >= " + minAvailability + " ? " + availOk);

        return latencyOk && costOk && availOk;
    }

    /**
     * Calculate penalty if SLA is violated.
     */
    public double calculatePenalty(double actualLatency,
                                    double actualCost) {
        double penalty = 0;

        if (actualLatency > maxLatencyMs) {
            // Penalty proportional to how much latency exceeded
            penalty += (actualLatency - maxLatencyMs) / maxLatencyMs * 10.0;
        }
        if (actualCost > maxCostPerHour) {
            penalty += (actualCost - maxCostPerHour) / maxCostPerHour * 10.0;
        }

        return penalty;
    }

    public void accept() { this.isAccepted = true; }

    // ===== GETTERS =====
    public double getMaxLatencyMs() { return maxLatencyMs; }
    public double getMaxCostPerHour() { return maxCostPerHour; }
    public double getMinAvailability() { return minAvailability; }
    public double getMinSecurityLevel() { return minSecurityLevel; }
    public double getMaxCarbonGrams() { return maxCarbonGrams; }
    public boolean isAccepted() { return isAccepted; }
    public String getContractId() { return contractId; }

    @Override
    public String toString() {
        return String.format(
            "SLAContract[id=%s, maxLatency=%.1fms, maxCost=$%.2f/hr, " +
            "minAvail=%.1f%%, security=%.1f, accepted=%s]",
            contractId, maxLatencyMs, maxCostPerHour,
            minAvailability, minSecurityLevel, isAccepted
        );
    }
}

