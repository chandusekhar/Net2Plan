package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;

import java.util.LinkedList;
import java.util.List;

public class RestDatabase
{
    private RestDatabase(){}

    public static NetPlan netPlan;
    public static List<IAlgorithm> algorithmsList;
    public static List<IReport> reportsList;

    static
    {
        netPlan = new NetPlan();
        algorithmsList = new LinkedList<>();
        reportsList = new LinkedList<>();
    }
}
