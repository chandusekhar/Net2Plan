package com.net2plan.oaas;

import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.shc.easyjson.*;
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
    private WebTarget target;
    private final int defaultPort = 8080;
    private String authToken;

    /**
     * Net2Plan OaaS Client constructor
     * @param ipAddress IP Address where Net2Plan OaaS is running
     * @param optionalPort port where OaaS is running (default value: 8080)
     */
    public Net2PlanOaaSClient(String ipAddress, int... optionalPort)
    {
        int port;
        if(optionalPort.length == 0)
            port = defaultPort;
        else if(optionalPort.length == 1)
            port = optionalPort[0];
        else
            throw new Net2PlanException("More than one port is not allowed");

        String baseURL =  "http://"+ipAddress+":"+port+"/Net2Plan-OaaS";
        Client client = ClientBuilder.newBuilder().build().register(MultiPartFeature.class);
        this.target = client.target(baseURL);
        this.authToken = "";
        ClientUtils.configureSecureClient();
    }

    /**
     * Establishes the Database configuration of the admin user
     * @param dbUser admin username
     * @param dbPass admin password
     * @param dbIpPort ip:port where the database is running
     * @return HTTP Response
     */
    public Response establishDatabaseConfiguration(String dbUser, String dbPass, String dbIpPort)
    {
        JSONObject json = new JSONObject();
        json.put("username", new JSONValue(dbUser));
        json.put("password", new JSONValue(dbPass));
        json.put("ipport", new JSONValue(dbIpPort));

        WebTarget this_target = target.path("/database/connection");
        Invocation.Builder inv = this_target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
        Response r = inv.post(Entity.entity(JSON.write(json), MediaType.APPLICATION_JSON_TYPE));
        return r;
    }

    /**
     * Authenticates an user
     * @param user user name
     * @param pass user password
     * @return HTTP Response
     */
    public Response authenticateUser(String user, String pass)
    {
        JSONObject json = new JSONObject();
        json.put("username", new JSONValue(user));
        json.put("password", new JSONValue(pass));

        WebTarget this_target = target.path("/database/authenticate");
        Invocation.Builder inv = this_target.request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
        Response r = inv.post(Entity.entity(JSON.write(json), MediaType.APPLICATION_JSON_TYPE));

        String entity = r.readEntity(String.class);
        System.out.println(entity);
        try {
            JSONObject entityJSON = JSON.parse(entity);
            JSONValue tokenValue = entityJSON.get("token");
            this.authToken = (tokenValue == null) ? "" : tokenValue.getValue();
        } catch (ParseException e)
        {
            return Response.serverError().entity(e.getMessage()).build();
        }

        return r;
    }

    /**
     * Obtains a list of all the available catalogs
     * @return HTTP Response
     */
    public Response getCatalogs()
    {
        WebTarget this_target = target.path("/OaaS/catalogs");
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Obtains the catalog represented by name {name}
     * @param name catalog´s name
     * @return HTTP Response
     */
    public Response getCatalogByName(String name)
    {
        WebTarget this_target = target.path("/OaaS/catalogs/"+name);
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Obtains a list of all the available algorithms
     * @return HTTP Response
     */
    public Response getAlgorithms()
    {
        WebTarget this_target = target.path("/OaaS/algorithms");
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Obtains the algorithm represented by name {name}
     * @param name algorithm's name
     * @return HTTP Response
     */
    public Response getAlgorithmByName(String name)
    {
        WebTarget this_target = target.path("/OaaS/algorithms/"+name);
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Obtains a list of all the available reports
     * @return HTTP Response
     */
    public Response getReports()
    {
        WebTarget this_target = target.path("/OaaS/reports");
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Obtains the report represented by name {name}
     * @param name report's name
     * @return HTTP Response
     */
    public Response getReportByName(String name)
    {
        WebTarget this_target = target.path("/OaaS/reports/"+name);
        Invocation.Builder inv = this_target.request().header("token",authToken).accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();
        return r;
    }

    /**
     * Uploads a catalog (JAR file) including different algorithms and/or reports
     * @param catalogFile catalog (JAR file)
     * @param optionalCategory BRONZE, SILVER, GOLD, ALL
     * @return HTTP Response
     */
    public Response uploadCatalog(File catalogFile, String... optionalCategory)
    {
        String category = (optionalCategory.length == 1) ? optionalCategory[0] : "ALL";
        WebTarget this_target = target.path("/OaaS/catalogs");
        FileDataBodyPart body = new FileDataBodyPart("file",catalogFile);
        MultiPart multi = new MultiPart();
        multi.bodyPart(body);

        Invocation.Builder inv = this_target.request(MediaType.APPLICATION_JSON).header("token",authToken).header("category",category);
        Response r = inv.post(Entity.entity(multi,MediaType.MULTIPART_FORM_DATA));

        return r;
    }

    /**
     * Sends an execution (algorithm or report) request to OaaS API
     * @param type execution type (ALGORITHM, REPORT)
     * @param name algorithm or report name to executeOperation
     * @param userParams Map including names and customized values of the execution parameters
     * @param netPlan input NetPlan
     * @return HTTP Response
     */
    public Response executeOperation(ClientUtils.ExecutionType type, String name, Map<String, String> userParams, NetPlan netPlan)
    {
        WebTarget this_target = target.path("/OaaS/execute");

        JSONObject json = new JSONObject();
        json.put("type",new JSONValue(type.toString()));
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

        Invocation.Builder inv = this_target.request(MediaType.APPLICATION_JSON_TYPE).header("token",authToken).accept(MediaType.APPLICATION_JSON_TYPE);
        Response r = inv.post(Entity.entity(JSON.write(json), MediaType.APPLICATION_JSON_TYPE));

        return r;
    }

    public static void main(String [] args)
    {
        Net2PlanOaaSClient client = new Net2PlanOaaSClient("localhost");

        Response db = client.establishDatabaseConfiguration("girtel","girtelserver","localhost:3306");

        Response auth2 = client.authenticateUser("root", "root");

        File catalog_2 = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\BuiltInExamples.jar");
        Response r2 = client.uploadCatalog(catalog_2);
        System.out.println("UPLOAD CATALOG -> "+r2.readEntity(String.class));

        Response getC2 = client.getCatalogs();
        System.out.println(getC2.readEntity(String.class));

        Response getA2 = client.getAlgorithmByName("Offline_fa_ospfWeightOptimization_GRASP");
        System.out.println(getA2.readEntity(String.class));

        File topologyFile = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\data\\networkTopologies\\example7nodes_ipOverWDM.n2p");
        NetPlan netPlan = new NetPlan(topologyFile);
        /*Map<String, String> params = new LinkedHashMap<>();
        params.put("grasp_initializationType","random");
        params.put("ospf_maxLinkWeight","8");
        params.put("grasp_differenceInWeightToBeNeighbors","3");
        params.put("algorithm_randomSeed","5");
        params.put("algorithm_outputFileNameRoot","");
        params.put("ospf_weightOfMaxUtilizationInObjectiveFunction","0.6");
        params.put("grasp_rclRandomnessFactor","0.6");
        params.put("algorithm_maxExecutionTimeInSeconds","40");
        params.put("grasp_maxNumIterations","70000");*/

        //Response rex = client.executeOperation(ClientUtils.ExecutionType.REPORT,"Report_delay",null, netPlan);
        //System.out.println("EXECUTE -> "+rex.readEntity(String.class));
    }

}
