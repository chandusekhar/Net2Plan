package com.net2plan;

import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.IExternal;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Net2Plan OaaS (Optimization as a Service) root resource (exposed at "OaaS" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public File UPLOAD_DIR = InternalUtils.UPLOAD_DIR;
    public Map<String, List<IExternal>> catalog2ExternalMap = RestDatabase.catalog2ExternalMap;
    public List<IAlgorithm> algorithms = RestDatabase.algorithms;
    public List<IReport> reports = RestDatabase.reports;


    @GET
    @Path("/catalogs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogs()
    {
        JSONObject catalogsJSON = new JSONObject();
        JSONArray catalogsArray = new JSONArray();
        for(Map.Entry<String, List<IExternal>> catalogEntry : catalog2ExternalMap.entrySet())
        {
            JSONObject catalogJSON = InternalUtils.parseCatalog(catalogEntry);
            catalogsArray.add(new JSONValue(catalogJSON));
        }
        catalogsJSON.put("catalogs", new JSONValue(catalogsArray));

        return OaaSUtils.OK(JSON.write(catalogsJSON));
    }

    /**
     *
     * @param input
     * @param fileMetaData
     * @return
     */
    @POST
    @Path("/catalogs")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCatalog(@FormDataParam("file") byte [] input, @FormDataParam("file") FormDataContentDisposition fileMetaData)
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

            List<IExternal> externalFiles = new LinkedList<>();
            URLClassLoader cl = new URLClassLoader(new URL[]{uploadedFile.toURI().toURL()}, this.getClass().getClassLoader());
            List<Class<IExternal>> classes = ClassLoaderUtils.getClassesFromFile(uploadedFile, IExternal.class, cl);
            for(Class<IExternal> _class : classes)
            {
                IExternal ext = _class.newInstance();
                externalFiles.add(ext);
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

            catalog2ExternalMap.put(fileMetaData.getName(), externalFiles);
            InternalUtils.cleanFolder(UPLOAD_DIR, false);

            return OaaSUtils.OK(null);

        } catch (IOException e)
        {
            return OaaSUtils.SERVER_ERROR(e.getMessage());
        } catch (IllegalAccessException e)
        {
            return OaaSUtils.SERVER_ERROR(e.getMessage());
        } catch (InstantiationException e)
        {
            return OaaSUtils.SERVER_ERROR(e.getMessage());
        }
    }

    @GET
    @Path("/catalogs/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogByName(@PathParam("name") String catalogName)
    {
        JSONObject catalogJSON = null;
        for(Map.Entry<String, List<IExternal>> catalogEntry : catalog2ExternalMap.entrySet())
        {
            String catalog = catalogEntry.getKey();
            if(catalog.equals(catalogName))
            {
                catalogJSON = InternalUtils.parseCatalog(catalogEntry);
                break;
            }
        }
        if(catalogJSON == null)
        {
            return OaaSUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Catalog "+catalogName+" not found")));
        }

        return OaaSUtils.OK(JSON.write(catalogJSON));
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
            JSONObject algorithmJSON = InternalUtils.parseAlgorithm(alg);
            algorithmsArray.add(new JSONValue(algorithmJSON));
        }
        algorithmsJSON.put("algorithms",new JSONValue(algorithmsArray));

        return OaaSUtils.OK(JSON.write(algorithmsJSON));
    }

    @GET
    @Path("/algorithms/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlgorithmByName(@PathParam("name") String algorithmName)
    {
        JSONObject algorithmJSON = null;
        for(IAlgorithm alg : algorithms)
        {
            String algName = alg.getClass().getName();
            if(algName.equals(algorithmName))
            {
                algorithmJSON = InternalUtils.parseAlgorithm(alg);
                break;
            }
        }

        if(algorithmJSON == null)
        {
            return OaaSUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Algorithm "+algorithmName+" not found")));
        }

        return OaaSUtils.OK(JSON.write(algorithmJSON));
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
            JSONObject reportJSON = InternalUtils.parseReport(rep);
            reportsArray.add(new JSONValue(reportJSON));
        }
        reportsJSON.put("reports",new JSONValue(reportsArray));

        return OaaSUtils.OK(JSON.write(reportsJSON));
    }

    @GET
    @Path("/reports/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportByName(@PathParam("name") String reportName)
    {
        JSONObject reportJSON = null;
        for(IReport rep : reports)
        {
            String repName = rep.getClass().getName();
            if(repName.equals(reportName))
            {
                reportJSON = InternalUtils.parseReport(rep);
                break;
            }
        }

        if(reportJSON == null)
        {
            return OaaSUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Report "+reportName+" not found")));
        }

        return OaaSUtils.OK(JSON.write(reportJSON));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/execute")
    public Response execute(JSONObject inputJSON)
    {
        String response = "";
        String type = inputJSON.get("type").getValue();
        String executeName = inputJSON.get("name").getValue();
        JSONObject userParams = inputJSON.get("userparams").getValue();
        JSONObject inputNetPlan = inputJSON.get("netPlan").getValue();

        NetPlan netPlan = new NetPlan(inputNetPlan);

        if(type.equalsIgnoreCase("ALGORITHM"))
        {
            IAlgorithm algorithm = null;
            for(IAlgorithm alg : algorithms)
            {
                if(executeName.equals(alg.getClass().getName()))
                {
                    algorithm = alg;
                    break;
                }
            }
            if(algorithm == null)
                return OaaSUtils.NOT_FOUND("Algorithm "+executeName+" not found");

            List<Triple<String, String, String>> algorithmParameters_raw = algorithm.getParameters();
            Map<String, String> algorithmParameters = new LinkedHashMap<>();
            for(Triple<String, String, String> t : algorithmParameters_raw)
            {
                String paramName = t.getFirst();
                String paramDefaultValue = t.getSecond();
                algorithmParameters.put(paramName, paramDefaultValue);
            }


            Map<String, String> userParametersMap = InternalUtils.parseParametersMap(userParams);
            if(userParametersMap != null)
            {
                for(Map.Entry<String, String> entry : userParametersMap.entrySet())
                {
                    String paramName = entry.getKey();
                    String userParamValue = entry.getValue();
                    if(algorithmParameters.containsKey(paramName))
                    {
                        String paramDefaultValue = algorithmParameters.get(paramName);
                        if(paramDefaultValue.startsWith("#select#"))
                        {
                            List<String> possibleValues = StringUtils.toList(StringUtils.split(paramDefaultValue.replace("#select# ","")));
                            if(possibleValues.contains(userParamValue))
                            {
                                algorithmParameters.put(paramName, userParamValue);
                            }
                            else{
                                return OaaSUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are: "+possibleValues);
                            }
                        }
                        else if(paramDefaultValue.startsWith("#boolean#"))
                        {
                            if(userParamValue.equals("true") || userParamValue.equals("false"))
                            {
                                algorithmParameters.put(paramName, userParamValue);
                            }
                            else{
                                return OaaSUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are true or false");
                            }
                        }
                        else{
                            algorithmParameters.put(paramName, userParamValue);
                        }
                    }
                    else{
                        return OaaSUtils.SERVER_ERROR("Undefined parameter "+paramName+" for this algorithm: "+algorithm.getClass().getName());
                    }
                }
            }
            else{
                for(Map.Entry<String, String> entry : algorithmParameters.entrySet())
                {
                    String paramName = entry.getKey();
                    String paramDefaultValue = entry.getValue();
                    String paramValue = "";
                    if(paramDefaultValue.startsWith("#select#"))
                    {
                        paramValue = StringUtils.split(paramDefaultValue.replace("#select# ",""))[0];
                    }
                    else if(paramDefaultValue.startsWith("#boolean#"))
                    {
                        paramValue = paramDefaultValue.replace("#boolean# ","");
                    }
                    else{
                        paramValue = paramDefaultValue;
                    }

                    algorithmParameters.put(paramName, paramValue);
                }
            }

            List<Triple<String, String, String>> net2planParameters_raw = Configuration.getNet2PlanParameters();
            Map<String, String> net2planParameters = new LinkedHashMap<>();
            net2planParameters_raw.stream().forEach(t -> net2planParameters.put(t.getFirst(), t.getSecond()));

            try{
                response = algorithm.executeAlgorithm(netPlan, algorithmParameters, net2planParameters);
            }
            catch (Exception e)
            {
                return OaaSUtils.SERVER_ERROR(e.getMessage());
            }

        }
        else if(type.equalsIgnoreCase("REPORT"))
        {
            IReport report = null;
            for(IReport rep : reports)
            {
                if(executeName.equals(rep.getClass().getName()))
                {
                    report = rep;
                    break;
                }
            }
            if(report == null)
                return OaaSUtils.NOT_FOUND("Report "+executeName+" not found");

            List<Triple<String, String, String>> reportParameters_raw = report.getParameters();
            Map<String, String> reportParameters = new LinkedHashMap<>();
            for(Triple<String, String, String> t : reportParameters_raw)
            {
                String paramName = t.getFirst();
                String paramDefaultValue = t.getSecond();
                reportParameters.put(paramName, paramDefaultValue);
            }

            Map<String, String> userParametersMap = InternalUtils.parseParametersMap(userParams);
            if(userParametersMap != null)
            {
                for(Map.Entry<String, String> entry : userParametersMap.entrySet())
                {
                    String paramName = entry.getKey();
                    String userParamValue = entry.getValue();
                    if(reportParameters.containsKey(paramName))
                    {
                        String paramDefaultValue = reportParameters.get(paramName);
                        if(paramDefaultValue.startsWith("#select#"))
                        {
                            List<String> possibleValues = StringUtils.toList(StringUtils.split(paramDefaultValue.replace("#select# ","")));
                            if(possibleValues.contains(userParamValue))
                            {
                                reportParameters.put(paramName, userParamValue);
                            }
                            else{
                                return OaaSUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are: "+possibleValues);
                            }
                        }
                        else if(paramDefaultValue.startsWith("#boolean#"))
                        {
                            if(userParamValue.equals("true") || userParamValue.equals("false"))
                            {
                                reportParameters.put(paramName, userParamValue);
                            }
                            else{
                                return OaaSUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are true or false");
                            }
                        }
                        else{
                            reportParameters.put(paramName, userParamValue);
                        }
                    }
                    else{
                        return OaaSUtils.SERVER_ERROR("Undefined parameter "+paramName+" for this report: "+report.getClass().getName());
                    }
                }
            }
            else{
                for(Map.Entry<String, String> entry : reportParameters.entrySet())
                {
                    String paramName = entry.getKey();
                    String paramDefaultValue = entry.getValue();
                    String paramValue = "";
                    if(paramDefaultValue.startsWith("#select#"))
                    {
                        paramValue = StringUtils.split(paramDefaultValue.replace("#select# ",""))[0];
                    }
                    else if(paramDefaultValue.startsWith("#boolean#"))
                    {
                        paramValue = paramDefaultValue.replace("#boolean# ","");
                    }
                    else{
                        paramValue = paramDefaultValue;
                    }

                    reportParameters.put(paramName, paramValue);
                }
            }

            List<Triple<String, String, String>> net2planParameters_raw = Configuration.getNet2PlanParameters();
            Map<String, String> net2planParameters = new LinkedHashMap<>();
            net2planParameters_raw.stream().forEach(t -> net2planParameters.put(t.getFirst(), t.getSecond()));

            try{
                response = report.executeReport(netPlan, reportParameters, net2planParameters);
            }
            catch (Exception e)
            {
                return OaaSUtils.SERVER_ERROR(e.getMessage());
            }

        }

        JSONObject responseJSON = new JSONObject();
        responseJSON.put("outputNetPlan", new JSONValue(netPlan.saveToJSON()));
        responseJSON.put("executeResponse", new JSONValue(response));

        return OaaSUtils.OK(JSON.write(responseJSON));
    }


}
