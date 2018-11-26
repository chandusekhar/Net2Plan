package com.net2plan;

import javax.ws.rs.core.Response;

public class OaaSUtils
{
    private OaaSUtils(){}

    /**
     * Creates a HTTP response 200, OK with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 200, OK
     */
    public static Response OK(Object message)
    {
        if(message == null)
            return Response.ok().build();
        else
            return Response.ok(message).build();
    }

    /**
     * Creates a HTTP response 404, NOT FOUND with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 404, NOT FOUND
     */
    public static Response NOT_FOUND(Object message)
    {
        if(message != null)
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Creates a HTTP response 500, SERVER ERROR with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 500, SERVER ERROR
     */
    public static Response SERVER_ERROR(Object message)
    {
        if(message == null)
            return Response.serverError().build();
        else
            return Response.serverError().entity(message).build();
    }

    /**
     * Constants for OaaS execution
     */
    public enum ExecutionType
    {
        /**
         * Algorithm execution
         */
        ALGORITHM("ALGORITHM"),

        /**
         * Report execution
         */
        REPORT("REPORT");

        private String type;
        ExecutionType(String type)
        {
            this.type = type;
        }

        @Override
        public String toString()
        {
            return this.type;
        }
    }

}
