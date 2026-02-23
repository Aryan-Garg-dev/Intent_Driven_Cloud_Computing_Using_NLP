# Intent-Driven Autonomous Virtualization and SLA Negotiation Framework for Cloud Environments

## Field / Area of Invention

Cloud Computing · Autonomous Resource Management · Natural Language Processing for Infrastructure · Service Level Agreement (SLA) Automation · Multi-Objective Optimization

This project lies at the intersection of **cloud virtualization**, **intent-based networking**, and **autonomous systems**. It proposes a novel framework where cloud infrastructure decisions—VM placement, SLA negotiation, and cost-performance trade-offs—are driven directly by human-readable intent rather than manual, low-level configuration.

---

## Background / Problem Statement

Current cloud computing platforms (AWS, Azure, GCP) require users to manually select instance types, configure SLA parameters, and balance cost against performance through trial and error. This leads to several critical problems:

1. **Complexity Barrier** — Users must understand dozens of VM types, pricing models, and regional availability to make informed decisions. Non-expert users frequently over-provision (wasting money) or under-provision (degrading performance).

2. **Static SLA Contracts** — Traditional SLAs are negotiated once at onboarding and remain fixed. They do not adapt to changing workload patterns or evolving user priorities over time.

3. **Lack of Intent Awareness** — Existing VM placement algorithms (First-Fit, Best-Fit, Round-Robin) optimize only for resource utilization (CPU, RAM). They are completely unaware of *why* a user needs the resources—whether the priority is low cost, low latency, high security, or sustainability.

4. **No Learning from History** — Cloud platforms do not learn from a user's past behavior to proactively predict and pre-allocate resources for future requests.

5. **Manual Trade-Off Analysis** — Balancing cost vs. performance vs. security vs. carbon footprint requires manual effort and domain expertise that most users lack.

There is no existing system that takes a simple natural language statement like *"I need fast, secure servers for banking"* and autonomously translates it into concrete cloud infrastructure decisions.

---

## Objectives

1. **Natural Language Intent Parsing** — Design a parser that converts human-readable requests (e.g., *"I want cheap and budget-friendly servers"*) into a machine-readable intent vector with priorities for cost, latency, security, and carbon footprint.

2. **Autonomous SLA Negotiation** — Build a negotiation agent that automatically generates SLA contracts (max latency, max cost, min availability, security level) from the parsed intent, including conflict resolution when priorities are contradictory (e.g., cheap AND fast).

3. **Intent-Weighted Trade-Off Optimization** — Implement a multi-objective scoring engine that evaluates candidate resource configurations based on how well they match the user's intent, rather than simple resource fit.

4. **Intent-Aware VM Placement** — Develop a VM placement policy that selects hosts based on intent priorities (performance-oriented users get less-loaded hosts; cost-oriented users get consolidated hosts; security-oriented users get isolated hosts).

5. **User Intent Learning** — Create a history-based learning module that tracks user intent patterns over time and predicts future intents using weighted averaging, enabling proactive resource allocation.

6. **End-to-End Simulation** — Integrate all components into a CloudSim Plus simulation that runs 8 diverse scenarios, logs metrics to CSV, and generates publication-quality graphs.

---

## Summary

This framework introduces an **intent-driven paradigm** for cloud resource management. Instead of requiring users to configure infrastructure manually, the system accepts a natural language description of what the user wants and autonomously handles the entire pipeline:

```
User Intent (Natural Language)
        │
        ▼
┌─────────────────────────┐
│  NL Intent Parser       │  Keyword matching → Intent vector
│  (cost, latency,        │  [0.9, 0.5, 0.3, 0.2]
│   security, carbon)     │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Intent History Learner │  Tracks patterns per user
│                         │  Predicts future intents
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  SLA Negotiation Agent  │  Intent → SLA contract
│                         │  Conflict resolution
│                         │  Auto-accept/reject
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Trade-Off Engine       │  Multi-objective scoring
│                         │  Pareto efficiency
│                         │  SLA compliance check
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Intent-Aware VM        │  Host scoring by intent
│  Placement Policy       │  Performance / Cost /
│                         │  Security / Carbon weights
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  CloudSim Plus          │  Datacenter simulation
│  Simulation Engine      │  4 hosts, 4 VMs, 8 cloudlets
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│  Metrics Logger &       │  CSV export
│  Graph Generator        │  PNG chart generation
└─────────────────────────┘
```

The simulation runs **8 experiments** with diverse intents (cost-focused, speed-focused, security-focused, balanced, green, mixed) and demonstrates that different intents produce measurably different infrastructure decisions—validating the framework's intent-awareness.

---

## Code Structure

