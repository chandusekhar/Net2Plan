package com.net2plan;


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

    protected static List<IAlgorithm> algorithms;
    protected static List<IReport> reports;
    protected static Map<String, List<IExternal>> catalog2ExternalMap;

    static
    {
        algorithms = new LinkedList<>();
        reports = new LinkedList<>();
        catalog2ExternalMap = new LinkedHashMap<>();
    }
}
