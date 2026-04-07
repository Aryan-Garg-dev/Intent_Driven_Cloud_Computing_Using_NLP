package org.intentcloudsim.ui.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Cloud Configuration Model
 * Represents user's infrastructure configuration
 */
public class CloudConfig {

    public String userId;
    public String userIntent;
    public boolean intentValidated;
    public String intentValidationMessage;
    public double parserConfidence;
    public String dominantPriority;

    public boolean rlApplied;
    public double systemLoad;
    public double rlReward;
    public double rlLatencyImprovementPercent;
    public double rlCostImprovementPercent;
    public double rlScoreImprovementPercent;
    public int rlRunCount;
    public double rlMaturityScore;
    public double rlHistoricalLatencyImprovementPercent;
    public double rlHistoricalCostImprovementPercent;
    public double rlHistoricalScoreImprovementPercent;
    public double rlHistoricalAvgReward;
    public double rlHistoricalRewardTrend;
    public double rlHistoricalSlaSuccessRate;

    public double parsedCostPriority;
    public double parsedLatencyPriority;
    public double parsedSecurityPriority;
    public double parsedCarbonPriority;

    public double refinedCostPriority;
    public double refinedLatencyPriority;
    public double refinedSecurityPriority;
    public double refinedCarbonPriority;

    public double costPriority;
    public double latencyPriority;
    public double securityPriority;
    public double carbonPriority;
    
    // SLA Parameters
    public double maxLatencyMs;
    public double maxCostPerHour;
    public double minAvailabilityPercent;
    public String securityLevel;  // LOW, MEDIUM, HIGH
    public int recommendedOptionIndex;
    public double[] candidateCosts;
    public double[] candidateLatencies;
    public double selectedCostPerHour;
    public double selectedLatencyMs;
    public double tradeoffScore;
    
    // Infrastructure Configuration
    public int numHosts;
    public int hostsPerDatacenter;
    public int numVMs;
    public int vmsPerHost;
    public int numCloudlets;
    public int cloudletsPerVM;
    
    // Resource Specifications
    public int hostCores;
    public long hostRamGb;
    public long hostStorageGb;
    public int vmCores;
    public long vmRamGb;
    
    // Placement Strategy
    public String vmPlacementPolicy;  // CONSOLIDATED, SPREAD, ISOLATED
    public String datacenterLocation;  // LOCAL, REGIONAL, GLOBAL
    public boolean greenDatacenter;
    
    // Simulation Parameters
    public long simulationTimeoutSeconds;
    public int numExperiments;

    public CloudConfig() {
        // Defaults
        this.userId = "user_default";
        this.userIntent = "";
        this.intentValidated = false;
        this.intentValidationMessage = "Provide a complete infrastructure intent.";
        this.parserConfidence = 0.0;
        this.dominantPriority = "Unknown";

        this.rlApplied = false;
        this.systemLoad = 0.5;
        this.rlReward = 0.0;
    this.rlLatencyImprovementPercent = 0.0;
    this.rlCostImprovementPercent = 0.0;
    this.rlScoreImprovementPercent = 0.0;
    this.rlRunCount = 0;
    this.rlMaturityScore = 0.0;
    this.rlHistoricalLatencyImprovementPercent = 0.0;
    this.rlHistoricalCostImprovementPercent = 0.0;
    this.rlHistoricalScoreImprovementPercent = 0.0;
    this.rlHistoricalAvgReward = 0.0;
    this.rlHistoricalRewardTrend = 0.0;
    this.rlHistoricalSlaSuccessRate = 0.0;

        this.parsedCostPriority = 0.5;
        this.parsedLatencyPriority = 0.5;
        this.parsedSecurityPriority = 0.3;
        this.parsedCarbonPriority = 0.2;

        this.refinedCostPriority = 0.5;
        this.refinedLatencyPriority = 0.5;
        this.refinedSecurityPriority = 0.3;
        this.refinedCarbonPriority = 0.2;

        this.costPriority = 0.5;
        this.latencyPriority = 0.5;
        this.securityPriority = 0.3;
        this.carbonPriority = 0.2;
        
        this.maxLatencyMs = 100.0;
        this.maxCostPerHour = 5.0;
        this.minAvailabilityPercent = 99.5;
        this.securityLevel = "MEDIUM";
        this.recommendedOptionIndex = 1;
        this.candidateCosts = new double[] {2.0, 4.0, 6.0, 8.0};
        this.candidateLatencies = new double[] {180.0, 130.0, 95.0, 70.0};
        this.selectedCostPerHour = this.candidateCosts[this.recommendedOptionIndex];
        this.selectedLatencyMs = this.candidateLatencies[this.recommendedOptionIndex];
        this.tradeoffScore = 0.0;
        
        this.numHosts = 4;
        this.hostsPerDatacenter = 4;
        this.numVMs = 4;
        this.vmsPerHost = 1;
        this.numCloudlets = 8;
        this.cloudletsPerVM = 1;
        
        this.hostCores = 8;
        this.hostRamGb = 16;
        this.hostStorageGb = 1000;
        this.vmCores = 2;
        this.vmRamGb = 4;
        
        this.vmPlacementPolicy = "SPREAD";
        this.datacenterLocation = "LOCAL";
        this.greenDatacenter = false;
        
        this.simulationTimeoutSeconds = 300;
        this.numExperiments = 1;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public static CloudConfig fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CloudConfig.class);
    }

    @Override
    public String toString() {
        return "CloudConfig{" +
                "userId='" + userId + '\'' +
                ", costPriority=" + costPriority +
                ", latencyPriority=" + latencyPriority +
                ", securityPriority=" + securityPriority +
                ", carbonPriority=" + carbonPriority +
                ", numHosts=" + numHosts +
                ", numVMs=" + numVMs +
                '}';
    }
}
