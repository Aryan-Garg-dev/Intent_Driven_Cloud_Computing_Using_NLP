package org.intentcloudsim.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.intentcloudsim.api.IntentPipelineService;
import org.intentcloudsim.intent.Intent;
import org.intentcloudsim.intent.NaturalLanguageIntentParser;
import org.intentcloudsim.sla.SLAContract;
import org.intentcloudsim.ui.models.CloudConfig;

import java.util.Locale;

/**
 * Tab 1: Input natural language intent and receive parsed results with suggestions
 */
public class IntentInputTab extends VBox {

    private TextArea inputArea;
    private TextArea resultArea;
    private Label confidenceLabel;
    private Label dominantLabel;
    private TextArea suggestionsArea;
    private CloudConfig currentConfig;
    private final IntentPipelineService pipelineService;

    public IntentInputTab() {
        setPadding(new Insets(15));
        setSpacing(10);
        currentConfig = new CloudConfig();
        pipelineService = new IntentPipelineService();

        // Split into input/output
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5);
        
        // Left side: Input and parse button
        VBox inputSection = createInputSection();
        
        // Right side: Results and suggestions
        VBox outputSection = createOutputSection();
        
        splitPane.getItems().addAll(inputSection, outputSection);
        
        getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
    }

    /**
     * Create input section with text area and parse button
     */
    private VBox createInputSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        Label label = new Label("📝 Enter Your Cloud Infrastructure Intent");
        label.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        inputArea = new TextArea();
        inputArea.setWrapText(true);
        inputArea.setPrefHeight(150);
        inputArea.setStyle("-fx-font-size: 11; -fx-font-family: 'Segoe UI';");
        inputArea.setText("I need fast, secure servers for banking applications at affordable cost");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button parseButton = new Button("🔍 Parse Intent");
        parseButton.setStyle("-fx-font-size: 11; -fx-padding: 10 20; -fx-text-fill: white; -fx-background-color: #3498db;");
        parseButton.setPrefWidth(140);
        parseButton.setOnAction(e -> parseAndAnalyze());

        Button exampleButton = new Button("📋 Example");
        exampleButton.setStyle("-fx-font-size: 11; -fx-padding: 10 15; -fx-background-color: #95a5a6;");
        exampleButton.setOnAction(e -> loadExample());

        Button clearButton = new Button("✕ Clear");
        clearButton.setStyle("-fx-font-size: 11; -fx-padding: 10 15; -fx-background-color: #95a5a6;");
        clearButton.setOnAction(e -> inputArea.clear());

        buttonBox.getChildren().addAll(parseButton, exampleButton, clearButton);

        box.getChildren().addAll(label, inputArea, buttonBox);
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        return box;
    }

    /**
     * Create output section with parsing results and suggestions
     */
    private VBox createOutputSection() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        // Metrics row
        HBox metricsBox = new HBox(15);
        metricsBox.setPadding(new Insets(10));
        metricsBox.setStyle("-fx-border-color: #d0d0d0; -fx-background-color: #f5f5f5; -fx-border-radius: 3;");

        confidenceLabel = new Label("Confidence: 0%");
        confidenceLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #0066cc;");

        dominantLabel = new Label("Dominant: None");
        dominantLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);
        metricsBox.getChildren().addAll(confidenceLabel, sep, dominantLabel);

        // Parsing results
        Label resultLabel = new Label("🎯 Parsed Intent Details");
        resultLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

        resultArea = new TextArea();
        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);
        resultArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10;");
        resultArea.setText("Results will appear here...");

        // Suggestions section
        Label suggestLabel = new Label("💡 Suggestions & SLA Recommendations");
        suggestLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 10 0 0 0;");

        suggestionsArea = new TextArea();
        suggestionsArea.setWrapText(true);
        suggestionsArea.setEditable(false);
        suggestionsArea.setPrefHeight(180);
        suggestionsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10; " +
                                  "-fx-control-inner-background: #f0f8f0;");
        suggestionsArea.setText("Suggestions will appear here...");

        VBox.setVgrow(resultArea, Priority.ALWAYS);
        VBox.setVgrow(suggestionsArea, Priority.ALWAYS);

        box.getChildren().addAll(metricsBox, resultLabel, resultArea, suggestLabel, suggestionsArea);
        return box;
    }

    /**
     * Parse the input text and analyze intent
     */
    private void parseAndAnalyze() {
        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            resultArea.setText("Please enter an intent statement.");
            return;
        }

        if (!isIntentDetailedEnough(input)) {
            String guidance = "Your intent looks too short/incomplete. Please provide a complete requirement, e.g.\n"
                + "- workload type (web app, gaming, analytics)\n"
                + "- priority (cost / latency / security / green)\n"
                + "- constraints (budget, latency target, compliance)";
            currentConfig.intentValidated = false;
            currentConfig.intentValidationMessage = guidance;
            resultArea.setText(guidance);
            suggestionsArea.setText("No recommendation generated yet. Please provide a more complete intent.");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Need More Detail");
                alert.setHeaderText("Please write a more complete intent");
                alert.setContentText(guidance);
                alert.showAndWait();
            });
            return;
        }

        Thread thread = new Thread(() -> {
            try {
                String userId = currentConfig.userId != null ? currentConfig.userId : "user_default";
                double systemLoad = deriveSystemLoad(input);

                NaturalLanguageIntentParser.ParseDiagnostics parseDiagnostics =
                    NaturalLanguageIntentParser.parseWithDiagnostics(input);
                IntentPipelineService.AnalysisResult analysis =
                    pipelineService.analyzeIntent(userId, input, systemLoad);

                Intent parsedIntent = analysis.parsedIntent();
                Intent refinedIntent = analysis.refinedIntent();
                SLAContract sla = analysis.slaContract();

                double observedCost = analysis.selectedCost() *
                    (1.0 + Math.max(0.0, systemLoad - 0.65) * 0.20);
                double observedLatency = analysis.selectedLatency() *
                    (1.0 + systemLoad * 0.25);
                boolean slaMet = observedCost <= sla.getMaxCostPerHour()
                    && observedLatency <= sla.getMaxLatencyMs();
                IntentPipelineService.FeedbackResult feedback =
                    pipelineService.applyFeedback(userId, slaMet, observedCost, observedLatency);

                // Update config with parsed intent
                currentConfig.userIntent = input;
                currentConfig.intentValidated = true;
                currentConfig.intentValidationMessage = "Intent accepted";
                currentConfig.parserConfidence = parseDiagnostics.confidence();
                currentConfig.dominantPriority = parseDiagnostics.dominantPriority();

                currentConfig.systemLoad = systemLoad;
                currentConfig.rlApplied = true;
                currentConfig.rlReward = feedback.reward();

                currentConfig.parsedCostPriority = parsedIntent.getCostPriority();
                currentConfig.parsedLatencyPriority = parsedIntent.getLatencyPriority();
                currentConfig.parsedSecurityPriority = parsedIntent.getSecurityPriority();
                currentConfig.parsedCarbonPriority = parsedIntent.getCarbonPriority();

                currentConfig.refinedCostPriority = refinedIntent.getCostPriority();
                currentConfig.refinedLatencyPriority = refinedIntent.getLatencyPriority();
                currentConfig.refinedSecurityPriority = refinedIntent.getSecurityPriority();
                currentConfig.refinedCarbonPriority = refinedIntent.getCarbonPriority();

                currentConfig.costPriority = refinedIntent.getCostPriority();
                currentConfig.latencyPriority = refinedIntent.getLatencyPriority();
                currentConfig.securityPriority = refinedIntent.getSecurityPriority();
                currentConfig.carbonPriority = refinedIntent.getCarbonPriority();
                
                currentConfig.maxLatencyMs = sla.getMaxLatencyMs();
                currentConfig.maxCostPerHour = sla.getMaxCostPerHour();
                currentConfig.minAvailabilityPercent = sla.getMinAvailability();
                currentConfig.securityLevel = sla.getMinSecurityLevel() > 5 ? "HIGH" : (sla.getMinSecurityLevel() > 2 ? "MEDIUM" : "LOW");
                currentConfig.recommendedOptionIndex = analysis.bestOption();
                currentConfig.candidateCosts = analysis.candidateCosts();
                currentConfig.candidateLatencies = analysis.candidateLatencies();
                currentConfig.selectedCostPerHour = analysis.selectedCost();
                currentConfig.selectedLatencyMs = analysis.selectedLatency();
                currentConfig.tradeoffScore = analysis.tradeoffScore();

                applyDynamicProvisioning(currentConfig, refinedIntent, sla, analysis, systemLoad, input);

                double baselineLatency = analysis.baselineSelectedLatency();
                double baselineCost = analysis.baselineSelectedCost();
                double baselineScore = analysis.baselineTradeoffScore();

                double latencyImprovement = improvementPercent(baselineLatency, analysis.selectedLatency());
                double costImprovement = improvementPercent(baselineCost, analysis.selectedCost());
                double scoreImprovement = improvementPercent(analysis.tradeoffScore(), baselineScore);

                currentConfig.rlLatencyImprovementPercent = latencyImprovement;
                currentConfig.rlCostImprovementPercent = costImprovement;
                currentConfig.rlScoreImprovementPercent = scoreImprovement;

                IntentPipelineService.LearningStats stats = feedback.learningStats();
                currentConfig.rlRunCount = stats.runCount();
                currentConfig.rlMaturityScore = stats.maturityScore();
                currentConfig.rlHistoricalLatencyImprovementPercent = stats.avgLatencyImprovementPercent();
                currentConfig.rlHistoricalCostImprovementPercent = stats.avgCostImprovementPercent();
                currentConfig.rlHistoricalScoreImprovementPercent = stats.avgScoreImprovementPercent();
                currentConfig.rlHistoricalAvgReward = stats.avgReward();
                currentConfig.rlHistoricalRewardTrend = stats.rewardTrend();
                currentConfig.rlHistoricalSlaSuccessRate = stats.slaSuccessRate();

                // Prepare display
                StringBuilder results = new StringBuilder();
                results.append("╔════════════════════════════════════════╗\n");
                results.append("║    PARSED INTENT ANALYSIS             ║\n");
                results.append("╚════════════════════════════════════════╝\n\n");
                results.append("Input: ").append(input).append("\n\n");
                results.append("PARSED PRIORITY SCORES:\n");
                results.append(String.format(Locale.ROOT, "  • Cost Priority:     %.1f%%\n", parsedIntent.getCostPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Latency Priority:  %.1f%%\n", parsedIntent.getLatencyPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Security Priority: %.1f%%\n", parsedIntent.getSecurityPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Carbon Priority:   %.1f%%\n\n", parsedIntent.getCarbonPriority() * 100));

                results.append("RL-REFINED PRIORITY SCORES:\n");
                results.append(String.format(Locale.ROOT, "  • Cost Priority:     %.1f%%\n", refinedIntent.getCostPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Latency Priority:  %.1f%%\n", refinedIntent.getLatencyPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Security Priority: %.1f%%\n", refinedIntent.getSecurityPriority() * 100));
                results.append(String.format(Locale.ROOT, "  • Carbon Priority:   %.1f%%\n", refinedIntent.getCarbonPriority() * 100));

                results.append("\nPARSER CONFIDENCE BREAKDOWN:\n");
                appendDimensionConfidence(results, "Cost", parseDiagnostics.dimensions().get("cost"));
                appendDimensionConfidence(results, "Latency", parseDiagnostics.dimensions().get("latency"));
                appendDimensionConfidence(results, "Security", parseDiagnostics.dimensions().get("security"));
                appendDimensionConfidence(results, "Carbon", parseDiagnostics.dimensions().get("carbon"));
                if (!parseDiagnostics.matchedDomains().isEmpty()) {
                    results.append("  • Domain Context:    ")
                        .append(String.join(", ", parseDiagnostics.matchedDomains()))
                        .append("\n");
                }

                StringBuilder suggestions = new StringBuilder();
                suggestions.append("╔════════════════════════════════════════╗\n");
                suggestions.append("║    RECOMMENDED CONFIGURATION          ║\n");
                suggestions.append("╚════════════════════════════════════════╝\n\n");
                suggestions.append("NEGOTIATED SLA:\n");
                suggestions.append(String.format(Locale.ROOT, "  • Max Latency:       %.0f ms\n", sla.getMaxLatencyMs()));
                suggestions.append(String.format(Locale.ROOT, "  • Max Cost:          $%.2f/hour\n", sla.getMaxCostPerHour()));
                suggestions.append(String.format(Locale.ROOT, "  • Min Availability:  %.1f%%\n", sla.getMinAvailability()));
                suggestions.append(String.format(Locale.ROOT, "  • Security Level:    %.1f\n\n", sla.getMinSecurityLevel()));

                suggestions.append("TRADE-OFF ANALYSIS:\n");
                for (int i = 0; i < analysis.candidateCosts().length; i++) {
                    String marker = (i == analysis.bestOption()) ? " ⭐ RECOMMENDED" : "";
                    suggestions.append(String.format(Locale.ROOT,
                        "  Option %d: $%.2f/hr, %.1fms latency%s\n",
                        i + 1,
                        analysis.candidateCosts()[i],
                        analysis.candidateLatencies()[i],
                        marker));
                }

                suggestions.append(String.format(Locale.ROOT,
                    "\nRL FEEDBACK:\n  • Observed Cost:      $%.2f/hr\n  • Observed Latency:   %.1f ms\n  • SLA Met:            %s\n  • Reward:             %.3f\n",
                    observedCost, observedLatency, slaMet ? "YES" : "NO", feedback.reward()));

                suggestions.append(String.format(Locale.ROOT,
                    "\nRL VALUE ADD (vs parser-only baseline):\n"
                        + "  • Baseline Best Option: Option %d ($%.2f/hr, %.1fms)\n"
                        + "  • RL Best Option:       Option %d ($%.2f/hr, %.1fms)\n"
                        + "  • Latency Improvement:  %.1f%%\n"
                        + "  • Cost Improvement:     %.1f%%\n"
                        + "  • Tradeoff Score Gain:  %.1f%%\n",
                    analysis.baselineBestOption() + 1,
                    baselineCost,
                    baselineLatency,
                    analysis.bestOption() + 1,
                    analysis.selectedCost(),
                    analysis.selectedLatency(),
                    latencyImprovement,
                    costImprovement,
                    scoreImprovement));

                suggestions.append(String.format(Locale.ROOT,
                    "\nRL LEARNING MATURITY (data-driven):\n"
                        + "  • Runs observed:        %d\n"
                        + "  • Maturity:             %.0f%% (%s)\n"
                        + "  • Avg Latency Gain:     %.1f%%\n"
                        + "  • Avg Cost Gain:        %.1f%%\n"
                        + "  • Avg Score Gain:       %.1f%%\n"
                        + "  • SLA Success Rate:     %.1f%%\n"
                        + "  • Reward Trend:         %.3f\n",
                    stats.runCount(),
                    stats.maturityScore() * 100.0,
                    stats.mature() ? "mature" : "warming up",
                    stats.avgLatencyImprovementPercent(),
                    stats.avgCostImprovementPercent(),
                    stats.avgScoreImprovementPercent(),
                    stats.slaSuccessRate() * 100.0,
                    stats.rewardTrend()));

                suggestions.append("\nPLACEMENT STRATEGY:\n");
                if (refinedIntent.getCostPriority() > 0.7) {
                    suggestions.append("  • Consolidate VMs on fewer hosts\n");
                    suggestions.append("  • Use shared infrastructure\n");
                    currentConfig.vmPlacementPolicy = "CONSOLIDATED";
                } else if (refinedIntent.getLatencyPriority() > 0.7) {
                    suggestions.append("  • Spread VMs across multiple hosts\n");
                    suggestions.append("  • Use high-performance hosts\n");
                    currentConfig.vmPlacementPolicy = "SPREAD";
                } else if (refinedIntent.getSecurityPriority() > 0.7) {
                    suggestions.append("  • Use isolated/dedicated hosts\n");
                    suggestions.append("  • Enable encryption on all VMs\n");
                    currentConfig.vmPlacementPolicy = "ISOLATED";
                } else {
                    suggestions.append("  • Balanced placement to optimize mixed priorities\n");
                    currentConfig.vmPlacementPolicy = "SPREAD";
                }

                if (refinedIntent.getCarbonPriority() > 0.5) {
                    suggestions.append("  • Use green/renewable energy datacenters\n");
                    currentConfig.greenDatacenter = true;
                } else {
                    currentConfig.greenDatacenter = false;
                }

                suggestions.append("\nPROVISIONED INFRASTRUCTURE (intent-derived):\n");
                suggestions.append(String.format(Locale.ROOT,
                    "  • Hosts:              %d (each: %d cores, %d GB RAM, %d GB storage)\n",
                    currentConfig.numHosts,
                    currentConfig.hostCores,
                    currentConfig.hostRamGb,
                    currentConfig.hostStorageGb));
                suggestions.append(String.format(Locale.ROOT,
                    "  • VMs:                %d (each: %d cores, %d GB RAM)\n",
                    currentConfig.numVMs,
                    currentConfig.vmCores,
                    currentConfig.vmRamGb));
                suggestions.append(String.format(Locale.ROOT,
                    "  • Cloudlets:          %d\n",
                    currentConfig.numCloudlets));

                Platform.runLater(() -> {
                    resultArea.setText(results.toString());
                    suggestionsArea.setText(suggestions.toString());
                    confidenceLabel.setText(String.format(Locale.ROOT,
                        "Confidence: %.0f%%", parseDiagnostics.confidence() * 100));
                    dominantLabel.setText("Dominant: " + parseDiagnostics.dominantPriority() + " | RL: enabled");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    resultArea.setText("Error parsing intent: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private boolean isIntentDetailedEnough(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).trim();
        if (normalized.length() < 18) return false;

        String[] words = normalized.split("\\s+");
        int meaningful = 0;
        for (String word : words) {
            if (word.matches("[a-zA-Z]{3,}")) {
                meaningful++;
            }
        }

        boolean hasSignalWord = normalized.contains("cost") || normalized.contains("budget")
            || normalized.contains("latency") || normalized.contains("fast")
            || normalized.contains("security") || normalized.contains("secure")
            || normalized.contains("green") || normalized.contains("sustainable")
            || normalized.contains("compliance") || normalized.contains("availability");

        return meaningful >= 5 && hasSignalWord;
    }

    private double deriveSystemLoad(String input) {
        int words = input.trim().split("\\s+").length;
        double lexicalFactor = Math.min(0.55, words * 0.03);
        double complexityFactor = input.contains(",") || input.contains("and") ? 0.10 : 0.05;
        return Math.max(0.25, Math.min(0.95, 0.30 + lexicalFactor + complexityFactor));
    }

    private double improvementPercent(double baseline, double candidate) {
        if (baseline <= 0.0) return 0.0;
        return ((baseline - candidate) / baseline) * 100.0;
    }

    private void applyDynamicProvisioning(CloudConfig config,
                                          Intent refinedIntent,
                                          SLAContract sla,
                                          IntentPipelineService.AnalysisResult analysis,
                                          double systemLoad,
                                          String input) {
        int bestTier = Math.max(0, Math.min(3, analysis.bestOption()));
        int wordCount = input == null ? 0 : input.trim().split("\\s+").length;

        double perfSignal = refinedIntent.getLatencyPriority();
        double securitySignal = refinedIntent.getSecurityPriority();
        double costSignal = refinedIntent.getCostPriority();
        double carbonSignal = refinedIntent.getCarbonPriority();

        int hostBase = 3 + bestTier;
        int perfScale = (int) Math.round(perfSignal * 4.0);
        int securityScale = (int) Math.round(securitySignal * 2.0);
        int loadScale = (int) Math.round(systemLoad * 3.0);
        int costReduction = (int) Math.round(costSignal * 3.0);

        int hosts = clampInt(2, 14, hostBase + perfScale + securityScale + loadScale - costReduction);

        int hostCores = 8 + (bestTier * 2) + (int) Math.round(perfSignal * 6.0);
        if (sla.getMaxLatencyMs() <= 80.0) {
            hostCores += 2;
        }
        hostCores = clampInt(8, 32, hostCores);

        long hostRamGb = 16L + (bestTier * 8L) + Math.round(perfSignal * 24.0);
        hostRamGb = clampLong(16L, 128L, hostRamGb);

        long hostStorageGb = 800L + (bestTier * 400L) + Math.round((1.0 - carbonSignal) * 300.0);
        hostStorageGb = clampLong(800L, 4800L, hostStorageGb);

        int vmDensity = clampInt(1, 4, 1 + bestTier + (int) Math.round((1.0 - securitySignal) * 1.5));
        int numVMs = clampInt(2, hosts * 4, hosts * vmDensity);

        int vmCores = clampInt(2, 8, 2 + bestTier + (perfSignal > 0.7 ? 1 : 0));
        long vmRamGb = clampLong(4L, 24L, 4L + (bestTier * 2L) + Math.round(securitySignal * 4.0));

        int cloudlets = clampInt(8, 300, (int) Math.round(numVMs * (1.8 + systemLoad * 1.6 + (wordCount / 40.0))));
        if (perfSignal > 0.8 && sla.getMaxLatencyMs() < 70.0) {
            cloudlets = clampInt(8, 300, cloudlets + 12);
        }

        config.numHosts = hosts;
        config.hostCores = hostCores;
        config.hostRamGb = hostRamGb;
        config.hostStorageGb = hostStorageGb;

        config.numVMs = numVMs;
        config.vmCores = vmCores;
        config.vmRamGb = vmRamGb;

        config.hostsPerDatacenter = hosts;
        config.vmsPerHost = Math.max(1, Math.round((float) numVMs / (float) hosts));
        config.numCloudlets = cloudlets;
        config.cloudletsPerVM = Math.max(1, Math.round((float) cloudlets / (float) numVMs));
    }

    private int clampInt(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    private long clampLong(long min, long max, long value) {
        return Math.max(min, Math.min(max, value));
    }

    private void appendDimensionConfidence(StringBuilder results,
                                           String label,
                                           NaturalLanguageIntentParser.DimensionConfidence confidence) {
        if (confidence == null) return;
        results.append(String.format(Locale.ROOT,
            "  • %-11s keyword=%.2f semantic=%.2f fused=%.2f final=%.2f%s\n",
            label + ":",
            confidence.keywordScore(),
            confidence.semanticScore(),
            confidence.fusedScore(),
            confidence.finalScore(),
            confidence.negated() ? " (negated)" : ""
        ));
    }

    /**
     * Load an example intent
     */
    private void loadExample() {
        String[] examples = {
            "I need very cheap servers for batch processing",
            "Fast real-time gaming servers with low latency",
            "Secure, encrypted infrastructure for banking compliance",
            "Balanced cost-effective and responsive servers",
            "Green sustainable carbon-neutral infrastructure"
        };
        String example = examples[(int)(Math.random() * examples.length)];
        inputArea.setText(example);
        parseAndAnalyze();
    }

    /**
     * Get the current configuration
     */
    public CloudConfig getCurrentConfig() {
        return currentConfig;
    }

    /**
     * Set configuration from another tab
     */
    public void setCurrentConfig(CloudConfig config) {
        this.currentConfig = config;
    }
}
