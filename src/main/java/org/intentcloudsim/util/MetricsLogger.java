package org.intentcloudsim.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs all simulation metrics to CSV files for analysis and graphs.
 */
public class MetricsLogger {

    private String outputDir;
    private List<String[]> records;
    private static final String[] HEADERS = {
        "Timestamp", "UserIntent", "CostPriority", "LatencyPriority",
        "SecurityPriority", "SLAMaxLatency", "SLAMaxCost",
        "ActualLatency", "ActualCost", "HostSelected",
        "TradeoffScore", "SLASatisfied"
    };

    public MetricsLogger(String outputDir) {
        this.outputDir = outputDir;
        this.records = new ArrayList<>();
    }

    public MetricsLogger() {
        this("results");
    }

    /**
     * Add a record to the log.
     */
    public void addRecord(String userIntent, double costPriority,
                          double latencyPriority, double securityPriority,
                          double slaMaxLatency, double slaMaxCost,
                          double actualLatency, double actualCost,
                          int hostSelected, double tradeoffScore,
                          boolean slaSatisfied) {

        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        records.add(new String[]{
            timestamp, userIntent,
            String.format("%.2f", costPriority),
            String.format("%.2f", latencyPriority),
            String.format("%.2f", securityPriority),
            String.format("%.2f", slaMaxLatency),
            String.format("%.2f", slaMaxCost),
            String.format("%.2f", actualLatency),
            String.format("%.2f", actualCost),
            String.valueOf(hostSelected),
            String.format("%.2f", tradeoffScore),
            String.valueOf(slaSatisfied)
        });
    }

    /**
     * Write all records to a CSV file.
     */
    public void saveToCSV(String filename) {
        try {
            // Create results directory if it doesn't exist
            java.io.File dir = new java.io.File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filepath = outputDir + "/" + filename;
            PrintWriter writer = new PrintWriter(new FileWriter(filepath));

            // Write headers
            writer.println(String.join(",", HEADERS));

            // Write data
            for (String[] record : records) {
                writer.println(String.join(",", record));
            }

            writer.close();
            System.out.println("[MetricsLogger] Saved " + records.size() +
                               " records to " + filepath);

        } catch (IOException e) {
            System.err.println("[MetricsLogger] ERROR saving CSV: " +
                               e.getMessage());
        }
    }

    /**
     * Print a summary to console.
     */
    public void printSummary() {
        System.out.println("\n===== SIMULATION SUMMARY =====");
        System.out.println("Total experiments: " + records.size());

        int slaSatisfied = 0;
        double totalCost = 0, totalLatency = 0;

        for (String[] record : records) {
            if (Boolean.parseBoolean(record[11])) slaSatisfied++;
            totalCost += Double.parseDouble(record[8]);
            totalLatency += Double.parseDouble(record[7]);
        }

        if (!records.isEmpty()) {
            System.out.printf("SLA Satisfaction Rate: %.1f%%%n",
                (slaSatisfied * 100.0 / records.size()));
            System.out.printf("Average Cost: $%.2f%n",
                totalCost / records.size());
            System.out.printf("Average Latency: %.1f ms%n",
                totalLatency / records.size());
        }
        System.out.println("==============================\n");
    }

    /**
     * Simple static log method for quick messages.
     */
    public static void log(String message) {
        System.out.println("[LOG " +
            LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")) +
            "] " + message);
    }

    public int getRecordCount() {
        return records.size();
    }
}

