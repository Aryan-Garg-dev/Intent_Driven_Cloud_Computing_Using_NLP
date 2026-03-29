package org.intentcloudsim.rl;

import org.intentcloudsim.intent.Intent;
import org.junit.Assert;
import org.junit.Test;

public class ReinforcementIntentRefinerTest {

    @Test
    public void refineIntent_shouldReturnValidBoundedIntent() {
        ReinforcementIntentRefiner refiner = new ReinforcementIntentRefiner();
        Intent parserIntent = new Intent(0.8, 0.7, 0.4, 0.2);
        Intent history = new Intent(0.6, 0.5, 0.5, 0.3);

        Intent refined = refiner.refineIntent(parserIntent, 0.65, history, "userA");

        Assert.assertTrue(refined.getCostPriority() >= 0.0 && refined.getCostPriority() <= 1.0);
        Assert.assertTrue(refined.getLatencyPriority() >= 0.0 && refined.getLatencyPriority() <= 1.0);
        Assert.assertTrue(refined.getSecurityPriority() >= 0.0 && refined.getSecurityPriority() <= 1.0);
        Assert.assertTrue(refined.getCarbonPriority() >= 0.0 && refined.getCarbonPriority() <= 1.0);
    }

    @Test
    public void updateFromFeedback_shouldReturnNonZeroRewardAfterRefine() {
        ReinforcementIntentRefiner refiner = new ReinforcementIntentRefiner();
        Intent parserIntent = new Intent(0.7, 0.8, 0.5, 0.3);
        Intent history = new Intent(0.5, 0.6, 0.4, 0.4);

        refiner.refineIntent(parserIntent, 0.55, history, "userB");
        double reward = refiner.updateFromFeedback("userB", true, 5.0, 60.0);

        Assert.assertNotEquals(0.0, reward, 0.0001);
    }
}
