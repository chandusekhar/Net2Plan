package com.net2plan;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.IExternal;
import com.net2plan.utils.ClassLoaderUtils;
import com.net2plan.utils.RestDatabase;
import com.net2plan.utils.InternalUtils;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Root resource (exposed at "OaaS" path)
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

        return InternalUtils.OK(JSON.write(catalogsJSON));
    }

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

            return InternalUtils.OK(null);

        } catch (IOException e)
        {
            return InternalUtils.SERVER_ERROR(e.getMessage());
        } catch (IllegalAccessException e)
        {
            return InternalUtils.SERVER_ERROR(e.getMessage());
        } catch (InstantiationException e)
        {
            return InternalUtils.SERVER_ERROR(e.getMessage());
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
            return InternalUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Catalog "+catalogName+" not found")));
        }

        return InternalUtils.OK(JSON.write(catalogJSON));
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

        return InternalUtils.OK(JSON.write(algorithmsJSON));
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
            return InternalUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Algorithm "+algorithmName+" not found")));
        }

        return InternalUtils.OK(JSON.write(algorithmJSON));
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

        return InternalUtils.OK(JSON.write(reportsJSON));
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
            return InternalUtils.NOT_FOUND(JSON.write(InternalUtils.NOT_FOUND_JSON("Report "+reportName+" not found")));
        }

        return InternalUtils.OK(JSON.write(reportJSON));
    }



}
