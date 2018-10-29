package com.net2plan;

import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import org.glassfish.hk2.api.Immediate;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "design" path)
 */
@Path("design")
public class RestApi
{
    private NetPlan netPlan = NetPlanCreator.netPlan;
    /**
     * Obtains current Net2Plan design
     *
     * @return Current Net2Plan design
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDesign()
    {
        return netPlan.toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String AddNode(RestNode node)
    {
        Node n = netPlan.addNode(node.getXCoord(), node.getYCoord(), node.getName(), null);
        if(n == null)
            return "{\"Error\": \"404\"}";

        return "{\"Success\": \"200\"}";
    }

}
