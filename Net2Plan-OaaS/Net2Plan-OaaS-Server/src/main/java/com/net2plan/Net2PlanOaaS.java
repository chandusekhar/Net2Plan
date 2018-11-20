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

           RestUtils.decompressJarFile(uploadedFile);

           // Here, we have to analyze each decompressed folder from JAR and check if there are any available algorithm or report

           RestUtils.cleanFolder(UPLOAD_DIR, false);
           return RestUtils.OK(null);

        } catch (IOException e)
        {
            return RestUtils.SERVER_ERROR(e.getMessage());
        }
    }

}
