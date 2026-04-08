# NLP + RL Pipeline Deep Dive

This document explains how natural language input is converted into cloud intent vectors, how reinforcement learning refines that intent over time, and how results are surfaced in the UI.

---

## 1) End-to-end flow at a glance

Primary runtime path:

1. User types an intent in `IntentInputTab` (`parseAndAnalyze()`)
2. Input quality gate runs (`isIntentDetailedEnough(...)`)
3. `IntentPipelineService.analyzeIntent(...)` executes:
   - parser intent (`NaturalLanguageIntentParser.parse`)
   - historical prediction (`IntentHistoryLearner.predict`)
   - RL refinement (`ReinforcementIntentRefiner.refineIntent`)
   - baseline vs refined evaluation via SLA + tradeoff engine
4. UI simulates observed runtime metrics and calls `IntentPipelineService.applyFeedback(...)`
5. RL updates Q-values (`updateFromFeedback`), history is summarized and persisted
6. `CloudConfig` carries parser/RL/SLA/tradeoff stats into Tab 3 simulation and RL impact panel

Key classes:

- NLP parsing: `org.intentcloudsim.intent.NaturalLanguageIntentParser`
- Semantic mapping: `org.intentcloudsim.intent.SemanticIntentMapper`
- Embedding-like encoder: `org.intentcloudsim.intent.SentenceBertLikeEncoder`
- User history: `org.intentcloudsim.intent.IntentHistoryLearner`
- RL: `org.intentcloudsim.rl.ReinforcementIntentRefiner`
- Orchestration: `org.intentcloudsim.api.IntentPipelineService`
- UI entry points: `org.intentcloudsim.ui.tabs.IntentInputTab`, `SimulationTab`

---

## 2) NLP parsing internals

## 2.1 Input → multi-signal scoring

`NaturalLanguageIntentParser.parse(String input)` computes four priority scores:

- cost
n- latency
- security
- carbon

Each dimension is built from two signals:

- keyword signal (weighted phrases + intensity modifiers)
- semantic signal (anchor similarity from `SemanticIntentMapper`)

Combination formula in code:

- `combined = 0.45 * keyword + 0.55 * semantic`
- if strong signal exists (`>= 0.75`), the parser keeps it from being diluted:
  - `combined = max(combined, strongSignal * 0.90)`

### 2.2 Keyword scoring

`calculateScore(...)` scans dimension-specific maps (`COST_KEYWORDS`, `LATENCY_KEYWORDS`, etc.) and:

- adds all matched keyword weights
- applies local intensity boost (`very`, `extremely`, `critically`, ...)
- averages matches
- applies a small multi-match bonus

So explicit, rich prompts naturally increase confidence and score stability.

### 2.3 Semantic scoring

`SemanticIntentMapper.extractScores(...)`:

- encodes sentence once using `SentenceBertLikeEncoder.encode(...)`
- compares with anchor phrase embeddings by cosine similarity
- keeps best similarity per dimension
- applies lexical fallback (`text.contains(anchorPhrase) -> >= 0.82`) for short/simple prompts

This gives resilience when exact keyword phrasing differs.

### 2.4 Negation handling (important fix)

Recent behavior uses **category-scoped negation**, not global negation.

`NaturalLanguageIntentParser.applyNegation(input, score, categoryTerms)`:

- checks only terms relevant to that dimension
- examples detected:
  - `latency is not important`
  - `speed doesn't matter`
  - `ignore latency`
  - `latency is low priority`
- downweights only that category (`score * 0.15`)

This prevents accidental suppression of all priorities and fixes misclassifications like latency-first when user says latency is unimportant.

### 2.5 Normalization and default fallback

After scoring:

- if total score > 1.0, vector is normalized by sum
- if no signal exists at all, defaults are used (`cost=0.5, latency=0.5, security=0.3, carbon=0.2`)

---

## 3) Semantic encoder details

