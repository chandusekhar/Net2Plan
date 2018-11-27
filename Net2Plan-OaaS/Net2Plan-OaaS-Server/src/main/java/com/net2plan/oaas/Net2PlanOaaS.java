package com.net2plan.oaas;

import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.IExternal;
import com.net2plan.utils.*;
import com.shc.easyjson.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


/**
 * Net2Plan OaaS root resource (URL: /OaaS)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public File UPLOAD_DIR = ServerUtils.UPLOAD_DIR;
    public List<Triple<String, List<IAlgorithm>, List<IReport>>> catalogAlgorithmsAndReports = ServerUtils.catalogAlgorithmsAndReports;


    /**
     * Obtains the list of available catalogs (URL: /OaaS/catalogs, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/catalogs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogs()
    {
        JSONObject catalogsJSON = new JSONObject();
        JSONArray catalogsArray = new JSONArray();
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            JSONObject catalogJSON = ServerUtils.parseCatalog(catalogEntry);
            catalogsArray.add(new JSONValue(catalogJSON));
        }
        catalogsJSON.put("catalogs", new JSONValue(catalogsArray));

        return ServerUtils.OK(catalogsJSON);
    }

    /**
     * Uploads a new catalog (JAR file) (URL: /OaaS/catalogs, Operation: POST, Consumes: MULTIPART FORM DATA (FORM NAME: file), Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @POST
    @Path("/catalogs")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCatalog(@FormDataParam("file") byte [] input, @FormDataParam("file") FormDataContentDisposition fileMetaData)
    {
        if(!UPLOAD_DIR.exists())
            UPLOAD_DIR.mkdirs();

        String catalogName = fileMetaData.getFileName();
        List<String> catalogsNames = ServerUtils.getCatalogsNames();
        JSONObject json = new JSONObject();

        if(catalogsNames.contains(catalogName))
        {
            json.put("message", new JSONValue("Catalog "+catalogName+" exists"));
            return ServerUtils.SERVER_ERROR(json);
        }


        List<IAlgorithm> algorithms = new LinkedList<>();
        List<IReport> reports = new LinkedList<>();

        File uploadedFile = new File(UPLOAD_DIR + File.separator + fileMetaData.getFileName());
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

            catalogAlgorithmsAndReports.add(Triple.unmodifiableOf(catalogName, algorithms, reports));
            ServerUtils.cleanFolder(UPLOAD_DIR, false);

            return ServerUtils.OK(null);

        } catch (IOException e)
        {
            json.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(json);
        } catch (IllegalAccessException e)
        {
            json.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(json);
        } catch (InstantiationException e)
        {
            json.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(json);
        }
    }

    /**
     * Obtains a catalog by its name (URL: /OaaS/catalogs/{catalogName}, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/catalogs/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCatalogByName(@PathParam("name") String catalogName)
    {
        JSONObject catalogJSON = null;
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            String catalog = catalogEntry.getFirst();
            if(catalog.equals(catalogName))
            {
                catalogJSON = ServerUtils.parseCatalog(catalogEntry);
                break;
            }
        }
        if(catalogJSON == null)
        {
            JSONObject json = new JSONObject();
            json.put("message", new JSONValue("Catalog "+catalogName+" not found"));
            return ServerUtils.NOT_FOUND(json);
        }

        return ServerUtils.OK(catalogJSON);
    }

    /**
     * Obtains the list of available algorithms (URL: /OaaS/algorithms, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/algorithms")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlgorithms()
    {
        JSONObject algorithmsJSON = new JSONObject();
        JSONArray algorithmsArray = new JSONArray();
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            List<IAlgorithm> algs = catalogEntry.getSecond();
            for(IAlgorithm alg : algs)
            {
                JSONObject algorithmJSON = ServerUtils.parseAlgorithm(alg);
                algorithmsArray.add(new JSONValue(algorithmJSON));
            }
        }
        algorithmsJSON.put("algorithms",new JSONValue(algorithmsArray));

        return ServerUtils.OK(algorithmsJSON);
    }

    /**
     * Obtains an algorithm by its name (URL: /OaaS/algorithms/{algorithmName}, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/algorithms/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlgorithmByName(@PathParam("name") String algorithmName)
    {
        JSONObject algorithmJSON = null;
        boolean found = false;
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            List<IAlgorithm> algs = catalogEntry.getSecond();
            for(IAlgorithm alg : algs)
            {
                String algName = alg.getClass().getName();
                if(algName.equals(algorithmName))
                {
                    algorithmJSON = ServerUtils.parseAlgorithm(alg);
                    found = true;
                }
            }
            if(found)
                break;
        }

        if(algorithmJSON == null)
        {
            JSONObject json = new JSONObject();
            json.put("message", new JSONValue("Algorithm "+algorithmName+" not found"));
            return ServerUtils.NOT_FOUND(json);
        }

        return ServerUtils.OK(algorithmJSON);
    }

    /**
     * Obtains the list of available reports (URL: /OaaS/reports, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReports()
    {
        JSONObject reportsJSON = new JSONObject();
        JSONArray reportsArray = new JSONArray();
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            List<IReport> reps = catalogEntry.getThird();
            for(IReport rep : reps)
            {
                JSONObject reportJSON = ServerUtils.parseReport(rep);
                reportsArray.add(new JSONValue(reportJSON));
            }
        }
        reportsJSON.put("reports",new JSONValue(reportsArray));

        return ServerUtils.OK(reportsJSON);
    }

    /**
     * Obtains a report by its name (URL: /OaaS/reports/{reportName}, Operation: GET, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @GET
    @Path("/reports/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportByName(@PathParam("name") String reportName)
    {
        JSONObject reportJSON = null;
        boolean found = false;
        for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
        {
            List<IReport> reps = catalogEntry.getThird();
            for(IReport rep : reps)
            {
                String repName = rep.getClass().getName();
                if(repName.equals(reportName))
                {
                    reportJSON = ServerUtils.parseReport(rep);
                    found = true;
                }
            }
            if(found)
                break;
        }

        if(reportJSON == null)
        {
            JSONObject json = new JSONObject();
            json.put("message", new JSONValue("Report "+reportName+" not found"));
            return ServerUtils.NOT_FOUND(json);
        }

        return ServerUtils.OK(reportJSON);
    }

    /**
     * Sends a request to execute an algorithm or a report
     * @param input input JSON Object. It has to be send using a specific format:
     *              <ul>
     *                  <li type="square">type: ALGORITHM / REPORT</li>
     *                  <li type="square">name: name of the algorithm or report to execute.</li>
     *                  <li type="square">userparams: map defining the user's custom parameter values. (The have to be the same as the defined in the algorithm or report, if not, the execution will fail)</li>
     *                  <li type="square">netPlan: NetPlan design (JSON formatted) in which the algorithm or report will be executed in.
     *                  To create this JSON representation, the method saveToJSON() in NetPlan class will help.</li>
     *              </ul>
     * @return HTTP Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/execute")
    public Response execute(String input)
    {
        JSONObject errorJSON = new JSONObject();
        String response = "";
        JSONObject inputJSON = null;
        try {
            inputJSON = JSON.parse(input);
        } catch (ParseException e)
        {
            errorJSON.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(errorJSON);
        }
        String type = inputJSON.get("type").getValue();
        String executeName = inputJSON.get("name").getValue();
        JSONArray userParams = inputJSON.get("userparams").getValue();
        JSONObject inputNetPlan = inputJSON.get("netPlan").getValue();

        NetPlan netPlan = new NetPlan(inputNetPlan);

        if(type.equalsIgnoreCase("ALGORITHM"))
        {
            boolean found = false;
            IAlgorithm algorithm = null;
            for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
            {
                List<IAlgorithm> algs = catalogEntry.getSecond();
                for(IAlgorithm alg : algs)
                {
                    String algName = alg.getClass().getName();
                    if(algName.equals(executeName))
                    {
                        algorithm = alg;
                        found = true;
                    }
                }
                if(found)
                    break;
            }

            if(algorithm == null)
            {
                errorJSON.put("message", new JSONValue("Algorithm "+executeName+" not found"));
                return ServerUtils.NOT_FOUND(errorJSON);
            }


            List<Triple<String, String, String>> algorithmParameters_raw = algorithm.getParameters();
            Map<String, String> algorithmParameters = new LinkedHashMap<>();
            for(Triple<String, String, String> t : algorithmParameters_raw)
            {
                String paramName = t.getFirst();
                String paramDefaultValue = t.getSecond();
                algorithmParameters.put(paramName, paramDefaultValue);
            }


            Map<String, String> userParametersMap = ServerUtils.parseParametersMap(userParams);
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
                                errorJSON.put("message", new JSONValue("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are: "+possibleValues));
                                return ServerUtils.SERVER_ERROR(errorJSON);
                            }
                        }
                        else if(paramDefaultValue.startsWith("#boolean#"))
                        {
                            if(userParamValue.equals("true") || userParamValue.equals("false"))
                            {
                                algorithmParameters.put(paramName, userParamValue);
                            }
                            else{
                                errorJSON.put("message", new JSONValue("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are true or false"));
                                return ServerUtils.SERVER_ERROR(errorJSON);
                            }
                        }
                        else{
                            algorithmParameters.put(paramName, userParamValue);
                        }
                    }
                    else{
                        errorJSON.put("message", new JSONValue("Undefined parameter "+paramName+" for this algorithm: "+algorithm.getClass().getName()));
                        return ServerUtils.SERVER_ERROR(errorJSON);
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
            }catch(Exception e)
            {
                errorJSON.put("message", new JSONValue(e.getMessage()));
                return ServerUtils.SERVER_ERROR(errorJSON);
            }

        }
        else if(type.equalsIgnoreCase("REPORT"))
        {
            IReport report = null;
            boolean found = false;
            for(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry : catalogAlgorithmsAndReports)
            {
                List<IReport> reps = catalogEntry.getThird();
                for(IReport rep : reps)
                {
                    String repName = rep.getClass().getName();
                    if(repName.equals(executeName))
                    {
                        report = rep;
                        found = true;
                    }
                }
                if(found)
                    break;
            }
            if(report == null)
            {
                errorJSON.put("message", new JSONValue("Report "+executeName+" not found"));
                return ServerUtils.NOT_FOUND(errorJSON);
            }


            List<Triple<String, String, String>> reportParameters_raw = report.getParameters();
            Map<String, String> reportParameters = new LinkedHashMap<>();
            for(Triple<String, String, String> t : reportParameters_raw)
            {
                String paramName = t.getFirst();
                String paramDefaultValue = t.getSecond();
                reportParameters.put(paramName, paramDefaultValue);
            }

            Map<String, String> userParametersMap = ServerUtils.parseParametersMap(userParams);
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
                                errorJSON.put("message", new JSONValue("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are: "+possibleValues));
                                return ServerUtils.SERVER_ERROR(errorJSON);
                            }
                        }
                        else if(paramDefaultValue.startsWith("#boolean#"))
                        {
                            if(userParamValue.equals("true") || userParamValue.equals("false"))
                            {
                                reportParameters.put(paramName, userParamValue);
                            }
                            else{
                                errorJSON.put("message", new JSONValue("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are true or false"));
                                return ServerUtils.SERVER_ERROR(errorJSON);
                            }
                        }
                        else{
                            reportParameters.put(paramName, userParamValue);
                        }
                    }
                    else{
                        errorJSON.put("message", new JSONValue("Undefined parameter "+paramName+" for this report: "+report.getClass().getName()));
                        return ServerUtils.SERVER_ERROR(errorJSON);
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
            }catch(Exception e)
            {
                errorJSON.put("message", new JSONValue(e.getMessage()));
                return ServerUtils.SERVER_ERROR(errorJSON);
            }
        }

        JSONObject responseJSON = new JSONObject();
        responseJSON.put("outputNetPlan", new JSONValue(netPlan.saveToJSON()));
        responseJSON.put("executeResponse", new JSONValue(response));

        return ServerUtils.OK(responseJSON);
    }


}
