package org.intentcloudsim.rl;

import org.intentcloudsim.intent.Intent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Lightweight Q-learning refiner that adapts intent vectors based on runtime feedback.
 *
 * State: quantized intent + system load
 * Action: adjust one priority or balance all
 * Reward: SLA success - alpha*cost - beta*latency
 */
public class ReinforcementIntentRefiner {

    private enum Action {
        BOOST_COST,
        BOOST_LATENCY,
        BOOST_SECURITY,
        BOOST_CARBON,
        BALANCE
    }

    private static class Transition {
        private final String stateKey;
        private final Action action;
        private final Intent refinedIntent;
        private final double systemLoad;

        private Transition(String stateKey, Action action,
                           Intent refinedIntent, double systemLoad) {
            this.stateKey = stateKey;
            this.action = action;
            this.refinedIntent = refinedIntent;
            this.systemLoad = systemLoad;
        }
    }

    private final Map<String, double[]> qTable = new HashMap<>();
    private final Map<String, Transition> activeTransitions = new HashMap<>();
    private final Random random;

    private final double alpha;
    private final double gamma;
    private final double epsilon;

    public ReinforcementIntentRefiner() {
        this(0.20, 0.85, 0.15, 42L);
    }

    public ReinforcementIntentRefiner(double alpha, double gamma,
                                      double epsilon, long seed) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.random = new Random(seed);
    }

    /**
     * Refine parser output intent using historical prediction + system context.
     */
    public Intent refineIntent(Intent parserIntent,
                               double systemLoad,
                               Intent historicalPrediction,
                               String userId) {

        Intent blended = blend(parserIntent, historicalPrediction, 0.70);
        String stateKey = stateKey(blended, systemLoad);
        Action action = chooseAction(stateKey);
        Intent refined = applyAction(blended, action);

        activeTransitions.put(userId,
            new Transition(stateKey, action, refined, systemLoad));

        System.out.printf(Locale.ROOT,
            "[RLRefiner] state=%s action=%s -> refinedIntent=%s%n",
            stateKey, action, refined);

        return refined;
    }

    /**
     * Update Q-values from observed outcome and return scalar reward.
     */
    public double updateFromFeedback(String userId,
                                     boolean slaMet,
                                     double observedCost,
                                     double observedLatency) {
        Transition t = activeTransitions.get(userId);
        if (t == null) {
            return 0.0;
        }

        double reward = computeReward(slaMet, observedCost, observedLatency);
        String nextState = stateKey(t.refinedIntent, t.systemLoad);

        double[] qValues = qTable.computeIfAbsent(t.stateKey,
            k -> new double[Action.values().length]);
        double[] nextQ = qTable.computeIfAbsent(nextState,
            k -> new double[Action.values().length]);

        int actionIndex = t.action.ordinal();
        double currentQ = qValues[actionIndex];
        double maxNext = max(nextQ);

        qValues[actionIndex] = currentQ +
            alpha * (reward + gamma * maxNext - currentQ);

        System.out.printf(Locale.ROOT,
            "[RLRefiner] feedback user=%s reward=%.3f updatedQ=%.3f%n",
            userId, reward, qValues[actionIndex]);

        return reward;
    }

    private Intent blend(Intent parserIntent,
                         Intent historyIntent,
                         double parserWeight) {
        double historyWeight = 1.0 - parserWeight;
        return new Intent(
            parserIntent.getCostPriority() * parserWeight +
                historyIntent.getCostPriority() * historyWeight,
            parserIntent.getLatencyPriority() * parserWeight +
                historyIntent.getLatencyPriority() * historyWeight,
            parserIntent.getSecurityPriority() * parserWeight +
                historyIntent.getSecurityPriority() * historyWeight,
            parserIntent.getCarbonPriority() * parserWeight +
                historyIntent.getCarbonPriority() * historyWeight
        );
    }

    private Action chooseAction(String stateKey) {
        if (random.nextDouble() < epsilon) {
            Action randomAction = Action.values()[random.nextInt(Action.values().length)];
            System.out.println("[RLRefiner] Exploration -> " + randomAction);
            return randomAction;
        }

        double[] qValues = qTable.computeIfAbsent(stateKey,
            k -> new double[Action.values().length]);

        int best = 0;
        for (int i = 1; i < qValues.length; i++) {
            if (qValues[i] > qValues[best]) {
                best = i;
            }
        }

        return Action.values()[best];
    }

    private Intent applyAction(Intent intent, Action action) {
        Intent updated = copy(intent);

        switch (action) {
            case BOOST_COST -> {
                updated.setCostPriority(updated.getCostPriority() + 0.12);
                updated.setLatencyPriority(updated.getLatencyPriority() - 0.05);
            }
            case BOOST_LATENCY -> {
                updated.setLatencyPriority(updated.getLatencyPriority() + 0.12);
                updated.setCostPriority(updated.getCostPriority() - 0.05);
            }
            case BOOST_SECURITY -> {
                updated.setSecurityPriority(updated.getSecurityPriority() + 0.12);
                updated.setCarbonPriority(updated.getCarbonPriority() - 0.04);
            }
            case BOOST_CARBON -> {
                updated.setCarbonPriority(updated.getCarbonPriority() + 0.12);
                updated.setLatencyPriority(updated.getLatencyPriority() - 0.03);
            }
            case BALANCE -> {
                double mean = (updated.getCostPriority() +
                    updated.getLatencyPriority() +
                    updated.getSecurityPriority() +
                    updated.getCarbonPriority()) / 4.0;
                updated.setCostPriority(0.5 * updated.getCostPriority() + 0.5 * mean);
                updated.setLatencyPriority(0.5 * updated.getLatencyPriority() + 0.5 * mean);
                updated.setSecurityPriority(0.5 * updated.getSecurityPriority() + 0.5 * mean);
                updated.setCarbonPriority(0.5 * updated.getCarbonPriority() + 0.5 * mean);
            }
        }

        return updated;
    }

    private String stateKey(Intent intent, double systemLoad) {
        int c = bucket(intent.getCostPriority());
        int l = bucket(intent.getLatencyPriority());
        int s = bucket(intent.getSecurityPriority());
        int g = bucket(intent.getCarbonPriority());
        int load = bucket(systemLoad);
        return c + "-" + l + "-" + s + "-" + g + "-" + load;
    }

    private int bucket(double value) {
        if (value < 0.25) return 0;
        if (value < 0.50) return 1;
        if (value < 0.75) return 2;
        return 3;
    }

    private double computeReward(boolean slaMet,
                                 double observedCost,
                                 double observedLatency) {
        double success = slaMet ? 1.0 : -1.0;
        double costPenalty = 0.08 * observedCost;
        double latencyPenalty = 0.25 * (observedLatency / 100.0);
        return success - costPenalty - latencyPenalty;
    }

    private Intent copy(Intent intent) {
        return new Intent(
            intent.getCostPriority(),
            intent.getLatencyPriority(),
            intent.getSecurityPriority(),
            intent.getCarbonPriority()
        );
    }

    private double max(double[] values) {
        double best = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > best) best = values[i];
        }
        return best;
    }
}
