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

    public static Response OK(Object message)
    {
        if(message == null)
            return Response.ok().build();
        else
            return Response.ok(message).build();
    }

    public static Response NOT_FOUND(Constants.NetworkElementType type)
    {
        if(type != null)
            return Response.status(Response.Status.NOT_FOUND).entity(type.toString()+" not found.").build();

        return Response.status(Response.Status.NOT_FOUND).entity("algorithm not found.").build();
    }

    public static Response SERVER_ERROR(Object message)
    {
        if(message == null)
            return Response.serverError().build();
        else
            return Response.serverError().entity(message).build();
    }

}
