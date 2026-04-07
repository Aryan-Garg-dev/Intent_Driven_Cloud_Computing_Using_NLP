package org.intentcloudsim.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.intentcloudsim.intent.Intent;
import org.intentcloudsim.intent.IntentHistoryLearner;
import org.intentcloudsim.intent.NaturalLanguageIntentParser;
import org.intentcloudsim.rl.ReinforcementIntentRefiner;
import org.intentcloudsim.sla.SLAContract;
import org.intentcloudsim.sla.SLANegotiationAgent;
import org.intentcloudsim.tradeoff.CostPerformanceTradeoffEngine;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UI-friendly service facade for intent parsing + RL refinement + SLA/tradeoff.
 *
 * This class can be directly called from a REST controller or desktop UI layer.
 */
public class IntentPipelineService {

    private static final int HISTORY_WINDOW = 50;
    private static final int MATURE_RUN_COUNT = 10;
    private static final Path HISTORY_FILE =
        Paths.get("results", "rl_learning_history.json");

    private final IntentHistoryLearner learner;
    private final ReinforcementIntentRefiner rlRefiner;
    private final SLANegotiationAgent slaAgent;
    private final CostPerformanceTradeoffEngine tradeoffEngine;
    private final Map<String, AnalysisResult> latestAnalysisByUser;
    private final Map<String, Deque<RunOutcome>> runHistoryByUser;
    private final Gson gson;

    public IntentPipelineService() {
        this.learner = new IntentHistoryLearner();
        this.rlRefiner = new ReinforcementIntentRefiner();
        this.slaAgent = new SLANegotiationAgent();
        this.tradeoffEngine = new CostPerformanceTradeoffEngine();
        this.latestAnalysisByUser = new HashMap<>();
        this.runHistoryByUser = new HashMap<>();
        this.gson = new Gson();

        loadHistoryFromDisk();
    }

    public AnalysisResult analyzeIntent(String userId, String userInput, double systemLoad) {
        Intent parsedIntent = NaturalLanguageIntentParser.parse(userInput);
        Intent predictedIntent = learner.predict(userId);
        Intent refinedIntent = rlRefiner.refineIntent(parsedIntent, systemLoad, predictedIntent, userId);
        learner.learn(userId, parsedIntent);

        Evaluation baseline = evaluate(parsedIntent, systemLoad);
        Evaluation refined = evaluate(refinedIntent, systemLoad);

        AnalysisResult analysis = new AnalysisResult(
            userId,
            userInput,
            parsedIntent,
            predictedIntent,
            refinedIntent,
            refined.slaContract(),
            refined.candidateCosts(),
            refined.candidateLatencies(),
            refined.bestOption(),
            refined.selectedCost(),
            refined.selectedLatency(),
            refined.tradeoffScore(),
            baseline.slaContract(),
            baseline.candidateCosts(),
            baseline.candidateLatencies(),
            baseline.bestOption(),
            baseline.selectedCost(),
            baseline.selectedLatency(),
            baseline.tradeoffScore()
        );

        latestAnalysisByUser.put(userId, analysis);
        return analysis;
    }

    public FeedbackResult applyFeedback(String userId, boolean slaMet,
                                        double observedCost, double observedLatency) {
        double reward = rlRefiner.updateFromFeedback(userId, slaMet, observedCost, observedLatency);

        AnalysisResult latest = latestAnalysisByUser.get(userId);
        Deque<RunOutcome> history = runHistoryByUser.computeIfAbsent(userId, k -> new ArrayDeque<>());

        double latencyImprovement = 0.0;
        double costImprovement = 0.0;
        double scoreImprovement = 0.0;
        if (latest != null) {
            latencyImprovement = improvementPercent(
                latest.baselineSelectedLatency(), latest.selectedLatency());
            costImprovement = improvementPercent(
                latest.baselineSelectedCost(), latest.selectedCost());
            scoreImprovement = scoreImprovementPercent(
                latest.baselineTradeoffScore(), latest.tradeoffScore());
        }

        history.addLast(new RunOutcome(latencyImprovement, costImprovement,
            scoreImprovement, reward, slaMet));
        while (history.size() > HISTORY_WINDOW) {
            history.removeFirst();
        }

        saveHistoryToDisk();

        LearningStats stats = summarizeHistory(history);
        return new FeedbackResult(userId, slaMet, observedCost, observedLatency, reward, stats);
    }

