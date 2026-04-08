# Metrics & Calculation Presentation Guide

This document explains how all major numbers shown in the UI are calculated, including:
- SLA terms
- cost prediction
- provisioned architecture
- graph behavior
- SLA violation counting
- RL impact verdicts

**Primary code references**
- `src/main/java/org/intentcloudsim/ui/tabs/IntentInputTab.java`
- `src/main/java/org/intentcloudsim/api/IntentPipelineService.java`
- `src/main/java/org/intentcloudsim/sla/SLANegotiationAgent.java`
- `src/main/java/org/intentcloudsim/tradeoff/CostPerformanceTradeoffEngine.java`
- `src/main/java/org/intentcloudsim/ui/tabs/SimulationTab.java`

---

## 1) SLA calculation (from intent to contract)

Implemented in `SLANegotiationAgent.negotiate(...)`.

Provider limits:
- latency: 10–200 ms
- cost: $0.50–$20.00/hr
- base availability: 95%

Given intent priorities in $[0,1]$:

$$
\text{maxLatency} = 200 - p_{latency}\cdot(200-10)
$$
$$
\text{maxCost} = 20 - p_{cost}\cdot(20-0.5)
$$
$$
\text{minAvailability} = 95 + 4.9\cdot p_{security}
$$
$$
\text{securityLevel} = 10\cdot p_{security}
$$
$$
\text{maxCarbon} = 100 - 80\cdot p_{carbon}
$$

Conflict relaxation (if both cost and latency priorities are > 0.7):
- maxLatency × 1.2
- maxCost × 1.15

---

## 2) Candidate option generation (cost + latency arrays)

Implemented in `IntentPipelineService`.

## 2.1 Candidate costs

Scale factor:
$$
\text{scale}=0.80 + 0.30\cdot p_{latency} + 0.20\cdot p_{security}
$$

Let `budgetCap = max(0.5, sla.maxCostPerHour)`.
Candidates (rounded to 2 decimals):
- $\max(0.5,\ budgetCap\cdot0.35\cdot scale)$
- $\max(0.8,\ budgetCap\cdot0.60\cdot scale)$
- $\max(1.2,\ budgetCap\cdot0.85\cdot scale)$
- $\max(1.5,\ budgetCap\cdot1.00\cdot scale)$

## 2.2 Candidate latencies

With `target = max(12, sla.maxLatencyMs)`,
`loadMultiplier = 1 + clamp(systemLoad,0,1)*0.40`,
`perfBias = 1 - p_latency*0.18`.

Candidates (rounded to 1 decimal):
- $target\cdot1.70\cdot loadMultiplier$
- $target\cdot1.30\cdot loadMultiplier\cdot perfBias$
- $target\cdot1.00\cdot loadMultiplier\cdot perfBias$
- $target\cdot0.72\cdot loadMultiplier\cdot perfBias$

---

## 3) Tradeoff score and best option

Implemented in `CostPerformanceTradeoffEngine`.

Score model:
$$
S = p_c\cdot\frac{10}{cost}
+ p_l\cdot\frac{1000}{latency}
+ p_s\cdot securityLevel
+ p_{co2}\cdot\frac{100}{carbonEmission}
$$

Simplified pipeline call uses default `securityLevel=5`, `carbonEmission=50`.

Best option = candidate index with highest score.

---

## 4) Observed feedback values (for RL reward)

In `IntentInputTab.parseAndAnalyze(...)`:

Observed cost:
$$
\text{observedCost}=\text{selectedCost}\cdot\left(1+\max(0,systemLoad-0.65)\cdot0.20\right)
$$

Observed latency:
$$
\text{observedLatency}=\text{selectedLatency}\cdot(1+0.25\cdot systemLoad)
$$

SLA met check:
- observedCost <= SLA maxCost
- observedLatency <= SLA maxLatency

This feeds RL reward calculation.

---

## 5) Provisioned architecture calculation (intent-derived)

Implemented in `IntentInputTab.applyDynamicProvisioning(...)`.

Inputs:
- `bestTier` (0..3 from chosen option)
- refined priorities (cost, latency, security, carbon)
- SLA strictness
- system load
- prompt length

## 5.1 Hosts
$$
hosts = clamp_{[2,14]}(hostBase + perfScale + securityScale + loadScale - costReduction)
$$
where
- `hostBase = 3 + bestTier`
- `perfScale = round(latencyPriority*4)`
- `securityScale = round(securityPriority*2)`
- `loadScale = round(systemLoad*3)`
- `costReduction = round(costPriority*3)`

## 5.2 Host specs
- `hostCores = clamp[8,32](8 + 2*bestTier + round(latencyPriority*6) + (SLA maxLatency<=80 ? 2 : 0))`
- `hostRamGb = clamp[16,128](16 + 8*bestTier + round(latencyPriority*24))`
- `hostStorageGb = clamp[800,4800](800 + 400*bestTier + round((1-carbonPriority)*300))`

