package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.NetPlan;

public class RestDatabase
{
    private RestDatabase(){}

    public static NetPlan netPlan;

    static
    {
        netPlan = new NetPlan();
    }
}