    public record AnalysisResult(
        String userId,
        String userInput,
        Intent parsedIntent,
        Intent predictedIntent,
        Intent refinedIntent,
        SLAContract slaContract,
        double[] candidateCosts,
        double[] candidateLatencies,
        int bestOption,
        double selectedCost,
        double selectedLatency,
        double tradeoffScore,
        SLAContract baselineSlaContract,
        double[] baselineCandidateCosts,
        double[] baselineCandidateLatencies,
        int baselineBestOption,
        double baselineSelectedCost,
        double baselineSelectedLatency,
        double baselineTradeoffScore
    ) {}

    public record FeedbackResult(
        String userId,
        boolean slaMet,
        double observedCost,
        double observedLatency,
        double reward,
        LearningStats learningStats
    ) {}

    public record LearningStats(
        int runCount,
        boolean mature,
        double maturityScore,
        double avgLatencyImprovementPercent,
        double avgCostImprovementPercent,
        double avgScoreImprovementPercent,
        double avgReward,
        double rewardTrend,
        double slaSuccessRate
    ) {}

    private double[] buildCandidateCosts(SLAContract sla, Intent refinedIntent) {
        double budgetCap = Math.max(0.5, sla.getMaxCostPerHour());
        double scale = 0.80 + (refinedIntent.getLatencyPriority() * 0.30) +
            (refinedIntent.getSecurityPriority() * 0.20);

        return new double[] {
            round2(Math.max(0.5, budgetCap * 0.35 * scale)),
            round2(Math.max(0.8, budgetCap * 0.60 * scale)),
            round2(Math.max(1.2, budgetCap * 0.85 * scale)),
            round2(Math.max(1.5, budgetCap * 1.00 * scale))
        };
    }

