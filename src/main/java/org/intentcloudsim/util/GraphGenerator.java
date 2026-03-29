package org.intentcloudsim.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Generates publication-quality graphs for patent and paper.
 * Uses XChart library.
 */
public class GraphGenerator {

    private String outputDir;

    public GraphGenerator(String outputDir) {
        this.outputDir = outputDir;
    }

    public GraphGenerator() {
        this("results");
    }

    /**
     * Generate Cost vs Latency scatter plot for different intents.
     */
    public void generateCostVsLatencyGraph(
            double[] costs, double[] latencies, String[] labels) {

        System.out.println("[GraphGen] Generating Cost vs Latency graph...");

        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("Cost vs Latency by User Intent")
            .xAxisTitle("Cost ($/hour)")
            .yAxisTitle("Latency (ms)")
            .build();

        chart.addSeries("Configurations", costs, latencies)
             .setMarker(SeriesMarkers.CIRCLE);

        try {
            // Create results directory if it doesn't exist
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BitmapEncoder.saveBitmap(chart,
                outputDir + "/cost_vs_latency",
                BitmapEncoder.BitmapFormat.PNG);
            System.out.println("[GraphGen] Saved: " + outputDir +
                               "/cost_vs_latency.png");
        } catch (IOException e) {
            System.err.println("[GraphGen] Error: " + e.getMessage());
        }
    }

    /**
     * Generate SLA Satisfaction bar chart.
     */
    public void generateSLASatisfactionGraph(
            String[] intentTypes, double[] satisfactionRates) {

        double[] violationRates = new double[satisfactionRates.length];
        for (int i = 0; i < satisfactionRates.length; i++) {
            violationRates[i] = 100.0 - satisfactionRates[i];
        }

        generateSLASatisfactionGraph(intentTypes, satisfactionRates, violationRates);
    }

    /**
     * Generate SLA success vs violation bar chart.
     */
    public void generateSLASatisfactionGraph(
            String[] intentTypes, double[] satisfactionRates, double[] violationRates) {

        System.out.println("[GraphGen] Generating SLA Satisfaction graph...");

        CategoryChart chart = new CategoryChartBuilder()
            .width(800).height(600)
            .title("SLA Success vs Violation by Intent Type")
            .xAxisTitle("Intent Type")
            .yAxisTitle("Rate (%)")
            .build();

        chart.getStyler().setYAxisMin(0.0);
        chart.getStyler().setYAxisMax(100.0);

        List<String> categories = new ArrayList<>();
        List<Number> successValues = new ArrayList<>();
        List<Number> violationValues = new ArrayList<>();
        for (int i = 0; i < intentTypes.length; i++) {
            categories.add(intentTypes[i]);
            successValues.add(satisfactionRates[i]);
            violationValues.add(violationRates[i]);
        }

        chart.addSeries("SLA Satisfied (%)", categories, successValues);
        chart.addSeries("SLA Violated (%)", categories, violationValues);

        try {
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BitmapEncoder.saveBitmap(chart,
                outputDir + "/sla_satisfaction",
                BitmapEncoder.BitmapFormat.PNG);
            System.out.println("[GraphGen] Saved: " + outputDir +
                               "/sla_satisfaction.png");
        } catch (IOException e) {
            System.err.println("[GraphGen] Error: " + e.getMessage());
        }
    }

    /**
     * Generate Tradeoff Score comparison.
     */
    public void generateTradeoffGraph(
            double[] costPriorities, double[] scores) {

        System.out.println("[GraphGen] Generating Tradeoff Score graph...");

        CategoryChart chart = new CategoryChartBuilder()
            .width(800).height(600)
            .title("Tradeoff Score by Experiment")
            .xAxisTitle("Experiment (Cost Priority)")
            .yAxisTitle("Tradeoff Score")
            .build();

        List<String> categories = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        for (int i = 0; i < scores.length; i++) {
            String label = String.format(Locale.ROOT, "E%d (c=%.2f)", i + 1,
                i < costPriorities.length ? costPriorities[i] : 0.0);
            categories.add(label);
            values.add(scores[i]);
        }

        chart.addSeries("Tradeoff Score", categories, values);

        try {
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BitmapEncoder.saveBitmap(chart,
                outputDir + "/tradeoff_scores",
                BitmapEncoder.BitmapFormat.PNG);
            System.out.println("[GraphGen] Saved: " + outputDir +
                               "/tradeoff_scores.png");
        } catch (IOException e) {
            System.err.println("[GraphGen] Error: " + e.getMessage());
        }
    }

