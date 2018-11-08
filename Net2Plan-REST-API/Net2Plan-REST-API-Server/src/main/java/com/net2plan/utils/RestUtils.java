package com.net2plan.utils;

import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkElement;
import com.net2plan.internal.Constants;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

public class RestUtils
{
    public static NetPlan netPlan;

    static
    {
        netPlan = new NetPlan();
    }

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
         * Layer type.
         */
        LAYER("layer"),

        /**
         * Node type.
         */
        NODE("node"),

        /**
         * Link type.
         */
        LINK("link"),

        /**
         * Demand type.
         */
        DEMAND("demand"),

        /**
         * Multicat demand type.
         */
        MULTICAST_DEMAND("multicast demand"),

        /**
         * Route type.
         */
        ROUTE("route"),

        /**
         * Multicast tree type.
         */
        MULTICAST_TREE("multicast tree"),

        /**
         * Forwarding rule type.
         */
        FORWARDING_RULE("forwarding rule"),

        /**
         * Resource type.
         */
        RESOURCE("resource"),

        /**
         * Shared-risk group type.
         */
        SRG("SRG"),

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
