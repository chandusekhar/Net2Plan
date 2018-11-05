package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.internal.Constants;

import javax.ws.rs.core.Response;
import java.io.*;

public class RestUtils
{
    public static NetPlan netPlan;

    static
    {
        netPlan = new NetPlan();
    }

    public static Response OK(Object answer)
    {
        if(answer == null)
            return Response.ok().build();
        else
            return Response.ok(answer).build();
    }

    public static Response NOT_FOUND(Constants.NetworkElementType type)
    {
        if(type != null)
            return Response.status(Response.Status.NOT_FOUND).entity(type.toString()+" not found.").build();

        return Response.status(Response.Status.NOT_FOUND).entity("algorithm not found.").build();
    }

    public static void uploadFile(InputStream uploadedInputStream, String uploadedFileLocation)
    {

        try {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = uploadedInputStream.read(bytes)) != -1)
            {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }


}