`SentenceBertLikeEncoder` is a lightweight dependency-free approximation:

- fixed vector size: 256
- token hashing features
- synonym canonicalization (e.g., `cheap -> cost`, `fast -> latency`)
- unigram + bigram features
- L2 normalization
- cosine similarity for semantic matching

This is not a true transformer SBERT, but it preserves a similar calling model and can be swapped later without changing parser callers.

---

## 4) Historical user learning (pre-RL memory)

`IntentHistoryLearner` stores per-user intent history:

- learns each parser intent with `learn(userId, intent)`
- predicts next preference by weighted average of recent window (`windowSize=10`)
- recent intents have higher linear weight

This prediction is used as RL context in the blend step.

---

## 5) RL refinement internals

## 5.1 State, action, transition

In `ReinforcementIntentRefiner`:

- state = bucketized intent vector + bucketized system load
- action set:
  - `BOOST_COST`
  - `BOOST_LATENCY`
  - `BOOST_SECURITY`
  - `BOOST_CARBON`
  - `BALANCE`
- transition stored per user for later feedback update

State key format:

- `c-l-s-g-load` where each component is bucketed into 0..3

### 5.2 Blend before action

Before selecting action:

- `blended = 0.70 * parserIntent + 0.30 * historyPrediction`

This stabilizes refinement and gives personalized continuity.

### 5.3 Exploration/exploitation

Action policy is epsilon-greedy with decay:

- base epsilon from constructor (`0.15` default)
- adaptive decay by feedback count:
  - `epsilon_t = max(minEpsilon, epsilon * exp(-feedbackCount / 40.0))`
- `minEpsilon = 0.02`

So RL explores more early and exploits more as feedback grows.

### 5.4 Reward function

`computeReward(slaMet, observedCost, observedLatency)`:

- success term: `+1` if SLA met, else `-1`
- cost penalty: `0.08 * observedCost`
- latency penalty: `0.25 * (observedLatency / 100)`

$$
reward = success - 0.08 \cdot cost - 0.25 \cdot \frac{latency}{100}
$$

Interpretation:

- violating SLA pushes reward negative quickly
- higher cost/latency further reduce reward

### 5.5 Q-learning update

For selected action $a$ in state $s$:

