package com.net2plan;


import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.Constants;
import com.shc.easyjson.*;
import javax.ws.rs.core.Response;
import java.io.File;

public class PruebaCliente
{
    public static void main(String [] args)
    {


        Net2PlanOaaSClient net2planClient = new Net2PlanOaaSClient("localhost","","");

        File catalog = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\BuiltInExamples.jar");
        Response uploadResponse = net2planClient.uploadCatalog(catalog);
        System.out.println("UPLOAD CATALOG ->" + uploadResponse.getStatus()+", "+uploadResponse.readEntity(String.class));

        JSONObject json = new JSONObject();
        json.put("type",new JSONValue("ALGORITHM"));
        json.put("name",new JSONValue("com.net2plan.examples.ocnbook.offline.Offline_fa_ospfWeightOptimization_EA"));
        json.put("userParams", new JSONValue(new JSONArray()));

        File topologyFile = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\data\\networkTopologies\\example7nodes.n2p");
        NetPlan netPlan = new NetPlan(topologyFile);
        Response executeResponse = net2planClient.execute(OaaSUtils.ExecutionType.ALGORITHM, "com.net2plan.examples.ocnbook.offline.Offline_fa_ospfWeightOptimization_EA", null, netPlan);

        System.out.println("EXECUTE ->" + executeResponse.getStatus()+", "+executeResponse.readEntity(String.class));
    }
}
