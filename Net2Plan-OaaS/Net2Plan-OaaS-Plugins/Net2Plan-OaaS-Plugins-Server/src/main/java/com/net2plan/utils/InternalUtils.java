package com.net2plan.utils;



import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.internal.IExternal;
import com.net2plan.internal.SystemUtils;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class InternalUtils
{

    /**
     * Directory where uploaded files will be stored while they are being analyzed
     */
    public final static File UPLOAD_DIR;
    static { UPLOAD_DIR = new File(SystemUtils.getCurrentDir().getAbsolutePath() + File.separator + "upload"); }


    /**
     * Deletes all files and directories inside a directory
     * @param folder directory to leave empty
     * @param deleteFolder true if folder will be deleted, false if not
     */
    public static void cleanFolder(File folder, boolean deleteFolder)
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

    public static JSONObject parseAlgorithm(IAlgorithm alg)
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

    public static JSONObject parseReport(IReport rep)
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

    public static JSONObject parseCatalog(Map.Entry<String, List<IExternal>> catalogEntry)
    {
        String catalogName = catalogEntry.getKey();
        List<IExternal> catalogExternals = catalogEntry.getValue();
        JSONObject catalogJSON = new JSONObject();
        JSONArray externalsArray = new JSONArray();
        for(IExternal ext : catalogExternals)
        {
            if(ext instanceof IAlgorithm)
            {
                JSONObject algJSON = parseAlgorithm((IAlgorithm)ext);
                externalsArray.add(new JSONValue(algJSON));
            }
            else if(ext instanceof IReport)
            {
                JSONObject repJSON = parseReport((IReport)ext);
                externalsArray.add(new JSONValue(repJSON));
            }
        }
        catalogJSON.put("name", new JSONValue(catalogName));
        catalogJSON.put("files", new JSONValue(externalsArray));
        return catalogJSON;
    }

    public static JSONObject NOT_FOUND_JSON(String message)
    {
        if(message == null)
            message = "";
        JSONObject notfound = new JSONObject();
        notfound.put("message", new JSONValue(message));
        return notfound;
    }

    public static Map<String, String> parseParametersMap(JSONObject paramJSON)
    {
        Map<String, String> params = new LinkedHashMap<>();
        if(paramJSON.size() == 0)
            return null;
        for(Map.Entry<String, JSONValue> entry : paramJSON.entrySet())
        {
            params.put(entry.getKey(), entry.getValue().getValue());
        }

        return params;
    }


}
