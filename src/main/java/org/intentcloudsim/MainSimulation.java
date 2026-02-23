package org.intentcloudsim;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import org.intentcloudsim.intent.*;
import org.intentcloudsim.sla.*;
import org.intentcloudsim.tradeoff.*;
import org.intentcloudsim.placement.*;
import org.intentcloudsim.util.*;

import java.util.ArrayList;
import java.util.List;

/**
 * MAIN SIMULATION: Intent-Driven Autonomous Cloud Virtualization
 *
 * This brings together ALL 5 patent ideas into one simulation.
 *
 * Flow:
 * 1. User provides natural language intent
 * 2. Intent is parsed into a vector
 * 3. SLA is auto-negotiated from intent
 * 4. Tradeoff engine evaluates options
 * 5. VM is placed on best host based on intent
 * 6. Simulation runs in CloudSim Plus
 * 7. Results are logged and graphed
 */
public class MainSimulation {

    // CloudSim Plus configuration
    private static final int NUM_HOSTS = 4;
    private static final int HOST_PES = 8;        // cores per host
    private static final long HOST_MIPS = 10000;   // speed per core
    private static final long HOST_RAM = 16384;    // 16 GB
    private static final long HOST_BW = 10000;     // 10 Gbps
    private static final long HOST_STORAGE = 1000000; // 1 TB

    private static final int NUM_VMS = 4;
    private static final int VM_PES = 2;
    private static final long VM_MIPS = 2500;
    private static final long VM_RAM = 4096;       // 4 GB
    private static final long VM_BW = 1000;

    private static final int NUM_CLOUDLETS = 8;
    private static final long CLOUDLET_LENGTH = 50000;
    private static final int CLOUDLET_PES = 1;

    // Test scenarios with different user intents
    private static final String[] TEST_INTENTS = {
        "I want cheap and budget-friendly servers for my startup",
        "I need fast real-time low latency processing for gaming",
        "Deploy secure encrypted compliant infrastructure for banking",
        "I want a balanced solution that is cost-effective and responsive",
        "Run my workload on green sustainable carbon neutral infrastructure",
        "I need high performance secure servers at affordable cost",
        "Give me the fastest possible execution, money is no object",
        "Minimize cost as much as possible, latency doesn't matter"
    };

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Intent-Driven Autonomous Cloud Virtualization  ║");
        System.out.println("║  Patent: All 5 Ideas Combined                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // Initialize components
        MetricsLogger logger = new MetricsLogger("results");
        IntentHistoryLearner learner = new IntentHistoryLearner();
        SLANegotiationAgent slaAgent = new SLANegotiationAgent();
        CostPerformanceTradeoffEngine tradeoffEngine =
            new CostPerformanceTradeoffEngine();
        IntentAwareVmPlacementPolicy placementPolicy =
            new IntentAwareVmPlacementPolicy();

        // Run simulation for each test intent
        for (int i = 0; i < TEST_INTENTS.length; i++) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("EXPERIMENT " + (i + 1) + " / " +
                               TEST_INTENTS.length);
            System.out.println("=".repeat(60));

            String userInput = TEST_INTENTS[i];

            // ===== STEP 1: Parse Intent (Patent Idea 16) =====
            System.out.println("\n--- STEP 1: Parse Intent ---");
            Intent intent = NaturalLanguageIntentParser.parse(userInput);

            // ===== STEP 2: Learn from History (Patent Idea 19) =====
            System.out.println("\n--- STEP 2: Learn Intent ---");
            String userId = "user_" + (i % 3); // simulate 3 users
            learner.learn(userId, intent);
            Intent predicted = learner.predict(userId);
            System.out.println("Predicted intent: " + predicted);

            // ===== STEP 3: Negotiate SLA (Patent Idea 17) =====
            System.out.println("\n--- STEP 3: Negotiate SLA ---");
            SLAContract sla = slaAgent.negotiate(intent);

            // ===== STEP 4: Evaluate Tradeoffs (Patent Idea 18) =====
            System.out.println("\n--- STEP 4: Evaluate Tradeoffs ---");
            double[] candidateCosts = {2.0, 5.0, 10.0, 15.0};
            double[] candidateLatencies = {150.0, 80.0, 40.0, 20.0};
            int bestOption = tradeoffEngine.findBestOption(
                candidateCosts, candidateLatencies, intent);
            double selectedCost = candidateCosts[bestOption];
            double selectedLatency = candidateLatencies[bestOption];
            double tradeoffScore = tradeoffEngine.score(
                selectedCost, selectedLatency, intent);

            // ===== STEP 5: Run CloudSim Plus (Patent Idea 20) =====
            System.out.println("\n--- STEP 5: CloudSim Plus Simulation ---");
            runCloudSimulation(intent, placementPolicy, i);

            // ===== STEP 6: Log Results =====
            boolean slaSatisfied = sla.isSatisfied(
                selectedLatency, selectedCost, 99.5);

            logger.addRecord(
                userInput, intent.getCostPriority(),
                intent.getLatencyPriority(), intent.getSecurityPriority(),
                sla.getMaxLatencyMs(), sla.getMaxCostPerHour(),
                selectedLatency, selectedCost,
                bestOption, tradeoffScore, slaSatisfied
            );
        }

