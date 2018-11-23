package com.net2plan;


import com.net2plan.interfaces.networkDesign.NetPlan;
import com.shc.easyjson.*;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

public class PruebaCliente
{
    public static void main(String [] args) throws ParseException {
        /*Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget target = client.target("http://localhost:8080/net2plan-oaas-server-0.7.0-SNAPSHOT/OaaS/").path("catalogs");

        File f = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\BuiltInExamples.jar");
        FileDataBodyPart body = new FileDataBodyPart("file",f);
        MultiPart multi = new MultiPart();
        multi.bodyPart(body);

        Invocation.Builder inv = target.request(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON);
        Response r = inv.post(Entity.entity(multi,MediaType.MULTIPART_FORM_DATA));*/

        Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget target = client.target("http://localhost:8080/net2plan-oaas-server-0.7.0-SNAPSHOT/OaaS/").path("execute");

        JSONObject json = new JSONObject();
        json.put("type",new JSONValue("ALGORITHM"));
        json.put("name",new JSONValue("com.net2plan.examples.ocnbook.offline.Offline_fa_ospfWeightOptimization_EA"));
        json.put("userParams", new JSONValue(new JSONArray()));

        File topologyFile = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\data\\networkTopologies\\example7nodes.n2p");
        NetPlan netPlan = new NetPlan(topologyFile);
        JSONObject netPlanJSON = netPlan.saveToJSON();

        json.put("netPlan", new JSONValue(netPlanJSON));

        String json_string = JSON.write(json);
        System.out.println(json_string);

        Invocation.Builder inv = target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
        Response r = inv.post(Entity.entity(json_string, MediaType.APPLICATION_JSON_TYPE));

        System.out.println(r.getStatus());
        System.out.println(r.getStatusInfo());
        System.out.println(r.readEntity(String.class));
    }
}
