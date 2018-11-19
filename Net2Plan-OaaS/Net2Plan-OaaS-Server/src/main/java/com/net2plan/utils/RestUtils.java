package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.NetPlan;


import javax.ws.rs.core.Response;


public class RestUtils
{

    /**
     * Creates a HTTP response 200, OK with a specific message
     * @param message message to return
     * @return HTTP response 200, OK
     */
    public static Response OK(Object message)
    {
        if(message == null)
            return Response.ok().build();
        else
            return Response.ok(message).build();
    }

    public static Response NOT_FOUND(NotFoundResponseType type)
    {
        if(type != null)
            return Response.status(Response.Status.NOT_FOUND).entity(type.toString()+" not found.").build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public static Response SERVER_ERROR(Object message)
    {
        if(message == null)
            return Response.serverError().build();
        else
            return Response.serverError().entity(message).build();
    }

    public enum NotFoundResponseType
    {
        /**
         * Algorithm type
         */
        ALGORITHM("algorithm"),

        /**
         * Report type
         */
        REPORT("report");

        private final String label;

        NotFoundResponseType(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

}
