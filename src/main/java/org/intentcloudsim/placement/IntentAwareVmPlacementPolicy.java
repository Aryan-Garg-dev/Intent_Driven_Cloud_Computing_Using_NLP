package org.intentcloudsim.placement;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;
import org.intentcloudsim.intent.Intent;

import java.util.List;

/**
 * PATENT IDEA 20: Intent-Based VM Placement
 *
 * Places VMs on hosts based on user intent rather than just
 * resource availability. Considers cost, latency, security,
 * and carbon priorities.
 *
 * NOVELTY: Traditional placement only considers CPU/RAM fit.
 * This considers human intent as the primary driver.
 */
public class IntentAwareVmPlacementPolicy {

    /**
     * Select the best host for a VM based on user intent.
     *
     * @param vm The VM to place
     * @param hostList Available hosts
     * @param intent The user's intent
     * @return The best host, or null if none suitable
     */
    public Host selectHost(Vm vm, List<Host> hostList, Intent intent) {

        System.out.println("\n===== INTENT-BASED VM PLACEMENT =====");
        System.out.println("[Placement] Placing VM " + vm.getId() +
                           " with intent: " + intent);

        Host bestHost = null;
        double bestScore = -1;

        for (Host host : hostList) {
            // Skip hosts that don't have enough resources
            if (!hasEnoughResources(host, vm)) {
                System.out.println("[Placement] Host " + host.getId() +
                                   " → SKIP (insufficient resources)");
                continue;
            }

            double score = calculateHostScore(host, vm, intent);

            System.out.printf("[Placement] Host %d → score=%.2f " +
                            "(CPU=%.0f, RAM=%d, BW=%d)%n",
                            host.getId(), score,
                            host.getTotalMipsCapacity(),
                            host.getRam().getCapacity(),
                            host.getBw().getCapacity());

            if (score > bestScore) {
                bestScore = score;
                bestHost = host;
            }
        }

        if (bestHost != null) {
            System.out.println("[Placement] SELECTED: Host " +
                               bestHost.getId() +
                               " (score=" + String.format("%.2f", bestScore) +
                               ")");
        } else {
            System.out.println("[Placement] WARNING: No suitable host found!");
        }
        System.out.println("======================================\n");

        return bestHost;
    }

    /**
     * Calculate how well a host matches the user's intent.
     */
    private double calculateHostScore(Host host, Vm vm, Intent intent) {
        double score = 0;

        // PERFORMANCE SCORE (for latency priority)
        // More MIPS available → better performance → lower latency
        double availableMips = host.getTotalMipsCapacity() -
                               host.getTotalAllocatedMips();
        double performanceScore = availableMips /
                                  host.getTotalMipsCapacity();
        score += intent.getLatencyPriority() * performanceScore * 40;

        // COST SCORE (for cost priority)
        // Less loaded host = we're using expensive resources
        // More loaded host = better resource utilization = cheaper
        double utilization = host.getTotalAllocatedMips() /
                            Math.max(1, host.getTotalMipsCapacity());
        double costScore = utilization; // higher util = cheaper per VM
        score += intent.getCostPriority() * costScore * 30;

        // SECURITY SCORE (for security priority)
        // Fewer VMs on host → more isolation → more secure
        long existingVms = host.getVmList().size();
        double securityScore = 1.0 / (1.0 + existingVms);
        score += intent.getSecurityPriority() * securityScore * 20;

        // CARBON SCORE (for carbon priority)
        // Choose hosts with lower power draw
        // Consolidation (higher utilization) = fewer hosts needed = greener
        double carbonScore = utilization;
        score += intent.getCarbonPriority() * carbonScore * 10;

        return score;
    }

    /**
     * Check if host has enough resources for this VM.
     */
    private boolean hasEnoughResources(Host host, Vm vm) {
        boolean enoughPes = host.getFreePesNumber() >=
                           vm.getPesNumber();
        boolean enoughRam = host.getRam().getAvailableResource() >=
                           vm.getRam().getCapacity();
        boolean enoughBw = host.getBw().getAvailableResource() >=
                          vm.getBw().getCapacity();

        return enoughPes && enoughRam && enoughBw;
    }

    /**
     * Compare two placements and return which is better
     * for the given intent.
     */
    public String comparePlacements(Host host1, Host host2,
                                     Vm vm, Intent intent) {
        double score1 = calculateHostScore(host1, vm, intent);
        double score2 = calculateHostScore(host2, vm, intent);

        return "Host " + host1.getId() + " score=" +
               String.format("%.2f", score1) +
               " vs Host " + host2.getId() + " score=" +
               String.format("%.2f", score2) +
               " → Winner: Host " +
               (score1 > score2 ? host1.getId() : host2.getId());
    }
}

