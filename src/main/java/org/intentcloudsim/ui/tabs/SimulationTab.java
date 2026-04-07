package org.intentcloudsim.ui.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.intentcloudsim.ui.models.CloudConfig;

import java.util.Locale;

/**
 * Tab 3: Execute simulation and visualize results
 * 
 * Shows:
 * - Provisioned infrastructure from configuration
 * - Real-time performance metrics
 * - Cost breakdown and average cost per hour
 * - Resource utilization
 * - SLA compliance tracking
 */
public class SimulationTab extends VBox {
    private enum IntentProfile {
        LATENCY_FIRST,
        COST_FIRST,
        SECURITY_FIRST,
        GREEN_FIRST,
        BALANCED
    }

    private Button startButton;
    private Button pauseButton;
    private Button stopButton;
    private ProgressBar progressBar;
    private TextArea logArea;
    private Label statusLabel;
    private Label timeLabel;
    private CloudConfig currentConfig;
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private Thread simulationThread;
    
    // Metrics tracking
    private double totalSimulationCost = 0.0;
    private double avgLatency = 0.0;
    private int slaViolations = 0;
    private double cpuUtilization = 0.0;
    private double memoryUtilization = 0.0;
    private int vmMigrations = 0;
    private int totalCloudletsProcessed = 0;
    private XYChart.Series<String, Number> latencySeries;

    // Provisioned infrastructure labels (live-updated)
    private Label infraCoresValue;
    private Label infraRamValue;
    private Label infraStorageValue;
    private Label infraVmSpecsValue;
    private Label infraCloudletsValue;
    private Label infraPlacementValue;

    // Cost analysis labels (live-updated)
    private Label costComputeValue;
    private Label costStorageValue;
    private Label costNetworkValue;
    private Label costTotalValue;
    private Label costDurationValue;

    // RL impact labels (live-updated)
    private Label rlProfileValue;
    private Label rlRewardValue;
    private Label rlLatencyGainValue;
    private Label rlCostGainValue;
    private Label rlScoreGainValue;
    private Label rlStatusValue;
    private Label rlRunsValue;

    public SimulationTab() {
        setPadding(new Insets(10));
        setSpacing(10);
        currentConfig = new CloudConfig();

        // Top: Control panel
        HBox controlPanel = createControlPanel();

        // Middle: Log area
        VBox logSection = createLogSection();

        // Bottom: Results visualization
        HBox resultsSection = createResultsSection();

        getChildren().addAll(controlPanel, logSection, resultsSection);
        VBox.setVgrow(logSection, Priority.ALWAYS);
        VBox.setVgrow(resultsSection, Priority.SOMETIMES);

    refreshDisplayPanels();
    }

    /**
     * Create simulation control panel
     */
    private HBox createControlPanel() {
        HBox box = new HBox(15);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #d0d0d0; -fx-background-color: #f9f9f9; -fx-border-radius: 5;");
        box.setAlignment(Pos.CENTER_LEFT);

        startButton = new Button("▶ Start Simulation");
        startButton.setStyle("-fx-padding: 10 20; -fx-font-size: 12; -fx-font-weight: bold; " +
                            "-fx-background-color: #27ae60; -fx-text-fill: white;");
        startButton.setOnAction(e -> startSimulation());

        pauseButton = new Button("⏸ Pause");
        pauseButton.setStyle("-fx-padding: 10 20; -fx-font-size: 12; " +
                            "-fx-background-color: #f39c12; -fx-text-fill: white;");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseSimulation());

        stopButton = new Button("⏹ Stop");
        stopButton.setStyle("-fx-padding: 10 20; -fx-font-size: 12; " +
                           "-fx-background-color: #e74c3c; -fx-text-fill: white;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        timeLabel = new Label("Time: 0s");
        timeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-font-size: 10;");

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);

