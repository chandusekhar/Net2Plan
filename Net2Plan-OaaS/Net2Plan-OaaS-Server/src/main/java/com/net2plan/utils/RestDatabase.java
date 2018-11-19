package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestDatabase
{
    private RestDatabase(){}

    public static NetPlan netPlan;
    public static Map<String, List<IAlgorithm>> jar2AlgorithmsMap;
    public static Map<String, List<IReport>> jar2ReportsMap;

    static
    {
        netPlan = new NetPlan();
        jar2AlgorithmsMap = new LinkedHashMap<>();
        jar2ReportsMap = new LinkedHashMap<>();
    }
}
