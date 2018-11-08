package com.net2plan.examples;

import com.net2plan.examples.general.offline.Offline_ipOverWdm_routingSpectrumAndModulationAssignmentHeuristicNotGrooming;
import com.net2plan.examples.general.offline.Offline_ipOverWdm_routingSpectrumAndModulationAssignmentILPNotGrooming;
import com.net2plan.examples.general.offline.nfv.Offline_nfvPlacementILP_v1;
import com.net2plan.examples.ocnbook.offline.*;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExamplesController
{
    private ExamplesController(){}

    private static Map<String, IAlgorithm> name2AlgorithmMap;
    private static Map<String, IReport> name2ReportMap;

    static
    {
        name2AlgorithmMap = new LinkedHashMap<>();
        name2AlgorithmMap.put("NFV Placement", new Offline_nfvPlacementILP_v1());
        name2AlgorithmMap.put("Routing Spectrum And Modulation Assignment Heuristic not grooming", new Offline_ipOverWdm_routingSpectrumAndModulationAssignmentHeuristicNotGrooming());
        name2AlgorithmMap.put("Routing Spectrum And Modulation Assignment ILP not grooming", new Offline_ipOverWdm_routingSpectrumAndModulationAssignmentILPNotGrooming());
        name2AlgorithmMap.put("BA Num Formulations", new Offline_ba_numFormulations());
        name2AlgorithmMap.put("CA Wireless CSMA Window Size", new Offline_ca_wirelessCsmaWindowSize());
        name2AlgorithmMap.put("CA Wireless Persistence Probability", new Offline_ca_wirelessPersistenceProbability());
        name2AlgorithmMap.put("CA Wireless Transmission Power", new Offline_ca_wirelessTransmissionPower());
        name2AlgorithmMap.put("CBA Congestion Control Link Bandwidth Split Two QoS", new Offline_cba_congControLinkBwSplitTwolQoS());
        name2AlgorithmMap.put("CBA Wireless Congestion Control Transmission Power Assignement", new Offline_cba_wirelessCongControlTransmissionPowerAssignment());
        name2AlgorithmMap.put("CFA Modular Capacities And Routing Dual Decomposition", new Offline_cfa_modularCapacitiesAndRoutingDualDecomposition());
        name2AlgorithmMap.put("CFA XP Multiperiod Modular Capacities", new Offline_cfa_xpMultiperiodModularCapacities());
        name2AlgorithmMap.put("FA OSPF Weight Optimization ACO", new Offline_fa_ospfWeightOptimization_ACO());
        name2AlgorithmMap.put("FA OSPF Weight Optimization EA", new Offline_fa_ospfWeightOptimization_EA());
        name2AlgorithmMap.put("FA OSPF Weight Optimization GRASP", new Offline_fa_ospfWeightOptimization_GRASP());
        name2AlgorithmMap.put("FA OSPF Weight Optimization Greedy", new Offline_fa_ospfWeightOptimization_greedy());
        name2AlgorithmMap.put("FA OSPF Weight Optimization Local Search", new Offline_fa_ospfWeightOptimization_localSearch());
        name2AlgorithmMap.put("FA OSPF Weight Optimization SAN", new Offline_fa_ospfWeightOptimization_SAN());
        name2AlgorithmMap.put("FA OSPF Weight Optimization Tabu Search", new Offline_fa_ospfWeightOptimization_tabuSearch());
        name2AlgorithmMap.put("FA XDE 1+1 Path Protection", new Offline_fa_xde11PathProtection());
        name2AlgorithmMap.put("FA XDE Formulations", new Offline_fa_xdeFormulations());
        name2AlgorithmMap.put("FA XDE Formulations Multicast", new Offline_fa_xdeFormulationsMulticast());
        name2AlgorithmMap.put("FA XDE Shared Restoration", new Offline_fa_xdeSharedRestoration());
        name2AlgorithmMap.put("FA XP 1+1 Path Protection", new Offline_fa_xp11PathProtection());
        name2AlgorithmMap.put("FA XP Formulations", new Offline_fa_xpFormulations());
        name2AlgorithmMap.put("FA XP Formulations Multicast", new Offline_fa_xpFormulationsMulticast());
        name2AlgorithmMap.put("FA XP Multihour Dynamic Routing", new Offline_fa_xpMultihourDynamicRouting());
        name2AlgorithmMap.put("FA XP Multihour Oblivious Routing", new Offline_fa_xpMultihourObliviousRouting());
        name2AlgorithmMap.put("FA XTE Formulations", new Offline_fa_xteFormulations());
        name2AlgorithmMap.put("TCA Node Location", new Offline_tca_nodeLocation());
        name2AlgorithmMap.put("TCFA General Multilayer", new Offline_tcfa_generalMultilayer());
        name2AlgorithmMap.put("TCFA WDM Physical Desing GRASP And ILP", new Offline_tcfa_wdmPhysicalDesign_graspAndILP());
        name2AlgorithmMap.put("TCFA XDE Formulations Minimum Link Cost", new Offline_tcfa_xdeFormulationsMinLinkCost());


        name2ReportMap = new LinkedHashMap<>();
    }

    public static IAlgorithm getAlgorithm(String name)
    {
        return name2AlgorithmMap.get(name);
    }

    public static IReport getReport(String name)
    {
        return name2ReportMap.get(name);
    }
}
