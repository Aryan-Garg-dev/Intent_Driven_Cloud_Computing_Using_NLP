package org.intentcloudsim.intent;

import org.junit.Assert;
import org.junit.Test;

public class NaturalLanguageIntentParserTest {

    @Test
    public void parse_shouldPrioritizeLatencyAndCost_forFastButNotExpensivePhrase() {
        Intent intent = NaturalLanguageIntentParser.parse("I need fast but not expensive servers");

        Assert.assertTrue("Latency should remain a top priority for 'fast'",
            intent.getLatencyPriority() >= 0.50);
        Assert.assertTrue("Cost should stay meaningful for 'not expensive'",
            intent.getCostPriority() >= 0.40);
        Assert.assertTrue("Latency should be weighted at least as much as cost",
            intent.getLatencyPriority() >= intent.getCostPriority());
    }

    @Test
    public void parse_shouldReduceLatencyPriority_whenSpeedDoesNotMatter() {
        Intent intent = NaturalLanguageIntentParser.parse("minimize cost and latency doesn't matter");

        Assert.assertTrue("Cost should remain significant", intent.getCostPriority() >= 0.55);
        Assert.assertTrue("Latency should be downweighted", intent.getLatencyPriority() <= 0.35);
    }

    @Test
    public void parse_shouldNotChooseLatencyFirst_whenLatencyIsNotImportant() {
        Intent intent = NaturalLanguageIntentParser.parse(
            "I run overnight video transcoding jobs for 10,000 files, cost must be minimal, latency is not important, keep power usage low, and maintain 99.9% availability"
        );

        Assert.assertTrue("Cost should dominate or tie when explicitly minimized",
            intent.getCostPriority() >= intent.getLatencyPriority());
        Assert.assertTrue("Latency should be downweighted by explicit negation",
            intent.getLatencyPriority() <= 0.30);
    }
}
