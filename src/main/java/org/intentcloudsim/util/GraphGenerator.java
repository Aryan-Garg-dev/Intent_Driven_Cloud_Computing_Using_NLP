package org.intentcloudsim.util;

import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

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

        System.out.println("[GraphGen] Generating SLA Satisfaction graph...");

        CategoryChart chart = new CategoryChartBuilder()
            .width(800).height(600)
            .title("SLA Satisfaction Rate by Intent Type")
            .xAxisTitle("Intent Type")
            .yAxisTitle("Satisfaction Rate (%)")
            .build();

        List<String> categories = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        for (int i = 0; i < intentTypes.length; i++) {
            categories.add(intentTypes[i]);
            values.add(satisfactionRates[i]);
        }

        chart.addSeries("Satisfaction Rate", categories, values);

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

        XYChart chart = new XYChartBuilder()
            .width(800).height(600)
            .title("Tradeoff Score vs Cost Priority")
            .xAxisTitle("Cost Priority")
            .yAxisTitle("Tradeoff Score")
            .build();

        chart.addSeries("Tradeoff Scores", costPriorities, scores)
             .setMarker(SeriesMarkers.DIAMOND);

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
     * Generate all graphs from simulation data.
     */
    public void generateAll() {
        // Sample data for demonstration
        generateCostVsLatencyGraph(
            new double[]{2.0, 5.0, 8.0, 12.0, 15.0, 3.0, 7.0, 10.0},
            new double[]{150, 80, 40, 20, 15, 120, 60, 35},
            new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}
        );

        generateSLASatisfactionGraph(
            new String[]{"Cost-focused", "Speed-focused",
                         "Security-focused", "Balanced", "Green"},
            new double[]{85, 92, 88, 90, 82}
        );

        generateTradeoffGraph(
            new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9},
            new double[]{15, 18, 22, 28, 35, 40, 38, 32, 25}
        );

        generateLearningGraph(
            new double[]{1, 5, 10, 20, 30, 50, 75, 100},
            new double[]{30, 45, 58, 72, 80, 88, 92, 95}
        );

        System.out.println("[GraphGen] All graphs generated successfully!");
    }
}

