Project Structure:
CloudProject/
├── pom.xml                          ✅ (updated with all dependencies)
├── .gitignore                       ✅ (updated)
└── src/main/java/org/intentcloudsim/
├── MainSimulation.java          ✅ (main entry point)
├── intent/
│   ├── Intent.java              ✅ (intent vector model)
│   ├── NaturalLanguageIntentParser.java  ✅ (NL → intent)
│   └── IntentHistoryLearner.java         ✅ (pattern learning)
├── sla/
│   ├── SLAContract.java         ✅ (SLA model)
│   └── SLANegotiationAgent.java ✅ (auto-negotiation)
├── tradeoff/
│   └── CostPerformanceTradeoffEngine.java ✅ (multi-objective scoring)
├── placement/
│   └── IntentAwareVmPlacementPolicy.java  ✅ (intent-based VM placement)
└── util/
├── MetricsLogger.java       ✅ (CSV logging)
└── GraphGenerator.java      ✅ (PNG chart generation)

To build and run, open a terminal outside the IDE and run:
cd C:\Users\hpome\IdeaProject\CloudComputing\CloudProject
mvn clean compile -U
mvn exec:java

Or in IntelliJ: right-click pom.xml → Maven → Reload Project to download dependencies, then open MainSimulation.java and click the green ▶ play button next to main().