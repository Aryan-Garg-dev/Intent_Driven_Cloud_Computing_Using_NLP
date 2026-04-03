# Intent-Driven Cloud Computing Using NLP — Project Documentation

> **Comprehensive Technical and Patent Documentation**
>
> Covers: Field of Invention · Objectives · Working Principle · Detailed Description with Architecture
> Diagrams · Experimental Validation Results · Protectable Aspects

---

## Table of Contents

1. [Field / Area of Invention](#1-field--area-of-invention)
2. [Background and Motivation](#2-background-and-motivation)
3. [Objectives of Invention](#3-objectives-of-invention)
4. [Working Principle (Brief)](#4-working-principle-brief)
5. [System Architecture](#5-system-architecture)
6. [Detailed Description of the Invention](#6-detailed-description-of-the-invention)
   - 6.1 [Patent Idea 16 — Natural Language Intent Parser](#61-patent-idea-16--natural-language-intent-parser)
   - 6.2 [Patent Idea 17 — Intent-Aware SLA Negotiation Agent](#62-patent-idea-17--intent-aware-sla-negotiation-agent)
   - 6.3 [Patent Idea 18 — Cost-Performance Tradeoff Engine](#63-patent-idea-18--cost-performance-tradeoff-engine)
   - 6.4 [Patent Idea 19 — Intent History Learner](#64-patent-idea-19--intent-history-learner)
   - 6.5 [Patent Idea 20 — Intent-Aware VM Placement Policy](#65-patent-idea-20--intent-aware-vm-placement-policy)
   - 6.6 [Reinforcement Learning (RL) Intent Refiner](#66-reinforcement-learning-rl-intent-refiner)
   - 6.7 [Semantic Encoder (Sentence-BERT-Like)](#67-semantic-encoder-sentence-bert-like)
   - 6.8 [Intent Pipeline Service (API Layer)](#68-intent-pipeline-service-api-layer)
   - 6.9 [JavaFX Simulation UI](#69-javafx-simulation-ui)
   - 6.10 [Metrics Logger and Graph Generator](#610-metrics-logger-and-graph-generator)
7. [Experimental Validation Results](#7-experimental-validation-results)
8. [Aspects Requiring Protection](#8-aspects-requiring-protection)
9. [Technology Stack and Dependencies](#9-technology-stack-and-dependencies)
10. [How to Build and Run](#10-how-to-build-and-run)

---

## 1. Field / Area of Invention

**Domain:** Cloud Computing, Natural Language Processing (NLP), Autonomous Resource Management, Service Level Agreement (SLA) Automation, Reinforcement Learning.

This invention sits at the intersection of:

- **Intelligent Cloud Orchestration** — automating VM placement, SLA negotiation, and resource configuration without manual parameter specification.
- **Intent-Based Networking / Computing (IBN/IBC)** — translating high-level human goals (intents) directly into low-level infrastructure decisions, inspired by the IETF "intent-based networking" concept but applied to virtualized cloud resources.
- **Applied NLP for Infrastructure** — using keyword extraction, semantic embedding similarity, and negation/intensity detection to decode what a user wants from a cloud environment.
- **Adaptive Machine Learning** — using reinforcement learning (Q-learning) and intent-history learning to improve decisions over time, personalised to individual users.

---

## 2. Background and Motivation

### The Problem

Configuring cloud infrastructure today requires deep technical expertise:

- Users must select instance types (CPU, RAM, storage), latency SLAs, security groups, availability zones, and cost tiers manually.
- Service Level Agreements (SLAs) are negotiated in complex legal or API-based workflows that do not account for the user's actual intent.
- Resource placement algorithms (e.g., First Fit Decreasing, Round Robin) place VMs purely based on available capacity, ignoring user priorities like security, sustainability, or cost.
- There is no standard mechanism for a user to say "I need fast, affordable servers for my gaming application" and have the infrastructure automatically configure itself.

### The Gap This Invention Fills

| Existing Approach | Gap | This Invention |
|---|---|---|
| Manual instance selection (AWS, GCP) | Requires deep technical knowledge | NLP-based intent parser → automatic configuration |
| Static SLA templates | No adaptation to user language | Intent-driven SLA auto-negotiation |
| Capacity-based VM placement | Ignores user priorities | Intent-weighted multi-criteria placement |
| No per-user learning | System starts fresh every request | Intent history learner + RL refiner |
| Fixed cost/latency tradeoffs | Not intent-aware | Multi-objective tradeoff engine with intent weights |

---

## 3. Objectives of Invention

1. **Enable Natural Language Cloud Configuration** — Allow users to express infrastructure requirements in plain English without needing knowledge of cloud APIs or parameters.

2. **Automate SLA Negotiation** — Derive concrete, legally-enforceable SLA parameters (max latency, max cost, availability, security level, carbon limits) directly from parsed user intent.

3. **Intent-Weighted Multi-Objective Optimisation** — Evaluate and select resource configurations that best balance cost, latency, security, and carbon footprint according to the user's expressed priorities.

4. **Personalised Predictive Provisioning** — Learn individual user intent patterns over time to proactively predict and pre-provision resources before the user even requests them.

5. **Intent-Driven VM Placement** — Place virtual machines on physical hosts based on a multi-dimensional intent score rather than raw resource availability alone.

6. **Continuous Improvement via Reinforcement Learning** — Use a Q-learning feedback loop to refine intent interpretation over time, improving decisions as SLA outcomes are observed.

7. **Sustainability Awareness** — Incorporate a carbon/green priority dimension so that users can express environmental concerns alongside cost, latency, and security.

---

## 4. Working Principle (Brief)

The system converts a natural language sentence into a four-dimensional **Intent Vector**:

```
Intent = (costPriority, latencyPriority, securityPriority, carbonPriority)
         where each value ∈ [0.0, 1.0]
```

This vector is then used as the primary driver for all downstream cloud decisions:

```
NL Input → [NLP Parser + Semantic Encoder]
         → Intent Vector
         → [RL Refiner + History Learner] → Refined Intent
         → [SLA Negotiation Agent]        → SLA Contract
         → [Tradeoff Engine]              → Optimal Resource Option
         → [VM Placement Policy]          → Physical Host Selection
         → [CloudSim Plus]                → Simulation Execution
         → [Metrics Logger + Graph Gen]   → Results & Feedback
         → [RL Q-Update]                  → Better Future Decisions
```

---

## 5. System Architecture

### 5.1 High-Level System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                    INTENT-DRIVEN CLOUD SYSTEM                       │
│                                                                     │
│  ┌──────────────┐    ┌──────────────────────────────────────────┐  │
│  │   User /     │    │            INTENT LAYER                  │  │
│  │   JavaFX UI  │───▶│  NL Parser  ──▶  Semantic Encoder        │  │
│  │   (Tab 1)    │    │     │                   │                 │  │
│  └──────────────┘    │     ▼                   ▼                 │  │
│                      │  Keyword           SBERT-Like             │  │
│                      │  Matching          Embedding              │  │
│                      │     │                   │                 │  │
│                      │     └──────┬────────────┘                 │  │
│                      │            │ Hybrid Intent Vector         │  │
│                      └────────────┼─────────────────────────────┘  │
│                                   │                                  │
│                      ┌────────────▼─────────────────────────────┐  │
│                      │         REFINEMENT LAYER                  │  │
│                      │  History Learner  ──▶  RL Refiner         │  │
│                      │  (User Patterns)        (Q-Learning)      │  │
│                      └────────────┬─────────────────────────────┘  │
│                                   │ Refined Intent Vector           │
│                      ┌────────────▼─────────────────────────────┐  │
│                      │         DECISION LAYER                    │  │
│                      │  SLA Negotiation ──▶  Tradeoff Engine     │  │
│                      │  Agent                 (Multi-Objective)  │  │
│                      └────────────┬─────────────────────────────┘  │
│                                   │ SLA Contract + Best Option      │
│                      ┌────────────▼─────────────────────────────┐  │
│                      │         EXECUTION LAYER                   │  │
│                      │  VM Placement Policy                      │  │
│                      │  (Intent-Aware Host Scoring)              │  │
│                      │            │                              │  │
│                      │            ▼                              │  │
│                      │  CloudSim Plus Simulation                 │  │
│                      │  (Datacenter + Hosts + VMs + Cloudlets)   │  │
│                      └────────────┬─────────────────────────────┘  │
│                                   │                                  │
│                      ┌────────────▼─────────────────────────────┐  │
│                      │         OUTPUT LAYER                      │  │
│                      │  Metrics Logger → CSV                     │  │
│                      │  Graph Generator → PNG Charts             │  │
│                      │  RL Feedback Loop (Q-Table Update)        │  │
│                      └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 Module Dependency Diagram

```
MainSimulation / IntentPipelineService / SimulationUI
        │
        ├─── intent/
        │       ├── NaturalLanguageIntentParser  ◀─── SemanticIntentMapper
        │       │                                         │
        │       │                                    SentenceBertLikeEncoder
        │       ├── IntentHistoryLearner
        │       └── Intent  (data class)
        │
        ├─── rl/
        │       └── ReinforcementIntentRefiner
        │
        ├─── sla/
        │       ├── SLANegotiationAgent
        │       └── SLAContract  (data class)
        │
        ├─── tradeoff/
        │       └── CostPerformanceTradeoffEngine
        │
        ├─── placement/
        │       └── IntentAwareVmPlacementPolicy
        │
        ├─── util/
        │       ├── MetricsLogger
        │       └── GraphGenerator
        │
        └─── ui/
                ├── SimulationUI  (JavaFX Application)
                ├── tabs/
                │       ├── IntentInputTab
                │       ├── ConfigurationTab
                │       └── SimulationTab
                └── models/CloudConfig
```

### 5.3 Intent Vector Flow

```
User Input String
       │
       ▼
┌──────────────────────────────────────────────────────┐
│               NaturalLanguageIntentParser            │
│                                                      │
│  1. Keyword Matching (weighted, with phrases)        │
│     COST_KEYWORDS: {cheap→0.9, budget→0.85, ...}    │
│     LATENCY_KEYWORDS: {fast→0.9, real-time→0.95, …} │
│     SECURITY_KEYWORDS: {secure→0.9, hipaa→0.95, …}  │
│     CARBON_KEYWORDS: {green→0.9, sustainable→0.85,…}│
│                                                      │
│  2. Intensity Modifier Applied                       │
│     {very→1.1, extremely→1.2, ultra→1.2, …}         │
│                                                      │
│  3. Negation Detection                               │
│     "don't care about…" → score reduced to 0        │
│                                                      │
│  4. Semantic Score via SemanticIntentMapper          │
│     (SBERT-like cosine similarity to anchor phrases) │
│                                                      │
│  5. Hybrid Combination:                              │
│     score = 0.45 * keywordScore + 0.55 * semanticScore│
│                                                      │
│  OUTPUT: Intent(cost, latency, security, carbon)     │
└──────────────────────────────────────────────────────┘
       │
       ▼
┌────────────────────────────────┐
│  RL Refiner blends with         │
│  History Prediction (70/30)    │
│  Then applies Q-learning action │
│  (BOOST_COST / BOOST_LATENCY / │
│   BOOST_SECURITY / BOOST_CARBON│
│   / BALANCE)                   │
└────────────────────────────────┘
       │ Refined Intent Vector
       ▼
    Downstream decisions
```

### 5.4 SLA Negotiation Logic

```
Refined Intent
       │
       ▼
┌──────────────────────────────────────────────────────┐
│               SLANegotiationAgent                    │
│                                                      │
│  maxLatency  = 200 - (latencyPriority * 190)  [ms]  │
│  maxCost     = 20  - (costPriority    * 19.5) [$/h] │
│                                                      │
│  CONFLICT RESOLUTION:                                │
│  if cost > 0.7 AND latency > 0.7:                   │
│      maxLatency *= 1.2   (relax 20%)                │
│      maxCost    *= 1.15  (relax 15%)                │
│                                                      │
│  minAvailability = 95 + (securityPriority * 4.9) %  │
│  securityLevel   = securityPriority * 10            │
│  maxCarbon       = 100 - (carbonPriority * 80) g/h  │
│                                                      │
│  Contract ACCEPTED if maxLatency ≥ 10ms AND         │
│                        maxCost   ≥ $0.50/h          │
└──────────────────────────────────────────────────────┘
```

### 5.5 VM Placement Scoring

```
For each candidate Host:

  performanceScore = availableMIPS / totalMIPS
  costScore        = allocatedMIPS / totalMIPS  (utilisation = cheaper)
  securityScore    = 1 / (1 + existingVMs)
  carbonScore      = allocatedMIPS / totalMIPS  (consolidation = greener)

  hostScore = intent.latencyPriority  * performanceScore * 40
            + intent.costPriority     * costScore        * 30
            + intent.securityPriority * securityScore    * 20
            + intent.carbonPriority   * carbonScore      * 10

  Best host = argmax(hostScore)
```

### 5.6 Reinforcement Learning Q-Table Structure

```
State:  (costBucket, latencyBucket, securityBucket, carbonBucket, loadBucket)
        where each bucket ∈ {0,1,2,3}  (quartiles of [0,1])
        → 4^5 = 1024 possible states

Actions: [BOOST_COST, BOOST_LATENCY, BOOST_SECURITY, BOOST_CARBON, BALANCE]

Reward: R = slaSuccess(+1/-1) - 0.08*cost - 0.25*(latency/100)

Q-Update (standard Q-learning):
  Q(s,a) ← Q(s,a) + α · (R + γ · max_a'Q(s',a') - Q(s,a))
  α = 0.20  (learning rate)
  γ = 0.85  (discount factor)
  ε = 0.15  (exploration rate)
```

### 5.7 JavaFX UI Tab Flow

```
┌─────────────────────────────────────────────────────────┐
│  Tab 1: Intent Parser                                   │
│  ┌──────────────────────┬──────────────────────────┐   │
│  │  Text Input          │  Parsed Results           │   │
│  │  (NL sentence)       │  Priority Scores          │   │
│  │                      │  Confidence %             │   │
│  │  [Parse Intent]      │  Dominant Priority        │   │
│  │  [Example]           │  SLA Recommendations      │   │
│  │  [Clear]             │  Tradeoff Options         │   │
│  └──────────────────────┴──────────────────────────┘   │
│         │  (passes CloudConfig to Tab 2)                │
└─────────┼───────────────────────────────────────────────┘
          ▼
┌─────────────────────────────────────────────────────────┐
│  Tab 2: Configuration                                   │
│  Edit JSON / form fields for cloud config               │
│  Validate and save configuration                        │
│         │  (passes CloudConfig to Tab 3)                │
└─────────┼───────────────────────────────────────────────┘
          ▼
┌─────────────────────────────────────────────────────────┐
│  Tab 3: Simulation & Results                            │
│  Run CloudSim Plus simulation                           │
│  View logs, cost metrics, performance charts            │
│  Tradeoff analysis, infrastructure visualisation        │
└─────────────────────────────────────────────────────────┘
```

---

## 6. Detailed Description of the Invention

### 6.1 Patent Idea 16 — Natural Language Intent Parser

**Class:** `org.intentcloudsim.intent.NaturalLanguageIntentParser`

**Purpose:** Converts any free-form natural language sentence into a four-dimensional Intent Vector `(cost, latency, security, carbon)` where each value is in `[0.0, 1.0]`.

**Algorithm Detail:**

The parser employs a **hybrid dual-pathway architecture**:

**Pathway 1 — Keyword Matching (weight 0.45):**
- Maintains four curated lexicons: `COST_KEYWORDS`, `LATENCY_KEYWORDS`, `SECURITY_KEYWORDS`, `CARBON_KEYWORDS`, each mapping terms to relevance weights (e.g., `"cheap" → 0.9`, `"real-time" → 0.95`, `"hipaa" → 0.95`).
- Supports **multi-word phrases** (e.g., `"low latency"`, `"carbon neutral"`, `"budget friendly"`).
- Applies **intensity modifiers**: if a modifier word (`"very"`, `"extremely"`, `"ultra"`, etc.) precedes a keyword, its weight is multiplied by the modifier coefficient (up to ×1.2).
- Applies **negation detection**: patterns like `"don't care about"`, `"not important"`, `"low priority"` reduce the corresponding dimension's score to zero.
- When multiple keywords match in the same category, applies a bonus for keyword count: `score += 0.05 * (matchCount - 1)`.

**Pathway 2 — Semantic Matching (weight 0.55):**
- Uses `SemanticIntentMapper` which pre-encodes anchor phrases for each dimension using `SentenceBertLikeEncoder`.
- Computes cosine similarity between the input sentence embedding and each anchor embedding.
- Falls back to soft lexical matching if cosine similarity is insufficient.
- Applies negation heuristics (e.g., `"not cheap"` → reduces cost score).

**Combination Formula:**
```
hybridScore = 0.45 * keywordScore + 0.55 * semanticScore
if max(keywordScore, semanticScore) ≥ 0.75:
    hybridScore = max(hybridScore, strongSignal * 0.90)
```

**Example Parsing:**
```
Input:  "I need fast but cheap servers for gaming"
Output: Intent[cost=0.86, latency=0.87, security=0.00, carbon=0.07]
  - Semantic: cost=0.82, latency=0.82 (gaming = fast, cheap recognized)
  - Keyword: "fast"→0.9, "cheap"→0.9, "gaming"→0.85

Input:  "Secure encrypted infrastructure for banking and healthcare"
Output: Intent[cost=0.07, latency=0.00, security=0.90, carbon=0.09]
  - Keywords: "secure"→0.9, "encrypted"→0.85, "banking"→0.9, "healthcare"→0.9
```

---

### 6.2 Patent Idea 17 — Intent-Aware SLA Negotiation Agent

**Class:** `org.intentcloudsim.sla.SLANegotiationAgent`

**Purpose:** Automatically derives concrete Service Level Agreement parameters from the Intent Vector, replacing manual SLA specification.

**SLA Parameters Generated:**
- `maxLatencyMs` — maximum allowed response latency in milliseconds
- `maxCostPerHour` — maximum cost in USD per hour
- `minAvailability` — minimum uptime percentage
- `minSecurityLevel` — security level on a 0–10 scale
- `maxCarbonGrams` — maximum carbon emission in grams per hour

**Negotiation Logic:**
```
latencyPriority ∈ [0,1]  →  maxLatency = 200 - priority × 190   (ms)
costPriority    ∈ [0,1]  →  maxCost    = 20  - priority × 19.5  ($/hr)
securityPriority→         minAvailability = 95 + priority × 4.9  (%)
                           securityLevel   = priority × 10
carbonPriority  →          maxCarbon = 100 - priority × 80        (g/hr)
```

**Conflict Resolution:** When a user simultaneously requests high cost-saving (priority > 0.7) AND high performance (latency priority > 0.7) — a fundamentally conflicting requirement — the agent automatically relaxes both constraints by a small percentage rather than rejecting either demand.

**Auto-Acceptance:** The provider-side agent accepts the contract if the negotiated terms remain within achievable bounds (latency ≥ 10ms, cost ≥ $0.50/hr).

**Re-negotiation:** A dedicated `renegotiate()` method handles SLA violations at runtime by adding a 10% buffer to violated parameters.

---

### 6.3 Patent Idea 18 — Cost-Performance Tradeoff Engine

**Class:** `org.intentcloudsim.tradeoff.CostPerformanceTradeoffEngine`

**Purpose:** Evaluates multiple candidate resource configurations and selects the one that best matches the user's intent by computing a weighted multi-objective score.

**Scoring Formula:**
```
score(config, intent) =
    intent.costPriority     × (1/cost)    × 10.0
  + intent.latencyPriority  × (1/latency) × 1000.0
  + intent.securityPriority × securityLevel
  + intent.carbonPriority   × (1/carbonEmission) × 100.0
```

Higher score = better match with user's expressed priorities.

**Pareto Efficiency Metric:**
```
paretoScore = sqrt(normalizedCost × normalizedLatency)
```
This geometric mean provides a Pareto-optimal balance point for configurations where no single metric dominates.

**Example Run (Experiment 1 — Gaming/Fast/Cheap):**
```
Intent: cost=0.87, latency=0.71
Option 0: $2.00/hr, 150ms  → score = 10.08
Option 1: $5.00/hr,  80ms  → score = 11.60
Option 2: $10.0/hr,  40ms  → score = 19.57
Option 3: $15.0/hr,  20ms  → score = 36.95  ← BEST
```
Note: Despite the user wanting low cost, the high latency priority of the refined intent (gaming) drove selection toward low-latency option 3.

---

### 6.4 Patent Idea 19 — Intent History Learner

**Class:** `org.intentcloudsim.intent.IntentHistoryLearner`

**Purpose:** Tracks per-user intent history to predict future intents using a recency-weighted moving average.

**Algorithm:**
- Maintains a map of `userId → List<Intent>` (unbounded, capped by `windowSize = 10` for prediction).
- For prediction, applies linearly increasing weights to recent intents: the most recent intent has weight `n`, the next has `n-1`, etc.
- Weighted average across all four dimensions produces the predicted intent vector.

**Consistency Scoring:**
```
consistencyScore = max(0, 1.0 - variance)
```
Low variance across a user's history → high consistency → more reliable predictions.

**Usage in the Pipeline:**
The predicted intent from history is blended with the fresh NLP-parsed intent (70% parser, 30% history) before RL refinement, preventing stale predictions from overriding clear new signals while still benefiting from learned patterns.

---

### 6.5 Patent Idea 20 — Intent-Aware VM Placement Policy

**Class:** `org.intentcloudsim.placement.IntentAwareVmPlacementPolicy`

**Purpose:** Selects the optimal physical host for each virtual machine based on a multi-dimensional intent score, rather than placing VMs solely based on first-fit resource availability.

**Host Scoring (Reproduced for emphasis):**
```
performanceScore = availableMIPS / totalMIPS     (latency-aware)
costScore        = utilisation                   (packing = cheaper)
securityScore    = 1 / (1 + numExistingVMs)      (isolation = secure)
carbonScore      = utilisation                   (packing = greener)

hostScore = latencyPriority  × performanceScore × 40
          + costPriority     × costScore        × 30
          + securityPriority × securityScore    × 20
          + carbonPriority   × carbonScore      × 10
```

**Key Insight:** The same utilisation metric contributes positively to both cost and carbon scores (VM packing is cheaper and greener), while it competes with performance (a heavily loaded host is slower). The latency dimension naturally creates pressure to use less-loaded, higher-capacity hosts, while cost/carbon create pressure towards consolidation — mirroring real-world cloud trade-offs.

---

### 6.6 Reinforcement Learning (RL) Intent Refiner

**Class:** `org.intentcloudsim.rl.ReinforcementIntentRefiner`

**Purpose:** Applies Q-learning to continuously improve intent refinement decisions based on observed SLA outcomes.

**State Representation:**
Each of the four intent dimensions and the current system load is discretized into four buckets (0–3 representing 0–25%, 25–50%, 50–75%, 75–100% of range), yielding up to 4^5 = 1,024 unique states.

**Action Space (5 actions):**
| Action | Effect |
|---|---|
| `BOOST_COST` | +0.12 to cost, −0.05 to latency |
| `BOOST_LATENCY` | +0.12 to latency, −0.05 to cost |
| `BOOST_SECURITY` | +0.12 to security, −0.04 to carbon |
| `BOOST_CARBON` | +0.12 to carbon, −0.03 to latency |
| `BALANCE` | Move all dimensions toward their mean |

**Reward Function:**
```
R = SLASuccess(+1 or −1) − 0.08 × observedCost − 0.25 × (observedLatency / 100)
```
This penalises both over-spending and high latency even when the SLA is technically satisfied.

**Exploration:** Uses ε-greedy strategy (ε=0.15), meaning 15% of actions are taken randomly to explore new strategies.

---

### 6.7 Semantic Encoder (Sentence-BERT-Like)

**Class:** `org.intentcloudsim.intent.SentenceBertLikeEncoder`

**Purpose:** Provides a lightweight, dependency-free approximation of sentence embeddings using hash-based feature vectors, designed to be replaceable with a full SBERT model.

**Implementation:**
- Tokenizes input text into unigrams and bigrams.
- Maps tokens to canonical synonyms (`cheap → cost`, `fast → latency`, `secure → security`, etc.).
- Adds weighted contributions to a 256-dimensional vector using hash-based indexing (`feature.hashCode() % 256`).
- L2-normalises the resulting vector.
- Computes cosine similarity between two embeddings.

This approach captures domain-specific semantic similarity without requiring external ML models or network calls, making the system fully self-contained and deterministic.

---

### 6.8 Intent Pipeline Service (API Layer)

**Class:** `org.intentcloudsim.api.IntentPipelineService`

**Purpose:** Provides a clean service facade exposing the full intent processing pipeline for use by REST controllers, desktop UIs, or testing harnesses.

**API Surface:**
```java
AnalysisResult analyzeIntent(String userId, String userInput, double systemLoad)
FeedbackResult applyFeedback(String userId, boolean slaMet,
                              double observedCost, double observedLatency)
```

**Returns:**
- `AnalysisResult`: parsed, predicted, and refined intents; SLA contract; best resource option; cost/latency; tradeoff score.
- `FeedbackResult`: RL reward received and Q-table update details.

---

### 6.9 JavaFX Simulation UI

**Classes:** `org.intentcloudsim.ui.*`

A three-tab JavaFX desktop application providing a no-code interactive simulation interface:

**Tab 1 — Intent Parser:**
- Text input area with pre-filled example.
- Runs `NaturalLanguageIntentParser.parseWithConfidence()`, `SLANegotiationAgent.negotiate()`, and `CostPerformanceTradeoffEngine.findBestOption()` in a background thread.
- Displays priority scores (percentage bars), confidence level, dominant priority, and recommended SLA + resource configuration.

**Tab 2 — Configuration:**
- Shows and allows editing the `CloudConfig` object (JSON or form fields).
- Allows manual override of intent-derived parameters.

**Tab 3 — Simulation & Results:**
- Runs the full CloudSim Plus simulation using the configured intent.
- Shows live logs, execution timelines, cost breakdown, and performance metrics.

---

### 6.10 Metrics Logger and Graph Generator

**Classes:** `org.intentcloudsim.util.MetricsLogger`, `org.intentcloudsim.util.GraphGenerator`

**MetricsLogger:** Writes structured experiment records to CSV with 15 columns:
`Timestamp, UserIntent, CostPriority, LatencyPriority, SecurityPriority, SLAMaxLatency, SLAMaxCost, ActualLatency, ActualCost, HostSelected, TradeoffScore, SLASatisfied, SystemLoad, RLReward, PipelineMode`

**GraphGenerator:** Reads the CSV and produces five publication-quality PNG charts using XChart:
1. **Cost vs Latency Scatter** — shows the position of selected configurations in cost-latency space.
2. **Tradeoff Score Bar Chart** — compares tradeoff scores across experiments, sorted by cost priority.
3. **SLA Satisfaction Bar Chart** — success vs violation rates grouped by dominant intent type (Cost-focused, Latency-focused, Security-focused, Balanced).
4. **Learning Accuracy Line Graph** — cumulative SLA satisfaction rate over interactions (proxy for learning improvement).
5. **RL Reward Trend Line Graph** — shows Q-learning reward evolution over interactions.

---

## 7. Experimental Validation Results

The following results were obtained by running `MainSimulation` with three representative intent statements covering three distinct use-case profiles.

### 7.1 Test Configuration

| Parameter | Value |
|---|---|
| Hosts | 4 (varying MIPS: 80k–128k, RAM: 16–28 GB) |
| VMs per experiment | 4 (2500 MIPS, 2 cores, 4 GB RAM) |
| Cloudlets per experiment | 8 (50k–120k MI workloads) |
| Candidate resource options | 4: `($2, 150ms), ($5, 80ms), ($10, 40ms), ($15, 20ms)` |
| CloudSim Plus version | 8.0.0 |

### 7.2 Experiment 1 — Gaming / Fast but Cheap

**Intent:** `"I need fast but cheap servers for gaming"`

**Parsed Intent Vector:**
```
Semantic scores: cost=0.82, latency=0.82, security=0.00, carbon=0.12
Final parsed:    cost=0.86, latency=0.87, security=0.00, carbon=0.07
```

**RL Refinement:**
- State: `2-3-0-0-3` (high cost/latency priority, high system load)
- Action: `BOOST_COST`
- Refined intent: `cost=0.87, latency=0.71, security=0.15, carbon=0.14`

**SLA Negotiation (Conflict: both cost and latency > 0.7):**
```
maxLatency  = 78.79 ms    (strict, then relaxed 20% due to conflict)
maxCost     = $3.51/hr    (strict, then relaxed 15% due to conflict)
minAvail    = 95.7%
secLevel    = 1.5/10
maxCarbon   = 88.8 g/hr
Contract:   ACCEPTED
```

**Tradeoff Analysis:**
```
Option 0: $2.00, 150ms  → score = 10.08
Option 1: $5.00,  80ms  → score = 11.60
Option 2: $10.0,  40ms  → score = 19.57
Option 3: $15.0,  20ms  → score = 36.95  ← SELECTED
```

**VM Placement:**
```
All 4 VMs placed on Host 0 (score=31.28)
(All hosts initially equal in utilisation; first-fit tie-breaking)
```

**CloudSim Execution:**
```
Simulation duration: 96.32 seconds (simulated time)
All 8 cloudlets: SUCCESS
Cloudlet completion times: 40.1s → 96.1s (proportional to workload)
```

**SLA Check:**
```
Latency:      20ms ≤ 78.79ms   ✓
Cost:         $15   > $3.51/hr ✗  (tradeoff engine selected premium option)
Availability: 99.5% ≥ 95.7%   ✓
SLA Satisfied: FALSE
RL Reward: -2.250
```

---

### 7.3 Experiment 2 — Security-Critical (Banking/Healthcare)

**Intent:** `"Secure encrypted infrastructure for banking and healthcare"`

**Parsed Intent Vector:**
```
Semantic scores: cost=0.13, latency=0.00, security=0.82, carbon=0.16
Final parsed:    cost=0.07, latency=0.00, security=0.90, carbon=0.09
```

**RL Refinement:**
- State: `0-0-3-0-3` (high security, high load)
- Refined intent: `cost=0.32, latency=0.10, security=0.78, carbon=0.15`

**SLA Negotiation:**
```
maxLatency  = 181.0 ms    (lenient — low latency priority)
maxCost     = $13.78/hr   (lenient — low cost priority)
minAvail    = 98.8%       (high — high security priority)
secLevel    = 7.8/10      (high)
maxCarbon   = 21.6 g/hr
Contract:   ACCEPTED
```

**Tradeoff Analysis:**
```
Option 0: $2.00, 150ms  → score = 6.47
Option 1: $5.00,  80ms  → score = 6.10
Option 2: $10.0,  40ms  → score = 7.03
Option 3: $15.0,  20ms  → score = 9.42  ← SELECTED
```

**Host Placement Score:** `19.61` (lower than gaming experiment due to lower latency/cost weights)

**SLA Check:**
```
Latency:      20ms ≤ 181ms    ✓
Cost:         $15   > $13.78  ✗
Availability: 99.5% ≥ 98.8%  ✓
SLA Satisfied: FALSE
RL Reward: -2.250
```

---

### 7.4 Experiment 3 — Green / Sustainable

**Intent:** `"Green sustainable carbon-neutral cloud computing"`

**Parsed Intent Vector:**
```
Final parsed: cost=0.27, latency=0.10, security=0.15, carbon=0.70
```

**Tradeoff Analysis:**
```
Option 0: $2.00, 150ms  → score = 3.99
Option 1: $5.00,  80ms  → score = 3.68
Option 2: $10.0,  40ms  → score = 4.91
Option 3: $15.0,  20ms  → score = 7.32  ← SELECTED
```

**Host Placement Score:** `7.00` (primarily carbon-driven)

**SLA Check:**
```
Latency:      20ms ≤ 181ms    ✓
Cost:         $15   > $14.74  ✗
Availability: 99.5% ≥ 95.7%  ✓
SLA Satisfied: FALSE
RL Reward: -2.250
```

---

### 7.5 Summary Table

| Experiment | Intent | Cost Priority | Latency Priority | Security Priority | Carbon Priority | SLA Latency Limit | SLA Cost Limit | Actual Latency | Actual Cost | SLA Met | RL Reward |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | Gaming / Fast+Cheap | 0.87 | 0.71 | 0.15 | 0.14 | 78.79 ms | $3.51 | 20 ms | $15.00 | ✗ | −2.250 |
| 2 | Banking / Security | 0.32 | 0.10 | 0.78 | 0.15 | 181 ms | $13.78 | 20 ms | $15.00 | ✗ | −2.250 |
| 3 | Green / Sustainable | 0.27 | 0.10 | 0.15 | 0.70 | 181 ms | $14.74 | 20 ms | $15.00 | ✗ | −2.250 |

**Overall SLA Satisfaction Rate: 0%** across this 3-experiment batch (cost limit was exceeded in all cases — the tradeoff engine favoured the lowest-latency option regardless, suggesting the cost penalty term needs tuning for cost-sensitive scenarios).

### 7.6 Unit Test Results

All 4 unit tests pass:

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0

NaturalLanguageIntentParserTest:
  ✓ parse_shouldPrioritizeLatencyAndCost_forFastButNotExpensivePhrase
      Input: "I need fast but not expensive servers"
      latencyPriority=0.86 ≥ 0.60 ✓,  costPriority=0.70 ≥ 0.55 ✓

  ✓ parse_shouldReduceLatencyPriority_whenSpeedDoesNotMatter
      Input: "minimize cost and latency doesn't matter"
      costPriority=0.81 ≥ 0.55 ✓,  latencyPriority=0.11 ≤ 0.35 ✓

ReinforcementIntentRefinerTest:
  ✓ Refines intent correctly under known state+load
  ✓ Updates Q-table correctly from positive feedback
      reward=0.450, updatedQ=0.090 ✓
```

### 7.7 Generated Output Files

After running the simulation, the following artefacts are produced in the `results/` directory:

| File | Description |
|---|---|
| `simulation_results.csv` | Full experiment log with 15 metrics per run |
| `cost_vs_latency.png` | Scatter plot: Actual Cost vs Actual Latency per experiment |
| `tradeoff_scores.png` | Bar chart: Tradeoff score per experiment (sorted by cost priority) |
| `sla_satisfaction.png` | Bar chart: SLA satisfied vs violated by intent type |
| `learning_accuracy.png` | Line chart: Cumulative SLA satisfaction rate over interactions |
| `rl_reward_trend.png` | Line chart: RL reward per interaction |

---

## 8. Aspects Requiring Protection

The following aspects of this invention are novel and require intellectual property protection:

### 8.1 Core Novelties Warranting Patent Protection

**Claim 1 — Hybrid NLP Intent Parsing for Cloud Configuration**
> A method of converting a natural language string into a multi-dimensional cloud infrastructure intent vector using a hybrid approach combining: (a) weighted keyword lexicons with intensity modifiers and negation detection; (b) hash-based sentence embeddings with cosine similarity scoring against anchor phrases; (c) a configurable weighting formula (default 45/55) blending the two scores.

**Claim 2 — Intent-Derived Automatic SLA Generation**
> A method of automatically generating Service Level Agreement (SLA) parameters — including latency bounds, cost limits, availability percentages, security levels, and carbon emission limits — from a parsed intent vector, without requiring manual user specification of these technical parameters, with built-in conflict resolution for contradictory priorities.

**Claim 3 — Intent-Weighted Multi-Criteria Resource Tradeoff Engine**
> A method of scoring and selecting cloud resource configurations using a utility function that weights cost, latency, security, and carbon metrics by the user's respective intent priorities, enabling personalised Pareto-optimal resource selection.

**Claim 4 — Intent-History Learning for Predictive Cloud Provisioning**
> A method of recording per-user intent vectors over time and generating weighted-average predictions of future intent, for use in proactive resource pre-provisioning, using recency-weighted window averaging.

**Claim 5 — Intent-Aware Virtual Machine Placement Policy**
> A VM placement algorithm that selects physical host machines by computing a multi-dimensional score incorporating CPU availability (for latency), resource utilisation (for cost and carbon), and VM isolation (for security), weighted by the user's intent vector.

**Claim 6 — Reinforcement Learning Intent Refinement Loop**
> A Q-learning system that refines intent vectors at inference time by choosing from a fixed action set (boost individual priorities or balance) based on quantised intent+load state, and updates its Q-table using a reward function incorporating SLA success/failure, cost, and latency outcomes.

**Claim 7 — End-to-End Intent-Driven Cloud Simulation Framework**
> The complete integrated system combining all above claims (1–6) into a unified pipeline from natural language input to cloud simulation execution, including metrics logging and feedback loop, implemented on top of a standard cloud simulation platform (CloudSim Plus).

### 8.2 Secondary Protectable Aspects (Trade Secrets / Copyright)

- The specific synonym-canonical mapping and anchor phrase sets used in the semantic encoder (domain knowledge encoded as lookup tables).
- The specific reward function coefficients (0.08 cost penalty, 0.25 latency penalty) derived from empirical tuning.
- The host scoring weight constants (40/30/20/10 for latency/cost/security/carbon).
- The 70/30 blending ratio between fresh NLP intent and historical prediction.
- The UI/UX design of the three-tab simulation workflow.

---

## 9. Technology Stack and Dependencies

| Component | Technology | Version |
|---|---|---|
| Language | Java | 17 |
| Build Tool | Apache Maven | 3.x |
| Cloud Simulation | CloudSim Plus | 8.0.0 |
| Desktop UI | JavaFX | 21.0.1 |
| Chart Library | XChart | 3.8.6 |
| CSV I/O | OpenCSV | 5.9 |
| JSON Handling | Gson | 2.10.1 |
| Logging | SLF4J Simple | 2.0.5 |
| Testing | JUnit | 4.13.2 |

**Architecture Pattern:** Layered (Intent → Refinement → Decision → Execution → Output), with a service facade (`IntentPipelineService`) decoupling the UI from business logic.

---

## 10. How to Build and Run

### Prerequisites

- Java 17+
- Apache Maven 3.x
- (For UI) A graphical display environment (X11/Wayland/macOS/Windows)

### Build

```bash
mvn compile
```

### Run Unit Tests

```bash
mvn test
```

Expected: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`

### Run CLI Simulation (Headless)

Provide intent statements as command-line arguments:

```bash
mvn -q exec:java \
  -Dexec.mainClass=org.intentcloudsim.MainSimulation \
  -Dexec.args="I need fast but cheap servers for gaming"
```

Or interactively via stdin (enter blank line to finish):

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" \
  org.intentcloudsim.MainSimulation
```

### Run the JavaFX Desktop UI

```bash
mvn javafx:run
```

Or:

```bash
mvn exec:java -Dexec.mainClass=org.intentcloudsim.ui.SimulationUI
```

### Output Artefacts

All results are saved to the `results/` directory:
- `results/simulation_results.csv`
- `results/cost_vs_latency.png`
- `results/tradeoff_scores.png`
- `results/sla_satisfaction.png`
- `results/learning_accuracy.png`
- `results/rl_reward_trend.png`

---

*Documentation generated from source code analysis and live simulation run on 2026-04-03.*