## 5.3 VM layer
- `vmDensity = clamp[1,4](1 + bestTier + round((1-securityPriority)*1.5))`
- `numVMs = clamp[2, hosts*4](hosts * vmDensity)`
- `vmCores = clamp[2,8](2 + bestTier + (latencyPriority>0.7 ? 1 : 0))`
- `vmRamGb = clamp[4,24](4 + 2*bestTier + round(securityPriority*4))`

## 5.4 Workload volume
$$
cloudlets = clamp_{[8,300]}\left(round(numVMs\cdot(1.8 + 1.6\cdot systemLoad + wordCount/40))\right)
$$
Add +12 if very strict performance intent (`latencyPriority>0.8` and SLA maxLatency<70).

---

## 6) Cost analysis panel calculation

Implemented in `SimulationTab.estimateHourlyCosts()`.

Derived factors:
$$
costSensitivity = 1.10 - 0.35\cdot p_{cost}
$$
$$
performancePremium = 0.85 + 0.50\cdot p_{latency} + 0.20\cdot p_{security}
$$

Component costs:
$$
compute = numHosts\cdot\left(0.18 + hostCores\cdot0.05\cdot performancePremium\cdot costSensitivity\right)
$$
$$
storage = \frac{totalStorageGb}{1000}\cdot\left(0.03 + 0.04\cdot p_{security}\right)
$$
$$
network = numVMs\cdot\left(0.02 + 0.06\cdot p_{latency}\right)
$$
$$
total = compute + storage + network
$$

Adjustments:
- green datacenter discount: `total × 0.97`
- blend with tradeoff-selected hourly cost if present:
  `total = (total + selectedCostPerHour)/2`

---

## 7) Simulation timeline and graph calculation

Implemented in `SimulationTab.startSimulation()`.

## 7.1 Duration
$$
simulationSeconds = clamp_{[45,360]}\left(numCloudlets\cdot(2.4 - p_{latency})\right)
$$

## 7.2 Latency targets
- SLA-based target and option latency are blended.
- Baseline latency scales with system load.
- RL gain reduces target/baseline.

## 7.3 Curve shape parameters
Intent profile (`LATENCY_FIRST`, `COST_FIRST`, etc.) sets defaults for:
- convergence exponent,
- burst center/width/amplitude,
- oscillation cycles/amplitude.
Then they are adjusted by priorities and system load.

## 7.4 Graph point formula
At each step (
$progress=step/100$), avg latency is:
- convergence term + transient burst + oscillation + deterministic ripple,
- then adjusted by placement policy (`SPREAD`, `CONSOLIDATED`, `ISOLATED`),
- clamped to [8, 2000] ms.

Every 5 steps, a latency point is appended to the line chart.

---

## 8) SLA violation counting in simulation

Not a hardcoded constant anymore; it is sampled over runtime.

Rules:
- warmup grace: first 10 steps ignored,
- check every 5 steps,
- latency tolerance = `maxLatencyMs × 1.06`,
- cost tolerance = `maxCostPerHour × 1.04` using runtime run-rate.

Violation increments if either threshold is exceeded at a check.

Final compliance shown as:
$$
\text{SLA compliance \%} = \left(1 - \frac{violations}{checks}\right)\cdot 100
$$
(or 100% if 0 violations and checks > 0).

---

## 9) RL impact panel verdict calculation

In `SimulationTab.refreshDisplayPanels()`:

Weighted historical gain:
$$
weightedGain = 0.45\cdot scoreGain + 0.30\cdot latencyGain + 0.25\cdot costGain
$$

Stability signal:
$$
stabilitySignal = (slaSuccessRate - 0.5)\cdot100
$$

Trend signal:
$$
trendSignal = rewardTrend\cdot20
$$

Data-driven score:
$$
dataDrivenScore = weightedGain + stabilitySignal + trendSignal
$$

Status label logic then classifies RL as:
- warming up,
- improving,
- stable/plateau,
- converged near parity,
- or limited gain.

---

## 10) Presenter-friendly storyline 

1. **Intent in plain English** enters Tab 1.
2. System computes **NLP priorities + diagnostics**.
3. RL refines priorities from historical feedback.
4. SLA terms are negotiated quantitatively.
5. Candidate options are scored by intent-weighted tradeoff.
6. Provisioning is generated dynamically from intent/SLA/load.
7. Simulation visualizes latency/cost progression and SLA compliance.
8. RL panel explains whether learning is truly improving outcomes.

This gives a full, explainable chain from text intent to provisioned infrastructure and measured outcomes.
