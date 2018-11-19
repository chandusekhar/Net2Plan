package com.net2plan;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.RestDatabase;
import com.net2plan.utils.RestUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.Map;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public NetPlan netPlan = RestDatabase.netPlan;
    public Map<String, List<IAlgorithm>> jar2AlgorithmsMap = RestDatabase.jar2AlgorithmsMap;
    public Map<String, List<IReport>> jar2ReportsMap = RestDatabase.jar2ReportsMap;

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
    public Response uploadJAR(@FormDataParam("file") FormDataContentDisposition fileMetaData)
    {
        String UPLOAD_PATH = "C:/temp/";
        /*  try
        {
            int read = 0;
            byte[] bytes = new byte[1024];

           OutputStream out = new FileOutputStream(new File(UPLOAD_PATH + fileMetaData.getFileName()));

            while ((read = fileInputStream.read(bytes)) != -1)
            {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e)
        {
            throw new WebApplicationException("Error while uploading file. Please try again !!");
        }*/

        return RestUtils.OK(fileMetaData.getFileName());
    }

}
