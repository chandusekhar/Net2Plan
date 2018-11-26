package com.net2plan;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.Version;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Map;

public class Net2PlanOaaSClient
{
    private String baseURL;
    private Client client;
    private WebTarget target;
    private final int defaultPort = 8080;

    public Net2PlanOaaSClient(String ipAddress, String user, String password, int... optionalPort)
    {
        int port;
        if(optionalPort.length == 0)
            port = defaultPort;
        else if(optionalPort.length == 1)
            port = optionalPort[0];
        else
            throw new Net2PlanException("More than one port is not allowed");

        this.baseURL =  "http://"+ipAddress+":"+port+"/net2plan-oaas-server-"+ Version.getVersion()+"-SNAPSHOT/OaaS";
        this.client = ClientBuilder.newClient().register(MultiPartFeature.class);
        this.target = this.client.target(baseURL);
    }

    /**
     * Uploads a catalog (JAR file) including different algorithms and/or reports
     * @param catalogFile catalog (JAR file)
     * @return HTTP Response
     */
    public Response uploadCatalog(File catalogFile)
    {
        WebTarget this_target = target.path("catalogs");
        FileDataBodyPart body = new FileDataBodyPart("file",catalogFile);
        MultiPart multi = new MultiPart();
        multi.bodyPart(body);

        Invocation.Builder inv = this_target.request(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON);
        Response r = inv.post(Entity.entity(multi,MediaType.MULTIPART_FORM_DATA));

        return r;
    }

    /**
     * Send an execution (algorithm or report) request to OaaS API
     * @param type execution type (ALGORITHM, REPORT)
     * @param name algorithm or report name to execute
     * @param userParams Map including names and customized values of the execution parameters
     * @param netPlan input NetPlan
     * @return HTTP Response
     */
    public Response execute(String type, String name, Map<String, String> userParams, NetPlan netPlan)
    {
        WebTarget this_target = target.path("execute");

        JSONObject json = new JSONObject();
        json.put("type",new JSONValue(type));
        json.put("name",new JSONValue(name));
        if(userParams == null || userParams.size() == 0)
            json.put("userparams", new JSONValue(new JSONArray()));
        else
        {
            JSONArray paramsArray = ClientUtils.parseUserParameters(userParams);
            json.put("userparams", new JSONValue(paramsArray));
        }

        JSONObject netPlanJSON = netPlan.saveToJSON();
        json.put("netPlan", new JSONValue(netPlanJSON));

        Invocation.Builder inv = this_target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
        Response r = inv.post(Entity.entity(JSON.write(json), MediaType.APPLICATION_JSON_TYPE));

        return r;
    }

    public static void main(String [] args)
    {
        Net2PlanOaaSClient client = new Net2PlanOaaSClient("localhost","","");

        File catalog = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\BuiltInExamples.jar");
        Response r = client.uploadCatalog(catalog);
        System.out.println("UPLOAD CATALOG -> "+r.getStatus()+", "+r.readEntity(String.class));

        File topologyFile = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\data\\networkTopologies\\example7nodes.n2p");
        NetPlan netPlan = new NetPlan(topologyFile);
        Response r2 = client.execute("ALGORITHM","com.net2plan.examples.ocnbook.offline.Offline_fa_ospfWeightOptimization_EA",null, netPlan);
        System.out.println("EXECUTE -> "+r2.getStatus()+", "+r2.readEntity(String.class));
    }

}
