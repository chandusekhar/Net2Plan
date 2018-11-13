package com.net2plan.interfaces.networkDesign;

import com.shc.easyjson.JSONObject;

interface IReaderNetPlan_JSON extends IReaderNetPlan
{
    public void createFromJSON(NetPlan netPlan, JSONObject json);
}