    private double[] buildCandidateLatencies(SLAContract sla,
                                             Intent refinedIntent,
                                             double systemLoad) {
        double target = Math.max(12.0, sla.getMaxLatencyMs());
        double loadMultiplier = 1.0 + (Math.max(0.0, Math.min(1.0, systemLoad)) * 0.40);
        double perfBias = 1.0 - (refinedIntent.getLatencyPriority() * 0.18);

        return new double[] {
            round1(target * 1.70 * loadMultiplier),
            round1(target * 1.30 * loadMultiplier * perfBias),
            round1(target * 1.00 * loadMultiplier * perfBias),
            round1(target * 0.72 * loadMultiplier * perfBias)
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private Evaluation evaluate(Intent intent, double systemLoad) {
        SLAContract sla = slaAgent.negotiate(intent);

        double[] candidateCosts = buildCandidateCosts(sla, intent);
        double[] candidateLatencies = buildCandidateLatencies(sla, intent, systemLoad);

        int bestOption = tradeoffEngine.findBestOption(candidateCosts, candidateLatencies, intent);
        double selectedCost = candidateCosts[bestOption];
        double selectedLatency = candidateLatencies[bestOption];
        double tradeoffScore = tradeoffEngine.score(selectedCost, selectedLatency, intent);

        return new Evaluation(
            sla,
            candidateCosts,
            candidateLatencies,
            bestOption,
            selectedCost,
            selectedLatency,
            tradeoffScore
        );
    }

    private record Evaluation(
        SLAContract slaContract,
        double[] candidateCosts,
        double[] candidateLatencies,
        int bestOption,
        double selectedCost,
        double selectedLatency,
        double tradeoffScore
    ) {}

    private record RunOutcome(
        double latencyImprovement,
        double costImprovement,
        double scoreImprovement,
        double reward,
        boolean slaMet
    ) {}

    private LearningStats summarizeHistory(Deque<RunOutcome> history) {
        int size = history.size();
        if (size == 0) {
            return new LearningStats(0, false, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        double latencySum = 0.0;
        double costSum = 0.0;
        double scoreSum = 0.0;
        double rewardSum = 0.0;
        int successCount = 0;

        double recentRewardSum = 0.0;
        double previousRewardSum = 0.0;
        int recentCount = 0;
        int previousCount = 0;

        int split = Math.max(1, size / 2);
        int idx = 0;
        for (RunOutcome run : history) {
            latencySum += run.latencyImprovement();
            costSum += run.costImprovement();
            scoreSum += run.scoreImprovement();
            rewardSum += run.reward();
            if (run.slaMet()) successCount++;

            if (idx < split) {
                previousRewardSum += run.reward();
                previousCount++;
            } else {
                recentRewardSum += run.reward();
                recentCount++;
            }
            idx++;
        }

        double avgReward = rewardSum / size;
        double rewardTrend = (recentRewardSum / Math.max(1, recentCount)) -
            (previousRewardSum / Math.max(1, previousCount));
        double maturityScore = Math.min(1.0, size / (double) MATURE_RUN_COUNT);

        return new LearningStats(
            size,
            size >= MATURE_RUN_COUNT,
            maturityScore,
            latencySum / size,
            costSum / size,
            scoreSum / size,
            avgReward,
            rewardTrend,
            successCount / (double) size
        );
    }

    private double improvementPercent(double baseline, double candidate) {
        if (baseline <= 0.0) return 0.0;
        return ((baseline - candidate) / baseline) * 100.0;
    }

    private double scoreImprovementPercent(double baselineScore, double candidateScore) {
        if (Math.abs(baselineScore) < 1e-9) {
            return candidateScore > 0.0 ? 100.0 : 0.0;
        }
        return ((candidateScore - baselineScore) / Math.abs(baselineScore)) * 100.0;
    }

    private void loadHistoryFromDisk() {
        try {
            if (!Files.exists(HISTORY_FILE)) {
                return;
            }

            String json = Files.readString(HISTORY_FILE);
            if (json == null || json.isBlank()) {
                return;
            }

            Type type = new TypeToken<Map<String, List<RunOutcome>>>() {}.getType();
            Map<String, List<RunOutcome>> loaded = gson.fromJson(json, type);
            if (loaded == null || loaded.isEmpty()) {
                return;
            }

            int totalRuns = 0;
            for (Map.Entry<String, List<RunOutcome>> entry : loaded.entrySet()) {
                Deque<RunOutcome> deque = new ArrayDeque<>();
                if (entry.getValue() != null) {
                    for (RunOutcome run : entry.getValue()) {
                        deque.addLast(run);
                    }
                }

                while (deque.size() > HISTORY_WINDOW) {
                    deque.removeFirst();
                }

                runHistoryByUser.put(entry.getKey(), deque);
                totalRuns += deque.size();
            }

            rlRefiner.warmStart(totalRuns);
        } catch (Exception e) {
            System.out.println("[IntentPipelineService] Failed to load RL history: " + e.getMessage());
        }
    }

    private void saveHistoryToDisk() {
        try {
            Files.createDirectories(HISTORY_FILE.getParent());

            Map<String, List<RunOutcome>> serializable = new HashMap<>();
            for (Map.Entry<String, Deque<RunOutcome>> entry : runHistoryByUser.entrySet()) {
                serializable.put(entry.getKey(), List.copyOf(entry.getValue()));
            }

            String json = gson.toJson(serializable);
            Files.writeString(HISTORY_FILE, json);
        } catch (IOException e) {
            System.out.println("[IntentPipelineService] Failed to persist RL history: " + e.getMessage());
        }
    }
}
