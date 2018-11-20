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
 * Root resource (exposed at "myresource" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
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
        String UPLOAD_PATH = "C:\\Users\\César\\Desktop\\archivosCopia";
        File uploadDir = new File(UPLOAD_PATH);
        File uploadedFile = new File(UPLOAD_PATH + File.separator + fileMetaData.getFileName());
        System.out.println(uploadedFile.getAbsolutePath());
        try
        {
           OutputStream out = new FileOutputStream(uploadedFile);
           out.write(input);
           out.flush();
           out.close();

           decompressJarFile(uploadedFile);


        } catch (IOException e)
        {
            throw new Net2PlanException(e.getMessage());
        }

        return RestUtils.OK(null);
    }

    private void decompressJarFile(File jarFile)
    {
        String destDir = jarFile.getParentFile().getAbsolutePath();
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> enumJar = jar.entries();
            while(enumJar.hasMoreElements())
            {
                JarEntry file = enumJar.nextElement();
                File f = new File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory())
                {
                    f.mkdir();
                }
            }
            enumJar = jar.entries();
            while(enumJar.hasMoreElements())
            {
                JarEntry file = enumJar.nextElement();
                File f = new File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory())
                {
                   continue;
                }
                InputStream is = jar.getInputStream(file);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0)
                {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
            jar.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
