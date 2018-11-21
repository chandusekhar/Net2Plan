package com.net2plan;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.IExternal;
import com.net2plan.internal.SystemUtils;
import com.net2plan.utils.*;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Root resource (exposed at "OaaS" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public File JAR_FOLDER = new File("C:\\Users\\César\\Desktop\\JARS");
    public NetPlan netPlan = RestDatabase.netPlan;
    public List<IAlgorithm> algorithms = RestDatabase.algorithmsList;
    public List<IReport> reports = RestDatabase.reportsList;

    @GET
    @Path("/design")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDesign()
    {
        return RestUtils.OK(netPlan.saveToJSON());
    }

    @GET
    @Path("/algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlgorithms()
    {
        JSONObject algorithmsJSON = new JSONObject();
        JSONArray algorithmsArray = new JSONArray();
        for(IAlgorithm alg : algorithms)
        {
            JSONObject algorithmJSON = new JSONObject();
            algorithmJSON.put("name", new JSONValue(alg.getClass().getName()));
            JSONArray parametersArray = new JSONArray();
            for(Triple<String, String, String> param : alg.getParameters())
            {
                JSONObject parameter = new JSONObject();
                parameter.put("name", new JSONValue(param.getFirst()));
                parameter.put("defaultValue", new JSONValue(param.getSecond()));
                parameter.put("description", new JSONValue(param.getThird()));
                parametersArray.add(new JSONValue(parameter));
            }
            algorithmJSON.put("parameters", new JSONValue(parametersArray));
            algorithmJSON.put("description", new JSONValue(alg.getDescription()));
            algorithmsArray.add(new JSONValue(algorithmJSON));
        }
        algorithmsJSON.put("algorithms",new JSONValue(algorithmsArray));

        return RestUtils.OK(JSON.write(algorithmsJSON));
    }

    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReports()
    {
        JSONObject reportsJSON = new JSONObject();
        JSONArray reportsArray = new JSONArray();
        for(IReport rep : reports)
        {
            JSONObject reportJSON = new JSONObject();
            reportJSON.put("name", new JSONValue(rep.getClass().getName()));
            JSONArray parametersArray = new JSONArray();
            for(Triple<String, String, String> param : rep.getParameters())
            {
                JSONObject parameter = new JSONObject();
                parameter.put("name", new JSONValue(param.getFirst()));
                parameter.put("defaultValue", new JSONValue(param.getSecond()));
                parameter.put("description", new JSONValue(param.getThird()));
                parametersArray.add(new JSONValue(parameter));
            }
            reportJSON.put("parameters", new JSONValue(parametersArray));
            reportJSON.put("description", new JSONValue(rep.getDescription()));
            reportsArray.add(new JSONValue(reportJSON));
        }
        reportsJSON.put("reports",new JSONValue(reportsArray));

        return RestUtils.OK(JSON.write(reportsJSON));
    }

    @GET
    @Path("/JAR")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateJARFromFolder()
    {
        if(!JAR_FOLDER.exists())
            JAR_FOLDER.mkdirs();

            File [] jars = JAR_FOLDER.listFiles();
            for(File jar : jars)
            {
                if(jar.getName().endsWith(".jar"))
                {
                    System.out.println(jar.canRead());
                    System.out.println(jar.canWrite());
                    System.out.println(jar.canExecute());
                    System.out.println(jar.getAbsolutePath());
                    List<Class<IAlgorithm>> cl = ClassLoaderUtils.getClassesFromFile(jar, IAlgorithm.class, null);
                    cl.stream().forEach(_class -> System.out.println(_class.getName()));
                }
            }


           return RestUtils.OK(null);

    }

}
