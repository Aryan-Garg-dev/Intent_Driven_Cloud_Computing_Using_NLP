package org.intentcloudsim.intent;

import org.junit.Assert;
import org.junit.Test;

public class NaturalLanguageIntentParserTest {

    @Test
    public void parse_shouldPrioritizeLatencyAndCost_forFastButNotExpensivePhrase() {
        Intent intent = NaturalLanguageIntentParser.parse("I need fast but not expensive servers");

        Assert.assertTrue("Latency should be high for 'fast'", intent.getLatencyPriority() >= 0.60);
        Assert.assertTrue("Cost should be high for 'not expensive'", intent.getCostPriority() >= 0.55);
    }

    @Test
    public void parse_shouldReduceLatencyPriority_whenSpeedDoesNotMatter() {
        Intent intent = NaturalLanguageIntentParser.parse("minimize cost and latency doesn't matter");

        Assert.assertTrue("Cost should remain significant", intent.getCostPriority() >= 0.55);
        Assert.assertTrue("Latency should be downweighted", intent.getLatencyPriority() <= 0.35);
    }
}
