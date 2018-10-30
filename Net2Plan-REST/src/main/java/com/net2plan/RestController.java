package com.net2plan;

import com.net2plan.utils.RestUtils;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "design" path)
 */
@Path("/design")
public class RestController
{
    private NetPlan netPlan = RestUtils.netPlan;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDesign()
    {
        return Response.ok(netPlan.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response AddNode(RestNode node)
    {
        Node n = netPlan.addNode(node.getxCoord(), node.getyCoord(), node.getName(), null);
        if(n == null)
            return Response.serverError().entity("Couldn't add a new node").build();

        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/{index}")
    public Response getNodeByIndex(@PathParam("index") int index)
    {
        System.out.println(index);
        Node n = netPlan.getNode(index);
        System.out.println(n);
        if(n == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(new RestNode(n)).build();
    }



}