$$
Q(s,a) \leftarrow Q(s,a) + \alpha \left(r + \gamma \max_{a'}Q(s',a') - Q(s,a)\right)
$$

Defaults:

- $\alpha = 0.20$
- $\gamma = 0.85$

---

## 6) Baseline vs refined evaluation (what “RL gain” means)

`IntentPipelineService.analyzeIntent(...)` evaluates **both**:

- baseline path: parsed intent only
- refined path: RL-refined intent

Each path runs:

1. SLA negotiation (`SLANegotiationAgent.negotiate(intent)`)
2. dynamic candidate generation (`buildCandidateCosts`, `buildCandidateLatencies`)
3. best option search (`CostPerformanceTradeoffEngine.findBestOption`)
4. score calculation (`tradeoffEngine.score`)

This enables explicit RL deltas:

- latency improvement %
- cost improvement %
- tradeoff score improvement %

These are shown in Tab 1 and used in Tab 3 RL panel.

---

## 7) SLA + tradeoff transformation

## 7.1 SLA negotiation

`SLANegotiationAgent` maps intent to constraints:

- higher latency priority -> tighter max latency
- higher cost priority -> tighter cost cap
- high cost + high latency triggers compromise relaxation
- security priority increases required availability/security level

## 7.2 Tradeoff scoring

`CostPerformanceTradeoffEngine.score(...)` combines weighted components:

- inverse cost
- inverse latency
- direct security
- inverse carbon

Higher score = better fit to intent priorities.

---

## 8) Feedback, maturity, and persistence

## 8.1 Feedback application

`IntentPipelineService.applyFeedback(...)`:

- updates RL Q-values and receives reward
- computes run-level baseline-vs-refined improvements
- appends `RunOutcome`
- trims to `HISTORY_WINDOW = 50`
- summarizes learning stats

`LearningStats` contains:

- run count
- maturity (`runCount >= 10`)
- maturity score (`min(1, runCount/10)`)
- avg latency/cost/score improvements
- avg reward
- reward trend (recent half - previous half)
- SLA success rate

## 8.2 Persistence

RL run history is saved/loaded at:

- `results/rl_learning_history.json`

On startup:

- history is loaded
- total prior runs warm-start RL (`rlRefiner.warmStart(totalRuns)`)

This preserves epsilon decay and maturity continuity across restarts.

---

## 9) How UI consumes NLP + RL outputs

In `IntentInputTab.parseAndAnalyze()`:

- collects parser confidence + dominant priority
- stores parsed and refined vectors in `CloudConfig`
- stores baseline/refined candidate and selected metrics
- stores historical RL stats for paneling
- computes placement hints (`CONSOLIDATED`, `SPREAD`, `ISOLATED`) from refined priorities

In `SimulationTab`:

- uses refined priorities + selected SLA/tradeoff fields to drive simulation dynamics
- RL panel verdict uses weighted historical signals:
  - gain signal
  - SLA stability signal
  - reward trend signal
- mature-run logic includes converged/stable status to avoid falsely labeling plateau as failure

---

## 10) Why you may see negative rewards despite “good looking” runs

This is expected if one or more are true:

- SLA miss (strong `-1` success term)
- observed cost too high for negotiated cap
- observed latency too high after penalties
- repetitive scenario where marginal gain vs baseline flattens

So “limited gain” can reflect either:

- truly weak policy improvement
- convergence near baseline parity

The updated mature verdict logic distinguishes these better.

---

## 11) Practical tuning knobs

If you want to tune behavior quickly:

1. Parser blend weights:
   - `KEYWORD_WEIGHT`, `SEMANTIC_WEIGHT` in `NaturalLanguageIntentParser`
2. Negation impact:
   - `score * 0.15` in parser scoped negation
3. RL exploration schedule:
   - base epsilon, decay divisor (`40.0`), `minEpsilon`
4. Reward shaping:
   - cost penalty `0.08`, latency penalty `0.25`
5. Maturity threshold:
   - `MATURE_RUN_COUNT` in `IntentPipelineService`
6. History smoothing window:
   - `HISTORY_WINDOW = 50`

---

## 12) Current strengths and limitations

Strengths:

- deterministic and explainable pipeline
- baseline-vs-RL comparison built in
- persisted learning continuity
- scoped negation handling for realistic prompts

Limitations:

- encoder is hashed approximation, not contextual transformer
- per-user state is lightweight and local-memory oriented
- feedback is simulated from analysis path in UI (not external telemetry)

---

## 13) Suggested next evolution

1. Replace `SentenceBertLikeEncoder` with true embedding service (same interface)
2. Persist Q-table itself (not only run outcomes)
3. Use richer state features (SLA slack, trend bins, workload class)
4. Feed real runtime telemetry into `applyFeedback(...)`
5. Add per-scenario stratified maturity metrics (not only aggregated)

---

## 14) Minimal sequence diagram (text)

User Prompt  
→ `IntentInputTab.parseAndAnalyze()`  
→ `NaturalLanguageIntentParser.parseWithConfidence()`  
→ `IntentPipelineService.analyzeIntent()`  
→ `IntentHistoryLearner.predict()` + `ReinforcementIntentRefiner.refineIntent()`  
→ baseline/refined evaluate (`SLANegotiationAgent` + `CostPerformanceTradeoffEngine`)  
→ UI receives recommendations + deltas  
→ `IntentPipelineService.applyFeedback()`  
→ RL Q-update + run-history summarization + JSON persistence  
→ Tab 3 shows RL impact and maturity/verdict.
