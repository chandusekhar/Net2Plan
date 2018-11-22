package com.net2plan;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.IExternal;
import com.net2plan.internal.SystemUtils;
import com.net2plan.utils.ClassLoaderUtils;
import com.net2plan.utils.RestDatabase;
import com.net2plan.utils.RestUtils;
import com.net2plan.utils.Triple;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;



/**
 * Root resource (exposed at "OaaS" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public File UPLOAD_DIR = RestUtils.UPLOAD_DIR;
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
            String algName = (alg.getClass().getName() == null) ? "" : alg.getClass().getName();
            String algDescription = (alg.getDescription() == null) ? "" : alg.getDescription().replaceAll("\"","");
            algorithmJSON.put("name", new JSONValue(algName));
            algorithmJSON.put("description", new JSONValue(algDescription));
            JSONArray parametersArray = new JSONArray();
            if(alg.getParameters() != null)
            {
                for(Triple<String, String, String> param : alg.getParameters())
                {
                    JSONObject parameter = new JSONObject();
                    String paramName = (param.getFirst() == null) ? "" : param.getFirst();
                    String paramDefaultValue = (param.getSecond() == null) ? "" : param.getSecond();
                    String paramDescription = (param.getThird() == null) ? "" : param.getThird().replaceAll("\"","");
                    parameter.put("name", new JSONValue(paramName));
                    parameter.put("defaultValue", new JSONValue(paramDefaultValue));
                    parameter.put("description", new JSONValue(paramDescription));
                    parametersArray.add(new JSONValue(parameter));
                }
            }
            algorithmJSON.put("parameters", new JSONValue(parametersArray));
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
            String repName = (rep.getClass().getName() == null) ? "" : rep.getClass().getName();
            String repTitle = (rep.getTitle() == null) ? "" : rep.getTitle();
            String repDescription = (rep.getDescription() == null) ? "" : rep.getDescription().replaceAll("\"","");
            reportJSON.put("name", new JSONValue(repName));
            reportJSON.put("title", new JSONValue(repTitle));
            reportJSON.put("description", new JSONValue(repDescription));
            JSONArray parametersArray = new JSONArray();
            if(rep.getParameters() != null)
            {
                for(Triple<String, String, String> param : rep.getParameters())
                {
                    JSONObject parameter = new JSONObject();
                    String paramName = (param.getFirst() == null) ? "" : param.getFirst();
                    String paramDefaultValue = (param.getSecond() == null) ? "" : param.getSecond();
                    String paramDescription = (param.getThird() == null) ? "" : param.getThird().replaceAll("\"","");
                    parameter.put("name", new JSONValue(paramName));
                    parameter.put("defaultValue", new JSONValue(paramDefaultValue));
                    parameter.put("description", new JSONValue(paramDescription));
                    parametersArray.add(new JSONValue(parameter));
                }
            }
            reportJSON.put("parameters", new JSONValue(parametersArray));
            reportsArray.add(new JSONValue(reportJSON));
        }
        reportsJSON.put("reports",new JSONValue(reportsArray));

        return RestUtils.OK(JSON.write(reportsJSON));
    }

    @POST
    @Path("/JAR")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadJAR(@FormDataParam("file") byte [] input, @FormDataParam("file") FormDataContentDisposition fileMetaData)
    {

        File uploadedFile = new File(UPLOAD_DIR + File.separator + fileMetaData.getFileName());
        if(!UPLOAD_DIR.exists())
            UPLOAD_DIR.mkdirs();
        try
        {
           OutputStream out = new FileOutputStream(uploadedFile);
           out.write(input);
           out.flush();
           out.close();

            URLClassLoader cl = new URLClassLoader(new URL[]{uploadedFile.toURI().toURL()}, this.getClass().getClassLoader());
            List<Class<IExternal>> classes = ClassLoaderUtils.getClassesFromFile(uploadedFile, IExternal.class, cl);
            for(Class<IExternal> _class : classes)
            {
                IExternal ext = _class.newInstance();
                if(ext instanceof IAlgorithm)
                {
                    IAlgorithm alg = (IAlgorithm)ext;
                    algorithms.add(alg);
                }
                else if(ext instanceof IReport)
                {
                    IReport rep = (IReport)ext;
                    reports.add(rep);
                }

            }

           RestUtils.cleanFolder(UPLOAD_DIR, false);
           return RestUtils.OK(null);

        } catch (IOException e)
        {
            return RestUtils.SERVER_ERROR(e.getMessage());
        } catch (IllegalAccessException e)
        {
            return RestUtils.SERVER_ERROR(e.getMessage());
        } catch (InstantiationException e)
        {
            return RestUtils.SERVER_ERROR(e.getMessage());
        }
    }

}
