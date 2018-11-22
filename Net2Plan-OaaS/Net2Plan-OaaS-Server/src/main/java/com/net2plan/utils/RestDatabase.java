package com.net2plan.utils;


import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.internal.IExternal;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RestDatabase
{
    private RestDatabase(){}

    public static List<IAlgorithm> algorithms;
    public static List<IReport> reports;
    public static Map<String, List<IExternal>> catalog2ExternalMap;

    static
    {
        algorithms = new LinkedList<>();
        reports = new LinkedList<>();
        catalog2ExternalMap = new LinkedHashMap<>();
    }
}