    /**
     * Generate Intent Learning accuracy over time.
     */
    public void generateLearningGraph(
            double[] iterations, double[] accuracy) {

        System.out.println("[GraphGen] Generating Learning Accuracy graph...");

        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("Intent Prediction Accuracy Over Time")
            .xAxisTitle("Number of Interactions")
            .yAxisTitle("Prediction Accuracy (%)")
            .build();

        chart.addSeries("Accuracy", iterations, accuracy)
             .setMarker(SeriesMarkers.SQUARE);

        try {
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BitmapEncoder.saveBitmap(chart,
                outputDir + "/learning_accuracy",
                BitmapEncoder.BitmapFormat.PNG);
            System.out.println("[GraphGen] Saved: " + outputDir +
                               "/learning_accuracy.png");
        } catch (IOException e) {
            System.err.println("[GraphGen] Error: " + e.getMessage());
        }
    }

    /**
     * Generate RL reward trend graph from feedback loop.
     */
    public void generateRLRewardGraph(double[] iterations, double[] rewards) {

        System.out.println("[GraphGen] Generating RL Reward Trend graph...");

        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("RL Reward Trend Over Interactions")
            .xAxisTitle("Interaction #")
            .yAxisTitle("Reward")
            .build();

        chart.addSeries("RL Reward", iterations, rewards)
             .setMarker(SeriesMarkers.TRIANGLE_DOWN);

        try {
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            BitmapEncoder.saveBitmap(chart,
                outputDir + "/rl_reward_trend",
                BitmapEncoder.BitmapFormat.PNG);
            System.out.println("[GraphGen] Saved: " + outputDir +
                               "/rl_reward_trend.png");
        } catch (IOException e) {
            System.err.println("[GraphGen] Error: " + e.getMessage());
        }
    }

    /**
     * Generate all graphs from simulation data.
     */
    public void generateAll() {
        generateAll("simulation_results.csv");
    }

