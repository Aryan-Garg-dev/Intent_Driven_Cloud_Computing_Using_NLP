# Quick Start Guide

## Overview

This project implements an **Intent-Driven Cloud Computing Simulation** with an interactive JavaFX GUI and advanced NLP engine. The system parses natural language, negotiates SLAs, and optimally places VMs based on user intent.

---

## 30-Second Setup

### **1. Prerequisites**
```
✓ Java 17+ (OpenJDK, Amazon Corretto, or Oracle)
✓ Maven 3.8+
```

**Check if installed:**
```bash
java -version          # Should show Java 17+
mvn -version          # Should show Maven 3.8+
```

### **2. Navigate to Project**
```bash
cd "e:\VIT Projects\Intent_Driven_Cloud_Computing_Using_NLP"
```

### **3. Run Interactive UI**

**Windows:**
```bash
run.bat
# Select option 2: "Run JavaFX UI Application"
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
# Select option 2: "Run JavaFX UI Application"
```

**Direct Maven:**
```bash
mvn clean package
mvn javafx:run
```

That's it! The JavaFX window will open within 10-30 seconds.

---

## What You'll See

### **Main Window**
A tabbed interface with 5 sections:

#### **Tab 1: Simulation Control**
- Run all 8 test scenarios
- Real-time log output
- Progress bar
- Status indicator

#### **Tab 2: Intent Parser** ⭐ NLP Engine
- Text input for natural language
- Instant parsing and priority visualization  
- Confidence score
- Smart recommendations

#### **Tab 3: Infrastructure**
- Visual datacenter diagram
- Hosts, VMs, and Cloudlets
- Real-time resource usage
- Adjustable infrastructure size

#### **Tab 4: Metrics & Results**
- Performance graphs
- Cost analysis
- SLA compliance tracking
- Export to CSV

#### **Tab 5: Trade-off Analysis**
- Pareto frontier visualization
- Cost vs Latency plot
- Configuration scoring
- Intelligent recommendations

---

## Quick Test: Intent Parsing

1. Click the **Intent Parser** tab
2. Paste this in the text box:
   ```
   I need fast, secure servers for banking at affordable cost
   ```
3. Click **🔍 Parse Intent**

**You should see:**
```
Cost Priority:     65%
Latency Priority:  70%
Security Priority: 95%  ← Dominant
Carbon Priority:   20%

Confidence: 92%
Recommendation: Use isolated/dedicated infrastructure with encryption
```

---

## Running Full Simulation

1. Go to **Simulation Control** tab
2. Select **All 8 Scenarios (Full)**
3. Click **▶ Run Simulation**

**What happens:**
- All 8 test intents are parsed
- SLAs are negotiated
- Trade-offs evaluated
- CloudSim simulation runs
- Results logged real-time
- Completion shows success stats

**Expected time:** 30-60 seconds

---

## CLI Simulation (For Scripting)

If you prefer command-line only:

```bash
mvn clean package
mvn exec:java -Dexec.mainClass=org.intentcloudsim.MainSimulation
```

**Output:**
```
╔══════════════════════════════════════════════════════════╗
║  Intent-Driven Autonomous Cloud Virtualization  ║
║  Patent: All 5 Ideas Combined                   ║
╚══════════════════════════════════════════════════════════╝

============================================================
EXPERIMENT 1 / 8
============================================================

[IntentParser] Input: "I want cheap and budget-friendly servers"
[IntentParser] Parsed: Intent[cost=0.90, latency=0.50, security=0.30, carbon=0.20]

--- STEP 2: Learn Intent ---
Predicted intent: Intent[cost=0.87, latency=0.45, security=0.28, carbon=0.19]

... (7 more experiments)

SIMULATION COMPLETE
Check 'results/' folder for CSV and graphs
```

---

## 8 Test Scenarios Explained

The simulation runs these diverse intents to prove system works:

| # | Intent | Expected Result |
|---|--------|-----------------|
| 1 | "cheap budget-friendly" | Consolidated VMs, low cost |
| 2 | "fast real-time gaming" | High-performance hosts |
| 3 | "secure banking" | Isolated, encrypted hosts |
| 4 | "balanced cost-effective" | Trade-off optimized |
| 5 | "green sustainable" | Renewable energy DC |
| 6 | "high performance secure" | Both premium features |
| 7 | "fastest possible, money no object" | Ultra-premium tier |
| 8 | "minimize cost, latency doesn't matter" | Cheapest tier |

---

## File Locations

After running, check these folders:

```
results/
├── simulation_results.csv          # Full results data
├── latency_graph.png               # Performance plot
├── cost_analysis.png               # Cost breakdown
├── sla_compliance.png              # SLA tracking
└── trade_off_analysis.png          # Pareto frontier
```

---

## NLP Engine Examples

### **Input:** "Give me the fastest servers"
```
Latency Priority: 95%  ← Dominant
Cost Priority: 30%
Security Priority: 40%
Carbon Priority: 20%
Confidence: 88%
```

### **Input:** "I don't care about latency, just minimize cost"
```
Cost Priority: 95%     ← Dominant
Latency Priority: 0%   ← Explicitly negated
Security Priority: 30%
Carbon Priority: 20%
Confidence: 91%
```

### **Input:** "Very secure encrypted HIPAA compliant infrastructure"
```
Security Priority: 98%  ← Intensity modifiers applied
Cost Priority: 40%
Latency Priority: 35%
Carbon Priority: 20%
Confidence: 95%
```

---

## Troubleshooting

### **Problem: "mvn: command not found"**
- Install Maven from https://maven.apache.org/
- Or add Maven to PATH
- Or use `./mvnw` (Maven Wrapper) if available

### **Problem: "Java not found" or "Java too old"**
- Install Java 17+ from https://adoptium.net/
- Verify with: `java -version`

### **Problem: JavaFX window doesn't appear**
- Ensure you have 1280x720 minimum resolution
- Try: `mvn exec:java -Dexec.mainClass=org.intentcloudsim.ui.SimulationUI`

### **Problem: Maven downloads take forever**
- First build can be slow (downloading dependencies)
- Subsequent builds will be much faster
- Check internet connection

### **Problem: Out of memory**
- Increase heap: `mvn exec:java -Dexec.maxMemory=2g ...`

---

## Key Features

✓ **5 Patented Ideas Implemented**
- Natural Language Intent Parsing
- Autonomous SLA Negotiation
- Multi-Objective Trade-Off Optimization
- Intent-Aware VM Placement
- User Intent History Learning

✓ **Advanced NLP Engine**
- Keyword extraction (60+ keywords)
- Intensity modifiers ("very", "extremely")
- Negation handling ("don't care about")
- Confidence scoring

✓ **Modern JavaFX UI**
- Real-time visualization
- Professional charts and graphs
- Infrastructure diagram
- Responsive controls

✓ **CloudSim Plus Integration**
- Full datacenter simulation
- 4 hosts × 4 VMs × 8 cloudlets
- Intent-aware placement actual execution
- Accurate metrics collection

✓ **Complete Documentation**
- Research paper quality README
- This quickstart guide
- Inline code comments
- Build scripts for all platforms

---

## Next Steps

1. **Explore Intent Parser** - Try different natural language inputs
2. **Run Full Simulation** - See all 8 scenarios execute
3. **Check Metrics** - View performance and cost graphs
4. **Analyze Trade-offs** - Understand Pareto frontier
5. **Read README** - Dive into technical details

---

## Performance Expectations

| Operation | Expected Time |
|-----------|----------------|
| First build | 60-120 seconds |
| Subsequent builds | 20-30 seconds |
| JavaFX startup | 10-20 seconds |
| Single scenario | 5-10 seconds |
| All 8 scenarios | 45-90 seconds |

---

## Support & Documentation

- **README.md** - Complete technical documentation
- **Code comments** - Inline explanations in all Java files
- **This guide** - Quick reference

---

## Project Status

✓ **Complete and tested**
- All 5 patent ideas implemented
- JavaFX UI fully functional
- 8 test scenarios pass
- Cloud simulation accurate
- Results exported to CSV and graphs

---

**Ready to start?** Run `run.bat` (Windows) or `./run.sh` (Linux/Mac) now!