        box.getChildren().addAll(startButton, pauseButton, stopButton, sep, statusLabel, 
                                new Separator(javafx.geometry.Orientation.VERTICAL), 
                                timeLabel, progressBar);
        HBox.setHgrow(progressBar, Priority.NEVER);
        return box;
    }

    /**
     * Create simulation log section
     */
    private VBox createLogSection() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5;");

        Label label = new Label("📊 Simulation Execution Log");
        label.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

        logArea = new TextArea();
        logArea.setWrapText(true);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 9; " +
                        "-fx-control-inner-background: #1e1e1e; -fx-text-fill: #c0c0c0;");
        logArea.setText("[INFO] Simulation ready. Load configuration and click Start.\n");

        VBox.setVgrow(logArea, Priority.ALWAYS);
        box.getChildren().addAll(label, logArea);
        return box;
    }

    /**
     * Create results visualization section
     */
    private HBox createResultsSection() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5));

        // Provisioned infrastructure
        VBox infraBox = createProvisionedInfrastructurePanel();

        // Performance metrics
        VBox perfBox = createPerformanceMetricsPanel();

        // Cost breakdown
        VBox costBox = createCostBreakdownPanel();

        // RL impact
        VBox rlImpactBox = createRlImpactPanel();

        box.getChildren().addAll(infraBox, perfBox, costBox, rlImpactBox);
        HBox.setHgrow(infraBox, Priority.ALWAYS);
        HBox.setHgrow(perfBox, Priority.ALWAYS);
        HBox.setHgrow(costBox, Priority.ALWAYS);
        HBox.setHgrow(rlImpactBox, Priority.ALWAYS);
        return box;
    }

    /**
     * Create provisioned infrastructure panel based on CloudConfig
     */
    private VBox createProvisionedInfrastructurePanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #3498db; -fx-border-radius: 3; -fx-background-color: #ecf7ff; -fx-border-width: 2;");

        Label title = new Label("🖥️ Provisioned Infrastructure");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #0066cc;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(5));

        // Row 1: Compute Resources
        Label computeLabel = new Label("Compute Resources:");
        computeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #333;");
        grid.add(computeLabel, 0, 0);

    infraCoresValue = new Label();
    infraCoresValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraCoresValue, 0, 1);

    infraRamValue = new Label();
    infraRamValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraRamValue, 0, 2);

    infraStorageValue = new Label();
    infraStorageValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraStorageValue, 0, 3);

        // Row 2: Virtual Machines
        Label vmLabel = new Label("Virtual Machines:");
        vmLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #333; -fx-padding: 10 0 0 0;");
        grid.add(vmLabel, 0, 4);

    infraVmSpecsValue = new Label();
    infraVmSpecsValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraVmSpecsValue, 0, 5);

        // Row 3: Workload
        Label workloadLabel = new Label("Workload:");
        workloadLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #333; -fx-padding: 10 0 0 0;");
        grid.add(workloadLabel, 0, 6);

    infraCloudletsValue = new Label();
    infraCloudletsValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraCloudletsValue, 0, 7);

        // Row 4: Configuration
        Label configLabel = new Label("Configuration:");
        configLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: #333; -fx-padding: 10 0 0 0;");
        grid.add(configLabel, 0, 8);

    infraPlacementValue = new Label();
    infraPlacementValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc; -fx-padding: 0 0 0 20;");
    grid.add(infraPlacementValue, 0, 9);

        box.getChildren().addAll(title, grid);
        return box;
    }

    /**
     * Create performance metrics panel
     */
    private VBox createPerformanceMetricsPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #27ae60; -fx-border-radius: 3; -fx-background-color: #f0fff3; -fx-border-width: 2;");

        Label title = new Label("📊 Performance Metrics");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Performance timeline chart
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time (s)");
    NumberAxis yAxis = new NumberAxis();
    yAxis.setAutoRanging(true);
    yAxis.setForceZeroInRange(false);
        yAxis.setLabel("Latency (ms)");

        LineChart<String, Number> latencyChart = new LineChart<>(xAxis, yAxis);
        latencyChart.setTitle("Average Latency Over Time");
        latencyChart.setPrefHeight(140);
        latencyChart.setStyle("-fx-font-size: 9;");
        latencyChart.setLegendVisible(false);

        latencySeries = new XYChart.Series<>();
        latencySeries.setName("Latency (ms)");
        latencyChart.getData().add(latencySeries);

        VBox.setVgrow(latencyChart, Priority.ALWAYS);
        box.getChildren().addAll(title, latencyChart);
        return box;
    }

    /**
     * Create cost breakdown panel
     */
    private VBox createCostBreakdownPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 3; -fx-background-color: #ffeceb; -fx-border-width: 2;");

        Label title = new Label("💰 Cost Analysis");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        // Cost summary section
        GridPane costGrid = new GridPane();
        costGrid.setHgap(20);
        costGrid.setVgap(8);
        costGrid.setPadding(new Insets(5));

    CostBreakdown costs = estimateHourlyCosts();

        // Cost breakdown labels
        Label computeLabel = new Label("Compute:");
        computeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
    costComputeValue = new Label(String.format("$%.2f/hr", costs.compute()));
    costComputeValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc;");
        costGrid.add(computeLabel, 0, 0);
    costGrid.add(costComputeValue, 1, 0);

        Label storageLabel = new Label("Storage:");
        storageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
    costStorageValue = new Label(String.format("$%.2f/hr", costs.storage()));
    costStorageValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc;");
        costGrid.add(storageLabel, 0, 1);
    costGrid.add(costStorageValue, 1, 1);

        Label networkLabel = new Label("Network:");
        networkLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
    costNetworkValue = new Label(String.format("$%.2f/hr", costs.network()));
    costNetworkValue.setStyle("-fx-font-size: 10; -fx-text-fill: #0066cc;");
        costGrid.add(networkLabel, 0, 2);
    costGrid.add(costNetworkValue, 1, 2);

        // Separator
        Separator sep = new Separator(javafx.geometry.Orientation.HORIZONTAL);
        costGrid.add(sep, 0, 3);
        GridPane.setColumnSpan(sep, 2);

        // Total
        Label totalLabel = new Label("Total Hourly Cost:");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11; -fx-padding: 5 0 0 0;");
    costTotalValue = new Label(String.format("$%.2f/hr", costs.total()));
    costTotalValue.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
        costGrid.add(totalLabel, 0, 4);
    costGrid.add(costTotalValue, 1, 4);

        // Simulation duration
        Label durationLabel = new Label("Simulation Duration:");
        durationLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #666; -fx-padding: 5 0 0 0;");
    costDurationValue = new Label("(Updates during simulation)");
    costDurationValue.setStyle("-fx-font-size: 9; -fx-text-fill: #666;");
        costGrid.add(durationLabel, 0, 5);
    costGrid.add(costDurationValue, 1, 5);

        box.getChildren().addAll(title, costGrid);
        return box;
    }

    /**
     * Create RL impact panel.
     */
    private VBox createRlImpactPanel() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #8e44ad; -fx-border-radius: 3; -fx-background-color: #f7edff; -fx-border-width: 2;");

        Label title = new Label("🧠 RL Impact");
        title.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #6c2f8f;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(5));

        Label profileLabel = new Label("Intent Profile:");
        profileLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlProfileValue = new Label("-");
        rlProfileValue.setStyle("-fx-font-size: 10; -fx-text-fill: #5d2c7e;");
        grid.add(profileLabel, 0, 0);
        grid.add(rlProfileValue, 1, 0);

        Label rewardLabel = new Label("RL Reward:");
        rewardLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlRewardValue = new Label("0.000");
        rlRewardValue.setStyle("-fx-font-size: 10; -fx-text-fill: #5d2c7e;");
        grid.add(rewardLabel, 0, 1);
        grid.add(rlRewardValue, 1, 1);

    Label runsLabel = new Label("Learning Runs:");
    runsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
    rlRunsValue = new Label("0 (warming up)");
    rlRunsValue.setStyle("-fx-font-size: 10; -fx-text-fill: #5d2c7e;");
    grid.add(runsLabel, 0, 2);
    grid.add(rlRunsValue, 1, 2);

        Label latencyGainLabel = new Label("Latency Gain:");
        latencyGainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlLatencyGainValue = new Label("0.0%");
        rlLatencyGainValue.setStyle("-fx-font-size: 10; -fx-text-fill: #27ae60;");
    grid.add(latencyGainLabel, 0, 3);
    grid.add(rlLatencyGainValue, 1, 3);

        Label costGainLabel = new Label("Cost Gain:");
        costGainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlCostGainValue = new Label("0.0%");
        rlCostGainValue.setStyle("-fx-font-size: 10; -fx-text-fill: #27ae60;");
    grid.add(costGainLabel, 0, 4);
    grid.add(rlCostGainValue, 1, 4);

        Label scoreGainLabel = new Label("Score Gain:");
        scoreGainLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlScoreGainValue = new Label("0.0%");
        rlScoreGainValue.setStyle("-fx-font-size: 10; -fx-text-fill: #27ae60;");
    grid.add(scoreGainLabel, 0, 5);
    grid.add(rlScoreGainValue, 1, 5);

        Label statusLabel = new Label("RL Verdict:");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
        rlStatusValue = new Label("Awaiting intent analysis");
        rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #5d2c7e;");
    grid.add(statusLabel, 0, 6);
    grid.add(rlStatusValue, 1, 6);

        box.getChildren().addAll(title, grid);
        return box;
    }

    /**
     * Helper: Calculate total storage in GB
     */
    private long totalStorageGb() {
        return currentConfig.numHosts * currentConfig.hostStorageGb;
    }

    private CostBreakdown estimateHourlyCosts() {
        double costSensitivity = 1.10 - (currentConfig.costPriority * 0.35);
        double performancePremium = 0.85
            + (currentConfig.latencyPriority * 0.50)
            + (currentConfig.securityPriority * 0.20);

        double hourlyComputeCost = currentConfig.numHosts *
            (0.18 + (currentConfig.hostCores * 0.05 * performancePremium * costSensitivity));
        double hourlyStorageCost = (totalStorageGb() / 1000.0) *
            (0.03 + (currentConfig.securityPriority * 0.04));
        double hourlyNetworkCost = currentConfig.numVMs *
            (0.02 + (currentConfig.latencyPriority * 0.06));
        double totalHourlyCost = hourlyComputeCost + hourlyStorageCost + hourlyNetworkCost;

        if (currentConfig.greenDatacenter) {
            totalHourlyCost *= 0.97;
        }

        if (currentConfig.selectedCostPerHour > 0) {
            totalHourlyCost = (totalHourlyCost + currentConfig.selectedCostPerHour) / 2.0;
        }

        return new CostBreakdown(hourlyComputeCost, hourlyStorageCost, hourlyNetworkCost, totalHourlyCost);
    }

    private void refreshDisplayPanels() {
        if (infraCoresValue != null) {
            long totalCores = (long) currentConfig.numHosts * currentConfig.hostCores;
            long totalRam = (long) currentConfig.numHosts * currentConfig.hostRamGb;
            long totalStorage = (long) currentConfig.numHosts * currentConfig.hostStorageGb;

            infraCoresValue.setText(currentConfig.numHosts + " Hosts × " + currentConfig.hostCores +
                " cores = " + totalCores + " Total Cores");
            infraRamValue.setText(currentConfig.numHosts + " Hosts × " + currentConfig.hostRamGb +
                " GB = " + totalRam + " GB RAM");
            infraStorageValue.setText(currentConfig.numHosts + " Hosts × " + currentConfig.hostStorageGb +
                " GB = " + totalStorage + " GB Storage");
            infraVmSpecsValue.setText(currentConfig.numVMs + " VMs × (" + currentConfig.vmCores +
                " cores, " + currentConfig.vmRamGb + " GB RAM each)");
            infraCloudletsValue.setText(currentConfig.numCloudlets + " Cloudlets distributed across " +
                currentConfig.numVMs + " VMs");
            infraPlacementValue.setText("Placement: " + currentConfig.vmPlacementPolicy +
                " | Green: " + (currentConfig.greenDatacenter ? "Yes ✓" : "No"));
        }

        if (costComputeValue != null) {
            CostBreakdown costs = estimateHourlyCosts();
            costComputeValue.setText(String.format("$%.2f/hr", costs.compute()));
            costStorageValue.setText(String.format("$%.2f/hr", costs.storage()));
            costNetworkValue.setText(String.format("$%.2f/hr", costs.network()));
            costTotalValue.setText(String.format("$%.2f/hr", costs.total()));
            if (!isRunning) {
                costDurationValue.setText("Estimated runtime cost updates when simulation starts");
            }
        }

        if (rlProfileValue != null) {
            rlProfileValue.setText(resolveIntentProfile().name().replace('_', ' '));
            rlRewardValue.setText(String.format("%.3f", currentConfig.rlReward));
            rlRunsValue.setText(String.format("%d runs (maturity %.0f%%)",
                currentConfig.rlRunCount,
                currentConfig.rlMaturityScore * 100.0));
            rlLatencyGainValue.setText(String.format("%.1f%%", currentConfig.rlHistoricalLatencyImprovementPercent));
            rlCostGainValue.setText(String.format("%.1f%%", currentConfig.rlHistoricalCostImprovementPercent));
            rlScoreGainValue.setText(String.format("%.1f%%", currentConfig.rlHistoricalScoreImprovementPercent));

            double weightedGain = (currentConfig.rlHistoricalScoreImprovementPercent * 0.45)
                + (currentConfig.rlHistoricalLatencyImprovementPercent * 0.30)
                + (currentConfig.rlHistoricalCostImprovementPercent * 0.25);
            double stabilitySignal = (currentConfig.rlHistoricalSlaSuccessRate - 0.5) * 100.0;
            double trendSignal = currentConfig.rlHistoricalRewardTrend * 20.0;
            double dataDrivenScore = weightedGain + stabilitySignal + trendSignal;
            boolean matureModel = currentConfig.rlRunCount >= 20;
            boolean highSlaReliability = currentConfig.rlHistoricalSlaSuccessRate >= 0.80;
            boolean nearParityGain = Math.abs(weightedGain) <= 5.0;
            boolean flatRewardTrend = Math.abs(trendSignal) <= 3.0;

            if (!currentConfig.rlApplied) {
                rlStatusValue.setText("RL inactive (analyze an intent in Tab 1)");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d;");
            } else if (currentConfig.rlRunCount < 10) {
                rlStatusValue.setText(String.format("⏳ RL warming up (%d/10 runs) — learning from data", currentConfig.rlRunCount));
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #8e44ad; -fx-font-weight: bold;");
            } else if (dataDrivenScore > 8.0) {
                rlStatusValue.setText("✅ RL is improving decisions over baseline (data-backed)");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (Math.abs(dataDrivenScore) <= 4.0 && currentConfig.rlRunCount < 20) {
                rlStatusValue.setText("➖ RL is stable; gains are plateauing with similar runs (try varied intents)");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
            } else if (matureModel && highSlaReliability && nearParityGain && flatRewardTrend) {
                rlStatusValue.setText("✅ RL is converged and stable; performance is near baseline parity");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #16a085; -fx-font-weight: bold;");
            } else if (dataDrivenScore > 0.0) {
                rlStatusValue.setText("↗ RL shows mild improvement with current training data");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #2980b9; -fx-font-weight: bold;");
            } else {
                rlStatusValue.setText("⚠ RL gain is limited; diversify scenarios or retrain with stronger feedback");
                rlStatusValue.setStyle("-fx-font-size: 10; -fx-text-fill: #d35400; -fx-font-weight: bold;");
            }
        }
    }


    /**
     * Start simulation
     */
    private void startSimulation() {
        if (isRunning) return;

        if (!currentConfig.intentValidated || currentConfig.userIntent == null
            || currentConfig.userIntent.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Intent Required");
            alert.setHeaderText("Simulation needs a complete intent");
            alert.setContentText(currentConfig.intentValidationMessage != null
                ? currentConfig.intentValidationMessage
                : "Go to Tab 1 and provide a complete natural-language requirement before running simulation.");
            alert.showAndWait();
            logError("Simulation blocked: missing or incomplete intent input.");
            return;
        }

        isRunning = true;
        isPaused = false;
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);

        // Reset metrics
        totalSimulationCost = 0.0;
        avgLatency = 0.0;
        slaViolations = 0;
        cpuUtilization = 0.0;
        memoryUtilization = 0.0;
        vmMigrations = 0;
        totalCloudletsProcessed = 0;
        Platform.runLater(() -> latencySeries.getData().clear());

        simulationThread = new Thread(() -> {
            try {
                CostBreakdown costs = estimateHourlyCosts();
                double hourlyComputeCost = costs.compute();
                double hourlyStorageCost = costs.storage();
                double hourlyNetworkCost = costs.network();
                double totalHourlyCost = costs.total();

                int simulationSeconds = (int) clamp(
                    45,
                    360,
                    currentConfig.numCloudlets * (2.4 - currentConfig.latencyPriority)
                );

                Platform.runLater(() -> {
                    refreshDisplayPanels();
                    costDurationValue.setText(String.format("Target duration: %ds", simulationSeconds));
                });

                double slaLatencyTarget = Math.max(12.0, currentConfig.maxLatencyMs);
                double optionLatency = currentConfig.selectedLatencyMs > 0
                    ? currentConfig.selectedLatencyMs
                    : slaLatencyTarget;
                double targetLatency = clamp(
                    slaLatencyTarget * 0.65,
                    slaLatencyTarget * 1.20,
                    (optionLatency * 0.55) + (slaLatencyTarget * 0.45)
                );

                double baselineLatency = targetLatency *
                    (1.04 + (currentConfig.systemLoad * 0.18));

                double throughputCapacity = Math.max(
                    1.0,
                    currentConfig.numVMs * currentConfig.vmCores *
                        (0.65 + currentConfig.latencyPriority * 0.50)
                );

                double completionExponent = 1.05 + (1.0 - currentConfig.latencyPriority) * 0.25;
                double latencyPriority = currentConfig.latencyPriority;
                double costPriority = currentConfig.costPriority;
                double securityPriority = currentConfig.securityPriority;
                double carbonPriority = currentConfig.carbonPriority;

                IntentProfile profile = resolveIntentProfile();
                double curveExponent;
                double burstCenter;
                double burstWidth;
                double burstAmplitudeFactor;
                double oscillationCycles;
                double oscillationAmplitudeFactor;

                switch (profile) {
                    case LATENCY_FIRST -> {
                        curveExponent = 2.20;
                        burstCenter = 0.18;
                        burstWidth = 0.10;
                        burstAmplitudeFactor = 0.045;
                        oscillationCycles = 2.3;
                        oscillationAmplitudeFactor = 0.012;
                    }
                    case COST_FIRST -> {
                        curveExponent = 1.20;
                        burstCenter = 0.33;
                        burstWidth = 0.18;
                        burstAmplitudeFactor = 0.11;
                        oscillationCycles = 3.7;
                        oscillationAmplitudeFactor = 0.028;
                    }
                    case SECURITY_FIRST -> {
                        curveExponent = 1.55;
                        burstCenter = 0.28;
                        burstWidth = 0.15;
                        burstAmplitudeFactor = 0.095;
                        oscillationCycles = 2.9;
                        oscillationAmplitudeFactor = 0.020;
                    }
                    case GREEN_FIRST -> {
                        curveExponent = 1.35;
                        burstCenter = 0.31;
                        burstWidth = 0.17;
                        burstAmplitudeFactor = 0.06;
                        oscillationCycles = 4.2;
                        oscillationAmplitudeFactor = 0.024;
                    }
                    default -> {
                        curveExponent = 1.60;
                        burstCenter = 0.26;
                        burstWidth = 0.14;
                        burstAmplitudeFactor = 0.07;
                        oscillationCycles = 3.0;
                        oscillationAmplitudeFactor = 0.018;
                    }
                }

                curveExponent = clamp(1.05, 2.60,
                    curveExponent + (latencyPriority * 0.25) - (costPriority * 0.10));
                burstCenter = clamp(0.12, 0.45, burstCenter + (securityPriority * 0.02));
                burstWidth = clamp(0.07, 0.20, burstWidth + (costPriority * 0.01));
                burstAmplitudeFactor = clamp(0.03, 0.18,
                    burstAmplitudeFactor + (currentConfig.systemLoad * 0.03) + (securityPriority * 0.02));
                oscillationCycles = clamp(1.4, 4.6, oscillationCycles + (carbonPriority * 0.2));
                oscillationAmplitudeFactor = clamp(0.008, 0.070,
                    oscillationAmplitudeFactor + ((1.0 - latencyPriority) * 0.008));

                double rlEffectStrength = clamp(0.0, 0.35,
                    (Math.max(0.0, currentConfig.rlLatencyImprovementPercent)
                        + Math.max(0.0, currentConfig.rlScoreImprovementPercent)) / 220.0);
                double rlTargetBoost = 1.0 - (rlEffectStrength * 0.12);
                targetLatency = clamp(8.0, 2000.0, targetLatency * rlTargetBoost);
                baselineLatency = clamp(8.0, 2000.0, baselineLatency * (1.0 - rlEffectStrength * 0.08));
                int intentHash = Math.abs((currentConfig.userIntent == null ? "" :
                    currentConfig.userIntent.toLowerCase(Locale.ROOT)).hashCode());
                double phaseShift = ((intentHash % 360) / 360.0) * (2.0 * Math.PI);

                int warmupGraceSteps = 10;
                int slaCheckEverySteps = 5;
                int slaChecks = 0;

                logInfo("================================================");
                logInfo("Starting Simulation");
                logInfo("================================================");
                logInfo("User Intent: " + currentConfig.userIntent);
                logInfo("Configuration:");
                logInfo("  Hosts: " + currentConfig.numHosts + " (CPU: " + currentConfig.hostCores + " cores, RAM: " + currentConfig.hostRamGb + " GB each)");
                logInfo("  VMs: " + currentConfig.numVMs + " (CPU: " + currentConfig.vmCores + " cores, RAM: " + currentConfig.vmRamGb + " GB each)");
                logInfo("  Cloudlets: " + currentConfig.numCloudlets);
                logInfo("  Placement Strategy: " + currentConfig.vmPlacementPolicy);
                logInfo("  Green Datacenter: " + (currentConfig.greenDatacenter ? "Yes" : "No"));
                logInfo(String.format("  RL Enabled: %s | Reward: %.3f", currentConfig.rlApplied ? "Yes" : "No", currentConfig.rlReward));
                logInfo("");
                logInfo("Provisioned Resources Summary:");
                logInfo("  Total CPU Cores: " + (currentConfig.numHosts * currentConfig.hostCores));
                logInfo("  Total RAM: " + (currentConfig.numHosts * currentConfig.hostRamGb) + " GB");
                logInfo("  Total Storage: " + totalStorageGb() + " GB");
                logInfo("");
                logInfo("Cost Model:");
                logInfo(String.format("  Compute Cost:  $%.2f/hour", hourlyComputeCost));
                logInfo(String.format("  Storage Cost:  $%.2f/hour", hourlyStorageCost));
                logInfo(String.format("  Network Cost:  $%.2f/hour", hourlyNetworkCost));
                logInfo(String.format("  Total Hourly:  $%.2f/hour", totalHourlyCost));
                logInfo(String.format("  Runtime Target: %d seconds | SLA latency target %.1f ms", simulationSeconds, currentConfig.maxLatencyMs));
                logInfo(String.format("  Intent Profile: %s", profile));
                logInfo(String.format("  Curve Profile: exp=%.2f, burst@%.2f, width=%.2f, oscCycles=%.2f",
                    curveExponent, burstCenter, burstWidth, oscillationCycles));
                logInfo(String.format("  RL Impact: latencyGain=%.1f%%, costGain=%.1f%%, scoreGain=%.1f%%",
                    currentConfig.rlLatencyImprovementPercent,
                    currentConfig.rlCostImprovementPercent,
                    currentConfig.rlScoreImprovementPercent));
                logInfo("================================================");
                logInfo("");

                // Simulate execution steps
                for (int step = 0; step <= 100 && isRunning; step++) {
                    if (isPaused) {
                        Thread.sleep(100);
                        continue;
                    }

                    double progress = step / 100.0;
                    int elapsedSeconds = (int) Math.round(progress * simulationSeconds);
                    totalSimulationCost = (elapsedSeconds / 3600.0) * totalHourlyCost;

                    double convergence = Math.pow(Math.max(0.0, 1.0 - progress), curveExponent);
                    double oscillation = Math.sin((progress * 2.0 * Math.PI * oscillationCycles) + phaseShift) *
                        (targetLatency * oscillationAmplitudeFactor) * (0.6 + currentConfig.systemLoad);
                    double transientBurst = Math.exp(-Math.pow((progress - burstCenter) / burstWidth, 2)) *
                        targetLatency * burstAmplitudeFactor;
                    double deterministicRipple = Math.sin(progress * 13.0 + (intentHash % 11)) *
                        targetLatency * 0.006;

                    avgLatency = targetLatency +
                        (baselineLatency - targetLatency) * convergence +
                        transientBurst + oscillation + deterministicRipple;

                    if ("SPREAD".equalsIgnoreCase(currentConfig.vmPlacementPolicy)) {
                        avgLatency *= 0.95;
                    } else if ("CONSOLIDATED".equalsIgnoreCase(currentConfig.vmPlacementPolicy)) {
                        avgLatency *= 1.06;
                    } else if ("ISOLATED".equalsIgnoreCase(currentConfig.vmPlacementPolicy)) {
                        avgLatency *= 0.97;
                    }

                    avgLatency = clamp(8.0, 2000.0, avgLatency);

                    cpuUtilization = clamp(
                        10.0,
                        98.0,
                        22.0 + (progress * 52.0) + (currentConfig.systemLoad * 22.0)
                            - (currentConfig.costPriority * 8.0)
                    );

                    memoryUtilization = clamp(
                        15.0,
                        97.0,
                        28.0 + (progress * 45.0) + (currentConfig.securityPriority * 16.0)
                    );

                    double completionRatio = Math.pow(progress, completionExponent);
                    totalCloudletsProcessed = Math.min(
                        currentConfig.numCloudlets,
                        (int) Math.round(currentConfig.numCloudlets * completionRatio)
                    );

                    double imbalance = Math.max(0.0, currentConfig.systemLoad - (throughputCapacity / (currentConfig.numCloudlets + 1.0)));
                    vmMigrations = (int) Math.round(imbalance * currentConfig.numVMs * progress);

                    if (step > 0 && step % 25 == 0 && currentConfig.rlApplied) {
                        double adjustment = clamp(0.0, 0.08,
                            rlEffectStrength * (0.6 + progress));
                        avgLatency *= (1.0 - adjustment);
                        logInfo(String.format("[RL Adaptation] Step %d: policy adjusted latency by -%.1f%%",
                            step, adjustment * 100.0));
                    }

                    double runRatePerHour = totalSimulationCost * 3600.0 / Math.max(1.0, elapsedSeconds + 1.0);
                    double latencyTolerance = currentConfig.maxLatencyMs * 1.06;
                    double costTolerance = currentConfig.maxCostPerHour * 1.04;
                    if (step >= warmupGraceSteps && step % slaCheckEverySteps == 0) {
                        slaChecks++;
                        if (avgLatency > latencyTolerance || runRatePerHour > costTolerance) {
                            slaViolations++;
                        }
                    }

                    int finalStep = step;
                    double finalLatency = avgLatency;
                    double finalCpu = cpuUtilization;
                    int finalElapsedSeconds = elapsedSeconds;
                    Platform.runLater(() -> {
                        statusLabel.setText("Running - " + finalStep + "%");
                        statusLabel.setStyle("-fx-text-fill: #3498db;");
                        progressBar.setProgress(finalStep / 100.0);
                        timeLabel.setText(String.format("Time: %ds | Cost: $%.2f | Latency: %.0fms | CPU: %.0f%%",
                                finalElapsedSeconds, totalSimulationCost, finalLatency, finalCpu));

                        if (finalStep % 5 == 0) {
                            latencySeries.getData().add(new XYChart.Data<>(finalElapsedSeconds + "s", finalLatency));
                        }

                        if (costDurationValue != null) {
                            costDurationValue.setText(String.format("Elapsed: %ds | Runtime cost: $%.2f",
                                finalElapsedSeconds, totalSimulationCost));
                        }
                    });

                    // Log periodic events
                    if (step % 20 == 0 && step > 0) {
                        logInfo(String.format("[%ds] Processed %d/%d cloudlets | Latency: %.1f ms | CPU: %.0f%%",
                                elapsedSeconds, totalCloudletsProcessed,
                                currentConfig.numCloudlets, avgLatency, cpuUtilization));
                    }

                    if (step % 30 == 0 && step > 0 && step < 100 && vmMigrations > 0) {
                        logInfo(String.format("[%ds] VM rebalancing triggered (migrations=%d)", elapsedSeconds, vmMigrations));
                    }

                    Thread.sleep(100);
                }

                if (isRunning) {
                    totalSimulationCost = (simulationSeconds / 3600.0) * totalHourlyCost;
                    
                    Platform.runLater(() -> {
                        statusLabel.setText("✓ Completed");
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");
                        progressBar.setProgress(1.0);
                        startButton.setDisable(false);
                        pauseButton.setDisable(true);
                        stopButton.setDisable(true);
                        refreshDisplayPanels();
                        if (costDurationValue != null) {
                            costDurationValue.setText(String.format("Completed: %.0fs | Total runtime cost: $%.2f",
                                (double) simulationSeconds, totalSimulationCost));
                        }
                    });

                    logInfo("");
                    logInfo("================================================");
                    logInfo("Simulation Completed Successfully");
                    logInfo("================================================");
                    logInfo("Final Results:");
                    logInfo(String.format("  Total Execution Time:     %.2f minutes", simulationSeconds / 60.0));
                    logInfo(String.format("  Total Cost:              $%.2f", totalSimulationCost));
                    logInfo(String.format("  Average Cost per Hour:   $%.2f", totalHourlyCost));
                    logInfo(String.format("  Average Latency:         %.1f ms", avgLatency));
                    logInfo(String.format("  CPU Utilization:         %.1f%%", cpuUtilization));
                    logInfo(String.format("  Memory Utilization:      %.1f%%", memoryUtilization));
                    logInfo(String.format("  Cloudlets Processed:     %d/%d", totalCloudletsProcessed, currentConfig.numCloudlets));
                    logInfo(String.format("  VM Migrations:           %d", vmMigrations));
                    logInfo(String.format("  SLA Violations:          %d/%d checks", slaViolations, Math.max(1, slaChecks)));
                    
                    // SLA compliance check
                    if (slaViolations == 0 && slaChecks > 0) {
                        logInfo(String.format("  SLA Compliance:          ✓ PASSED (100%%)"));
                    } else {
                        double slaCompliance = (1.0 - (double) slaViolations / Math.max(1, slaChecks)) * 100;
                        logInfo(String.format("  SLA Compliance:          %.1f%%", slaCompliance));
                    }
                    
                    logInfo("================================================");
                }

                isRunning = false;
            } catch (Exception e) {
                logError("Simulation error: " + e.getMessage());
                Platform.runLater(() -> {
                    statusLabel.setText("✗ Error");
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    startButton.setDisable(false);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                });
                isRunning = false;
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    private double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Pause simulation
     */
    private void pauseSimulation() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseButton.setText("▶ Resume");
            statusLabel.setText("⏸ Paused");
            statusLabel.setStyle("-fx-text-fill: #f39c12;");
            logInfo("Simulation paused");
        } else {
            pauseButton.setText("⏸ Pause");
            statusLabel.setText("Running");
            statusLabel.setStyle("-fx-text-fill: #3498db;");
            logInfo("Simulation resumed");
        }
    }

    /**
     * Stop simulation
     */
    private void stopSimulation() {
        isRunning = false;
        isPaused = false;
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        pauseButton.setText("⏸ Pause");
        stopButton.setDisable(true);
        statusLabel.setText("Stopped");
        statusLabel.setStyle("-fx-text-fill: #95a5a6;");
        logInfo("Simulation stopped by user");
    }

    /**
     * Log info message
     */
    private void logInfo(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[INFO] " + message + "\n");
        });
    }

    /**
     * Log error message
     */
    private void logError(String message) {
        Platform.runLater(() -> {
            logArea.appendText("[ERROR] " + message + "\n");
        });
    }

    /**
     * Get current configuration
     */
    public CloudConfig getCurrentConfig() {
        return currentConfig;
    }

    /**
     * Set configuration
     */
    public void setCurrentConfig(CloudConfig config) {
        this.currentConfig = config;
        refreshDisplayPanels();
        logInfo("Configuration updated from previous tab");
    }

    private IntentProfile resolveIntentProfile() {
        double latency = currentConfig.latencyPriority;
        double cost = currentConfig.costPriority;
        double security = currentConfig.securityPriority;
        double carbon = currentConfig.carbonPriority;

        if (latency >= cost && latency >= security && latency >= carbon && latency >= 0.35) {
            return IntentProfile.LATENCY_FIRST;
        }
        if (cost >= latency && cost >= security && cost >= carbon && cost >= 0.35) {
            return IntentProfile.COST_FIRST;
        }
        if (security >= latency && security >= cost && security >= carbon && security >= 0.30) {
            return IntentProfile.SECURITY_FIRST;
        }
        if (carbon >= latency && carbon >= cost && carbon >= security && carbon >= 0.25) {
            return IntentProfile.GREEN_FIRST;
        }
        return IntentProfile.BALANCED;
    }

    private record CostBreakdown(double compute, double storage,
                                 double network, double total) {}
}