```
CloudProject/
├── pom.xml                                        Maven build config & dependencies
├── .gitignore                                     Git ignore rules
├── README.md                                      This file
├── results/                                       Generated at runtime
│   ├── simulation_results.csv                     Experiment metrics
│   ├── cost_vs_latency.png                        Scatter plot
│   ├── sla_satisfaction.png                       Bar chart
│   ├── tradeoff_scores.png                        Line chart
│   └── learning_accuracy.png                      Accuracy over time
│
└── src/main/java/org/intentcloudsim/
    │
    ├── MainSimulation.java                        Entry point; orchestrates the full pipeline
    │                                              across 8 test scenarios
    │
    ├── intent/
    │   ├── Intent.java                            Intent vector model with 4 priorities
    │   │                                          (cost, latency, security, carbon), each 0.0–1.0
    │   ├── NaturalLanguageIntentParser.java       Keyword-based NL parser; maps phrases like
    │   │                                          "cheap", "fast", "secure", "green" to scores
    │   └── IntentHistoryLearner.java              Per-user history tracking with weighted-average
    │                                              prediction and consistency scoring
    │
    ├── sla/
    │   ├── SLAContract.java                       SLA model (max latency, max cost, min availability,
    │   │                                          security level, max carbon); satisfaction checking
    │   └── SLANegotiationAgent.java               Translates intent → SLA parameters; handles
    │                                              cost-vs-latency conflicts; auto-accept/reject
    │
    ├── tradeoff/
    │   └── CostPerformanceTradeoffEngine.java     Intent-weighted scoring of resource configs;
    │                                              Pareto efficiency; SLA compliance validation
    │
    ├── placement/
    │   └── IntentAwareVmPlacementPolicy.java      Scores hosts on 4 dimensions weighted by intent:
    │                                              performance, cost (utilization), security
    │                                              (isolation), and carbon (consolidation)
    │
    └── util/
        ├── MetricsLogger.java                     Logs experiment data to CSV with 12 columns
        └── GraphGenerator.java                    Generates 4 PNG charts using XChart library
```

### Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| CloudSim Plus | 8.0.0 | Cloud infrastructure simulation engine |
| XChart | 3.8.6 | Publication-quality graph/chart generation |
| OpenCSV | 5.9 | CSV data export |
| JUnit | 4.13.2 | Unit testing |

---

## How to Run

### Prerequisites

- **Java 17** or later — [Download](https://adoptium.net/)
- **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **Git** — [Download](https://git-scm.com/)

Verify your installation:
```bash
java -version      # Should show 17+
mvn -version       # Should show 3.8+
```

### Build

```bash
cd C:\Users\hpome\IdeaProject\CloudComputing\CloudProject

# Download dependencies and compile (first run takes 1-2 minutes)
mvn clean compile -U
```

Expected output:
```
[INFO] BUILD SUCCESS
```

### Run the Simulation

**Option 1 — Maven:**
```bash
mvn exec:java
```

**Option 2 — Maven with explicit class:**
```bash
mvn exec:java -Dexec.mainClass="org.intentcloudsim.MainSimulation"
```

**Option 3 — IntelliJ IDEA:**
1. Right-click `pom.xml` → **Maven** → **Reload Project** (downloads dependencies)
2. Open `src/main/java/org/intentcloudsim/MainSimulation.java`
3. Click the green ▶ play button next to `public static void main`
4. Select **Run 'MainSimulation'**

### Expected Output

The simulation runs 8 experiments and prints:
```
╔══════════════════════════════════════════════════╗
║  Intent-Driven Autonomous Cloud Virtualization  ║
╚══════════════════════════════════════════════════╝

============================================================
EXPERIMENT 1 / 8
============================================================
--- STEP 1: Parse Intent ---
[IntentParser] Input: "I want cheap and budget-friendly servers for my startup"
[IntentParser] Parsed: Intent[cost=0.90, latency=0.50, security=0.30, carbon=0.20]

--- STEP 2: Learn Intent ---
--- STEP 3: Negotiate SLA ---
--- STEP 4: Evaluate Tradeoffs ---
--- STEP 5: CloudSim Plus Simulation ---

... (repeats for all 8 experiments) ...

===== SIMULATION SUMMARY =====
Total experiments: 8
SLA Satisfaction Rate: 87.5%
Average Cost: $6.25
Average Latency: 62.5 ms
==============================

╔══════════════════════════════════════════════════╗
║  SIMULATION COMPLETE                             ║
║  Check 'results/' folder for CSV and graphs      ║
╚══════════════════════════════════════════════════╝
```

### Output Files

After a successful run, check the `results/` folder:

| File | Description |
|------|-------------|
| `simulation_results.csv` | Raw data for all 8 experiments (12 columns) |
| `cost_vs_latency.png` | Cost vs. latency scatter plot |
| `sla_satisfaction.png` | SLA satisfaction rate by intent type |
| `tradeoff_scores.png` | Trade-off score vs. cost priority |
| `learning_accuracy.png` | Intent prediction accuracy over time |

### Troubleshooting

| Problem | Solution |
|---------|----------|
| `mvn clean compile` fails with Java version error | Ensure `java -version` shows 17+. In IntelliJ: **File → Project Structure → SDK → 17** |
| `Package does not exist: org.cloudsimplus` | Run `mvn clean install -U`. Check internet connection. In IntelliJ: right-click `pom.xml` → Maven → Reload Project |
| IntelliJ shows red underlines everywhere | **File → Invalidate Caches → Restart**. Wait for indexing to complete. |
| No graphs or CSV generated | The `results/` directory is created automatically at runtime. Ensure write permissions. |
| Cloudlets show `NOT FINISHED` | Increase `HOST_MIPS` or decrease `CLOUDLET_LENGTH` in `MainSimulation.java` |

