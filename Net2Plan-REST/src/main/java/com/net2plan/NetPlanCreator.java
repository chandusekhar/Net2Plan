package com.net2plan;

import com.net2plan.interfaces.networkDesign.NetPlan;

public class NetPlanCreator
{
    public static NetPlan netPlan;

    static
    {
        netPlan = new NetPlan();
    }

}
