# RL Trace Table (Pre-filled, 8 Experiments)

Source: `results/latest_run.log` from latest run.

| Iteration | User Intent (short) | State Key | Action Chosen | Refined Intent (c,l,s,co) | SLA Met | Cost | Latency | Reward | Updated Q(s,a) | Note |
|---:|---|---|---|---|---|---:|---:|---:|---:|---|
| 1 | cheap + budget-friendly startup servers | `3-0-0-0-1` | `BOOST_COST` | `(0.88, 0.18, 0.15, 0.14)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Strong cost focus, but cost still exceeded SLA max cost |
| 2 | fast real-time low-latency gaming | `1-3-0-0-2` | `BOOST_COST` | `(0.37, 0.73, 0.15, 0.14)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Latency satisfied, cost violated SLA |
| 3 | secure encrypted compliant banking infra | `0-0-3-0-2` | `BOOST_COST` | `(0.34, 0.10, 0.78, 0.14)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Security high, but budget constraint still violated |
| 4 | balanced cost-effective + responsive | `2-2-0-0-1` | `BOOST_COST` | `(0.86, 0.55, 0.00, 0.02)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Combined priorities, but selected cost exceeded SLA |
| 5 | green sustainable carbon-neutral workload | `0-1-0-2-2` | `BOOST_COST` | `(0.22, 0.22, 0.06, 0.65)` | `true` | 15.0 | 20.0 | -0.250 | -0.050 | Only scenario meeting both latency and cost SLA |
| 6 | high performance + secure + affordable | `2-2-3-0-3` | `BOOST_COST` | `(0.72, 0.55, 0.87, 0.07)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Security and speed high; cost target too strict |
| 7 | fastest execution, money no object | `1-3-0-0-1` | `BOOST_COST` | `(0.44, 0.72, 0.00, 0.01)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Latency met, but generated SLA cost cap still violated |
| 8 | minimize cost, latency doesn't matter | `2-0-0-0-3` | `BOOST_COST` | `(0.72, 0.12, 0.02, 0.19)` | `false` | 15.0 | 20.0 | -2.250 | -0.450 | Cost-prioritized intent but selected option remained expensive |

## Legend

- `(c,l,s,co)` = `(cost, latency, security, carbon)` priorities.
- Reward from current implementation:
  - `+1` if SLA met, else `-1`
  - minus cost penalty `0.08 * cost`
  - minus latency penalty `0.25 * (latency / 100)`
