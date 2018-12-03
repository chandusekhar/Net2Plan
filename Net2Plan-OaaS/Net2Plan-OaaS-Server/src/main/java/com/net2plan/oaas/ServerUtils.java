package com.net2plan.oaas;



import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.internal.IExternal;
import com.net2plan.internal.SystemUtils;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class ServerUtils
{
    protected static List<Triple<String, List<IAlgorithm>, List<IReport>>> catalogAlgorithmsAndReports;
    protected static DatabaseController dbController;
    protected static Map<String, Triple<String, Long, String>> tokens;

    static
    {
        catalogAlgorithmsAndReports = new LinkedList<>();
        dbController = null;
        tokens = new LinkedHashMap<>();
    }

    /**
     * Directory where uploaded files will be stored while they are being analyzed
     */
    protected final static File UPLOAD_DIR;
    static { UPLOAD_DIR = new File(SystemUtils.getCurrentDir().getAbsolutePath() + File.separator + "upload"); }

    protected static synchronized String addToken(String user, long id, String category)
    {
        String src = user + id + category + Math.random();
        byte [] srcEncoded = Base64.getEncoder().encode(src.getBytes());
        String token = new String(srcEncoded);
        tokens.put(token, Triple.unmodifiableOf(user, id, category));
        return token;
    }

    protected static synchronized boolean validateToken(String token)
    {
        return tokens.containsKey(token);
    }

    protected static synchronized Triple<String, Long, String> getInfoFromToken(String token)
    {
        return tokens.get(token);
    }
    /**
     * Creates a HTTP response 200, OK including a response JSON
     * @param json JSON Object to return (null if no JSON is desired)
     * @return HTTP response 200, OK
     */
    protected static Response OK(JSONObject json)
    {
        if(json == null || json.size() == 0)
            return Response.ok().build();
        else
            return Response.ok(JSON.write(json)).build();
    }

    /**
     * Creates a HTTP response 403, UNAUTHORIZED
     * @return HTTP response 403, UNAUTHORIZED
     */
    protected static Response UNAUTHORIZED()
    {
        JSONObject json = new JSONObject();
        json.put("message", new JSONValue("Unauthorized"));
        return Response.status(Response.Status.UNAUTHORIZED).entity(JSON.write(json)).build();
    }

    /**
     * Creates a HTTP response 404, NOT FOUND including a response JSON
     * @param json JSON Object to return (null if no JSON is desired)
     * @return HTTP response 404, NOT FOUND
     */
    protected static Response NOT_FOUND(JSONObject json)
    {
        if(json == null || json.size() == 0)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.status(Response.Status.NOT_FOUND).entity(JSON.write(json)).build();
    }

    /**
     * Creates a HTTP response 500, SERVER ERROR including a response JSON
     * @param json JSON Object to return (null if no JSON is desired)
     * @return HTTP response 500, SERVER ERROR
     */
    protected static Response SERVER_ERROR(JSONObject json)
    {
        if(json == null || json.size() == 0)
            return Response.serverError().build();
        else
            return Response.serverError().entity(JSON.write(json)).build();
    }

    /**
     * Deletes all files and directories inside a directory
     * @param folder directory to leave empty
     * @param deleteFolder true if folder will be deleted, false if not
     */
    protected static void cleanFolder(File folder, boolean deleteFolder)
    {
        if(folder.isDirectory())
            return;
        File [] files = folder.listFiles();
        if(files != null)
        {
            for(File f: files)
            {
                if(f.isDirectory())
                {
                    cleanFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if(deleteFolder)
            folder.delete();
    }

    /**
     * Obtains a JSON representation of an algorithm
     * @param alg algorithm to convert to JSON
     * @return JSON representation of the algorithm
     */
    protected static JSONObject parseAlgorithm(IAlgorithm alg)
    {
        JSONObject algorithmJSON = new JSONObject();
        String algName = (alg.getClass().getName() == null) ? "" : alg.getClass().getName();
        String algDescription = (alg.getDescription() == null) ? "" : alg.getDescription().replaceAll("\"","");
        algorithmJSON.put("name", new JSONValue(algName));
        algorithmJSON.put("type", new JSONValue("algorithm"));
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
        return algorithmJSON;
    }

    /**
     * Obtains a JSON representation of a report
     * @param rep report to convert to JSON
     * @return JSON representation of the report
     */
    protected static JSONObject parseReport(IReport rep)
    {
        JSONObject reportJSON = new JSONObject();
        String repName = (rep.getClass().getName() == null) ? "" : rep.getClass().getName();
        String repTitle = (rep.getTitle() == null) ? "" : rep.getTitle();
        String repDescription = (rep.getDescription() == null) ? "" : rep.getDescription().replaceAll("\"","");
        reportJSON.put("name", new JSONValue(repName));
        reportJSON.put("type", new JSONValue("report"));
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
        return reportJSON;
    }

    /**
     * Obtains the JSON representation of a catalog (JAR file)
     * @param catalogEntry Triple object with the name of the catalog, a list of its algorithms and a list of its reports
     * @return JSON representation of the catalog
     */
    protected static JSONObject parseCatalog(Triple<String, List<IAlgorithm>, List<IReport>> catalogEntry)
    {
        String catalogName = catalogEntry.getFirst();
        List<IAlgorithm> algorithms = catalogEntry.getSecond();
        List<IReport> reports = catalogEntry.getThird();
        JSONObject catalogJSON = new JSONObject();
        JSONArray externalsArray = new JSONArray();
        for(IAlgorithm alg : algorithms)
        {
            JSONObject algJSON = parseAlgorithm(alg);
            externalsArray.add(new JSONValue(algJSON));
        }
        for(IReport rep : reports)
        {
            JSONObject repJSON = parseReport(rep);
            externalsArray.add(new JSONValue(repJSON));
        }
        catalogJSON.put("name", new JSONValue(catalogName));
        catalogJSON.put("files", new JSONValue(externalsArray));
        return catalogJSON;
    }

    /**
     * Converts a JSONArray of parameters in a Map<String, String>
     * @param paramJSON parameters in JSON format
     * @return Map<String, String> including the parameters
     */
    protected static Map<String, String> parseParametersMap(JSONArray paramJSON)
    {
        Map<String, String> params = new LinkedHashMap<>();
        if(paramJSON.size() == 0)
            return null;
        for(JSONValue json : paramJSON)
        {
            JSONObject this_json = json.getValue();
            params.put(this_json.get("name").getValue(), this_json.get("value").getValue());
        }

        return params;
    }


    /**
     * Obtains a list with the catalog's names
     * @return List of catalog's names
     */
    protected static List<String> getCatalogsNames()
    {
        List<String> catalogs = new LinkedList<>();
        for(Triple<String, List<IAlgorithm>, List<IReport>> entry : catalogAlgorithmsAndReports)
        {
            catalogs.add(entry.getFirst());
        }

        return catalogs;
    }



}