        // Print summary and save results
        logger.printSummary();
        logger.saveToCSV("simulation_results.csv");

        // Generate graphs
        System.out.println("\n--- Generating Graphs ---");
        GraphGenerator graphGen = new GraphGenerator("results");
        graphGen.generateAll();

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║  SIMULATION COMPLETE                             ║");
        System.out.println("║  Check 'results/' folder for CSV and graphs      ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    /**
     * Run a CloudSim Plus simulation with intent-based placement.
     */
    private static void runCloudSimulation(Intent intent,
            IntentAwareVmPlacementPolicy placementPolicy,
            int experimentId) {

        // Create CloudSim Plus instance
        CloudSimPlus simulation = new CloudSimPlus();

        // Create Datacenter with Hosts
        Datacenter datacenter = createDatacenter(simulation);

        // Create Broker (manages VMs and Cloudlets)
        DatacenterBrokerSimple broker =
            new DatacenterBrokerSimple(simulation);

        // Create VMs
        List<Vm> vmList = createVms();

        // Create Cloudlets (tasks to execute)
        List<Cloudlet> cloudletList = createCloudlets();

        // Intent-based placement: select best host for each VM
        List<Host> hostList = datacenter.getHostList();
        for (Vm vm : vmList) {
            Host bestHost = placementPolicy.selectHost(
                vm, hostList, intent);
            if (bestHost != null) {
                System.out.println("[Simulation] VM " + vm.getId() +
                    " → Host " + bestHost.getId());
            }
        }

        // Submit VMs and Cloudlets to broker
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        // Run simulation
        simulation.start();

        // Print results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        System.out.println("\n--- Experiment " + experimentId + " Results ---");
        new CloudletsTableBuilder(finishedCloudlets).build();
    }

    /**
     * Create a datacenter with multiple hosts.
     */
    private static Datacenter createDatacenter(CloudSimPlus simulation) {
        List<Host> hostList = new ArrayList<>();

        for (int i = 0; i < NUM_HOSTS; i++) {
            // Each host has different specs to test placement
            long mips = HOST_MIPS + (i * 2000); // varying performance
            long ram = HOST_RAM + (i * 4096);    // varying RAM

            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < HOST_PES; j++) {
                peList.add(new PeSimple(mips));
            }

            Host host = new HostSimple(ram, HOST_BW, HOST_STORAGE, peList)
                .setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    /**
     * Create VMs for the simulation.
     */
    private static List<Vm> createVms() {
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < NUM_VMS; i++) {
            Vm vm = new VmSimple(VM_MIPS, VM_PES)
                .setRam(VM_RAM)
                .setBw(VM_BW)
                .setSize(10000)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());
            vmList.add(vm);
        }
        return vmList;
    }

    /**
     * Create cloudlets (tasks) for the simulation.
     */
    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < NUM_CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(
                CLOUDLET_LENGTH + (i * 10000), CLOUDLET_PES)
                .setUtilizationModelCpu(
                    new UtilizationModelDynamic(0.5));
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }
}

