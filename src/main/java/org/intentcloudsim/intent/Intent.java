package org.intentcloudsim.intent;

/**
 * Represents a user's intent for cloud resource allocation.
 * Each priority is a value between 0.0 (don't care) and 1.0 (very important).
 *
 * PATENT CLAIM: This class converts human desires into machine-readable
 * vectors for cloud virtualization decisions.
 */
public class Intent {

    // How much the user cares about low cost (0.0 to 1.0)
    private double costPriority;

    // How much the user cares about fast response (0.0 to 1.0)
    private double latencyPriority;

    // How much the user cares about security (0.0 to 1.0)
    private double securityPriority;

    // How much the user cares about green/carbon footprint (0.0 to 1.0)
    private double carbonPriority;

    // Timestamp when this intent was created
    private long timestamp;

    // Which user created this intent
    private String userId;

    /**
     * Create a new Intent with all priorities.
     */
    public Intent(double costPriority, double latencyPriority,
                  double securityPriority, double carbonPriority) {
        this.costPriority = clamp(costPriority);
        this.latencyPriority = clamp(latencyPriority);
        this.securityPriority = clamp(securityPriority);
        this.carbonPriority = clamp(carbonPriority);
        this.timestamp = System.currentTimeMillis();
        this.userId = "default";
    }

    /**
     * Simple constructor with just cost, latency, security.
     */
    public Intent(double costPriority, double latencyPriority,
                  double securityPriority) {
        this(costPriority, latencyPriority, securityPriority, 0.3);
    }

    /**
     * Default intent - balanced priorities.
     */
    public Intent() {
        this(0.5, 0.5, 0.5, 0.3);
    }

    /**
     * Make sure value is between 0.0 and 1.0
     */
    private double clamp(double value) {
        if (value < 0.0) return 0.0;
        if (value > 1.0) return 1.0;
        return value;
    }

    // ===== GETTERS =====
    public double getCostPriority() { return costPriority; }
    public double getLatencyPriority() { return latencyPriority; }
    public double getSecurityPriority() { return securityPriority; }
    public double getCarbonPriority() { return carbonPriority; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }

    // ===== SETTERS =====
    public void setUserId(String userId) { this.userId = userId; }
    public void setCostPriority(double v) { this.costPriority = clamp(v); }
    public void setLatencyPriority(double v) { this.latencyPriority = clamp(v); }
    public void setSecurityPriority(double v) { this.securityPriority = clamp(v); }
    public void setCarbonPriority(double v) { this.carbonPriority = clamp(v); }

    @Override
    public String toString() {
        return String.format(
            "Intent[cost=%.2f, latency=%.2f, security=%.2f, carbon=%.2f, user=%s]",
            costPriority, latencyPriority, securityPriority, carbonPriority, userId
        );
    }
}