    /**
     * Generate all graphs from actual CSV run output.
     */
    public void generateAll(String csvFilename) {
        String csvPath = outputDir + "/" + csvFilename;

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            List<String[]> rows = reader.readAll();
            if (rows.size() <= 1) {
                System.out.println("[GraphGen] No simulation rows found in " + csvPath);
                return;
            }

            String[] header = rows.get(0);
            Map<String, Integer> idx = indexMap(header);

            List<Double> costs = new ArrayList<>();
            List<Double> latencies = new ArrayList<>();
            List<Double> costPriorities = new ArrayList<>();
            List<Double> tradeoffScores = new ArrayList<>();

            List<Double> interactions = new ArrayList<>();
            List<Double> cumulativeSlaRate = new ArrayList<>();

            Map<String, int[]> slaByIntentType = new LinkedHashMap<>();
            int runningSlaMet = 0;
            int interaction = 0;

            for (int r = 1; r < rows.size(); r++) {
                String[] row = rows.get(r);
                if (row.length < header.length) continue;

                double actualCost = parseDouble(row, idx, "ActualCost", 0.0);
                double actualLatency = parseDouble(row, idx, "ActualLatency", 0.0);
                double costPriority = parseDouble(row, idx, "CostPriority", 0.0);
                double tradeoffScore = parseDouble(row, idx, "TradeoffScore", 0.0);
                boolean slaSatisfied = parseBoolean(row, idx, "SLASatisfied", false);

                costs.add(actualCost);
                latencies.add(actualLatency);
                costPriorities.add(costPriority);
                tradeoffScores.add(tradeoffScore);

                interaction++;
                if (slaSatisfied) runningSlaMet++;
                interactions.add((double) interaction);
                cumulativeSlaRate.add((runningSlaMet * 100.0) / interaction);

                String intentType = dominantIntentType(row, idx);
                int[] stats = slaByIntentType.computeIfAbsent(intentType, k -> new int[]{0, 0});
                stats[0]++; // total
                if (slaSatisfied) stats[1]++; // satisfied
            }

            generateCostVsLatencyGraph(
                toPrimitive(costs),
                toPrimitive(latencies),
                buildLabels(costs.size())
            );

            // Sort by cost priority so the chart reads left-to-right coherently.
            List<double[]> pairs = new ArrayList<>();
            for (int i = 0; i < costPriorities.size(); i++) {
                pairs.add(new double[]{costPriorities.get(i), tradeoffScores.get(i)});
            }
            pairs.sort((a, b) -> Double.compare(a[0], b[0]));

            List<Double> sortedCostPriorities = new ArrayList<>();
            List<Double> sortedTradeoffScores = new ArrayList<>();
            for (double[] pair : pairs) {
                sortedCostPriorities.add(pair[0]);
                sortedTradeoffScores.add(pair[1]);
            }

            generateTradeoffGraph(toPrimitive(sortedCostPriorities), toPrimitive(sortedTradeoffScores));

            List<String> typeLabels = new ArrayList<>();
            List<Double> typeRates = new ArrayList<>();
            List<Double> typeViolationRates = new ArrayList<>();
            for (Map.Entry<String, int[]> entry : slaByIntentType.entrySet()) {
                typeLabels.add(entry.getKey());
                int total = entry.getValue()[0];
                int sat = entry.getValue()[1];
                double successRate = total == 0 ? 0.0 : (sat * 100.0 / total);
                typeRates.add(successRate);
                typeViolationRates.add(100.0 - successRate);
            }

            generateSLASatisfactionGraph(
                typeLabels.toArray(new String[0]),
                toPrimitive(typeRates),
                toPrimitive(typeViolationRates)
            );

            generateLearningGraph(toPrimitive(interactions), toPrimitive(cumulativeSlaRate));
            System.out.println("[GraphGen] All graphs generated successfully from " + csvPath);

        } catch (IOException | CsvException e) {
            System.err.println("[GraphGen] Error reading CSV for graph generation: " + e.getMessage());
        }
    }

    private Map<String, Integer> indexMap(String[] header) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < header.length; i++) {
            map.put(header[i], i);
        }
        return map;
    }

    private double parseDouble(String[] row, Map<String, Integer> idx,
                               String key, double fallback) {
        Integer i = idx.get(key);
        if (i == null || i >= row.length) return fallback;
        try {
            return Double.parseDouble(row[i]);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private boolean parseBoolean(String[] row, Map<String, Integer> idx,
                                 String key, boolean fallback) {
        Integer i = idx.get(key);
        if (i == null || i >= row.length) return fallback;
        return Boolean.parseBoolean(row[i]);
    }

    private String dominantIntentType(String[] row, Map<String, Integer> idx) {
        double c = parseDouble(row, idx, "CostPriority", 0.0);
        double l = parseDouble(row, idx, "LatencyPriority", 0.0);
        double s = parseDouble(row, idx, "SecurityPriority", 0.0);

        double max = Math.max(c, Math.max(l, s));
        if (max <= 0.0) return "Unclassified";

        if (almostEqual(max, c) && almostEqual(max, l)) return "Balanced";
        if (almostEqual(max, c)) return "Cost-focused";
        if (almostEqual(max, l)) return "Latency-focused";
        return "Security-focused";
    }

    private boolean almostEqual(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    private String[] buildLabels(int size) {
        String[] labels = new String[size];
        for (int i = 0; i < size; i++) {
            labels[i] = String.format(Locale.ROOT, "E%d", i + 1);
        }
        return labels;
    }

    private double[] toPrimitive(List<Double> values) {
        double[] arr = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            arr[i] = values.get(i);
        }
        return arr;
    }
}

