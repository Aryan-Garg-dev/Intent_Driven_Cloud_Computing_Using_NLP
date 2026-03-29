package org.intentcloudsim.api;

import org.intentcloudsim.intent.Intent;
import org.intentcloudsim.intent.IntentHistoryLearner;
import org.intentcloudsim.intent.NaturalLanguageIntentParser;
import org.intentcloudsim.rl.ReinforcementIntentRefiner;
import org.intentcloudsim.sla.SLAContract;
import org.intentcloudsim.sla.SLANegotiationAgent;
import org.intentcloudsim.tradeoff.CostPerformanceTradeoffEngine;

/**
 * UI-friendly service facade for intent parsing + RL refinement + SLA/tradeoff.
 *
 * This class can be directly called from a REST controller or desktop UI layer.
 */
public class IntentPipelineService {

    private final IntentHistoryLearner learner;
    private final ReinforcementIntentRefiner rlRefiner;
    private final SLANegotiationAgent slaAgent;
    private final CostPerformanceTradeoffEngine tradeoffEngine;

    public IntentPipelineService() {
        this.learner = new IntentHistoryLearner();
        this.rlRefiner = new ReinforcementIntentRefiner();
        this.slaAgent = new SLANegotiationAgent();
        this.tradeoffEngine = new CostPerformanceTradeoffEngine();
    }

    public AnalysisResult analyzeIntent(String userId, String userInput, double systemLoad) {
        Intent parsedIntent = NaturalLanguageIntentParser.parse(userInput);
        Intent predictedIntent = learner.predict(userId);
        Intent refinedIntent = rlRefiner.refineIntent(parsedIntent, systemLoad, predictedIntent, userId);
        learner.learn(userId, parsedIntent);

        SLAContract sla = slaAgent.negotiate(refinedIntent);

        double[] candidateCosts = {2.0, 5.0, 10.0, 15.0};
        double[] candidateLatencies = {150.0, 80.0, 40.0, 20.0};

        int bestOption = tradeoffEngine.findBestOption(candidateCosts, candidateLatencies, refinedIntent);
        double selectedCost = candidateCosts[bestOption];
        double selectedLatency = candidateLatencies[bestOption];
        double tradeoffScore = tradeoffEngine.score(selectedCost, selectedLatency, refinedIntent);

        return new AnalysisResult(
            userId,
            userInput,
            parsedIntent,
            predictedIntent,
            refinedIntent,
            sla,
            bestOption,
            selectedCost,
            selectedLatency,
            tradeoffScore
        );
    }

    public FeedbackResult applyFeedback(String userId, boolean slaMet,
                                        double observedCost, double observedLatency) {
        double reward = rlRefiner.updateFromFeedback(userId, slaMet, observedCost, observedLatency);
        return new FeedbackResult(userId, slaMet, observedCost, observedLatency, reward);
    }

    public record AnalysisResult(
        String userId,
        String userInput,
        Intent parsedIntent,
        Intent predictedIntent,
        Intent refinedIntent,
        SLAContract slaContract,
        int bestOption,
        double selectedCost,
        double selectedLatency,
        double tradeoffScore
    ) {}

    public record FeedbackResult(
        String userId,
        boolean slaMet,
        double observedCost,
        double observedLatency,
        double reward
    ) {}
}
