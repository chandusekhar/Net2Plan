package com.net2plan;

import com.net2plan.components.RestLink;
import com.net2plan.components.RestNetworkLayer;
import com.net2plan.components.RestNode;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.utils.RestUtils;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

/**
 * Root resource (exposed at "design" path)
 */
@Path("/design")
public class RestController
{
    private NetPlan netPlan = RestUtils.netPlan;


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response getDesign()
    {
        return Response.ok(netPlan.toString()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response getNodes()
    {
        List<Node> nodes = netPlan.getNodes();
        List<RestNode> restNodes = new LinkedList<>();
        nodes.stream().forEach(n -> restNodes.add(new RestNode(n)));
        return Response.ok(restNodes).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response addNode(RestNode node)
    {
        Node n = netPlan.addNode(node.getxCoord(), node.getyCoord(), node.getName(), null);
        if(n == null)
            return Response.serverError().entity("Couldn't add a new node").build();

        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response removeAllNodes()
    {
        netPlan.removeAllNodes();
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/index/{index}")
    public Response getNodeByIndex(@PathParam("index") int index)
    {
        Node n = netPlan.getNode(index);
        if(n == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(new RestNode(n)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/id/{id}")
    public Response getNodeFromId(@PathParam("id") long id)
    {
        Node n = netPlan.getNodeFromId(id);
        if(n == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(new RestNode(n)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/name/{name}")
    public Response getNodesByName(@PathParam("name") String name)
    {
        List<Node> nodes = netPlan.getNodeByNameAllNodes(name);
        List<RestNode> restNodes = new LinkedList<>();
        nodes.stream().forEach(n -> restNodes.add(new RestNode(n)));
        if(restNodes.isEmpty())
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(restNodes).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers")
    public Response getLayers()
    {
        List<NetworkLayer> layers = netPlan.getNetworkLayers();
        List<RestNetworkLayer> restLayers = new LinkedList<>();
        layers.stream().forEach(layer -> restLayers.add(new RestNetworkLayer(layer)));
        return Response.ok(restLayers).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers")
    public Response addLayer(RestNetworkLayer layer)
    {
        NetworkLayer networkLayer = netPlan.addLayer(layer.getName(), layer.getDescription(), layer.getLinkCapacityUnitsName(), layer.getDemandTrafficUnitsName(),null, null);
        if(networkLayer == null)
            return Response.serverError().entity("Couldn't add a new network layer").build();

        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers")
    public Response removeAllLayers()
    {
        netPlan.removeAllNetworkLayers();
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response getLinks(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return Response.serverError().entity("Selected network layer doesn't exist").build();

        List<Link> links = netPlan.getLinks(layer);
        List<RestLink> restLinks = new LinkedList<>();
        links.stream().forEach(l -> restLinks.add(new RestLink(l)));
        return Response.ok(restLinks).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response addLink(@PathParam("layerindex") int layerIndex, RestLink link)
    {
        Node originNode = netPlan.getNode(link.getOriginNodeIndex());
        Node destinationNode = netPlan.getNode(link.getDestinationNodeIndex());
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);

        if(originNode == null)
            return Response.serverError().entity("Selected origin node doesn't exist").build();

        if(destinationNode == null)
            return Response.serverError().entity("Selected destination node doesn't exist").build();

        if(layer == null)
            return Response.serverError().entity("Selected network layer doesn't exist").build();

        Link l = netPlan.addLink(originNode, destinationNode, link.getCapacity(), link.getLengthInKm(), link.getPropagationSpeedInKmPerSecond(), null, layer);

        if(l == null)
            return Response.serverError().entity("Couldn't add a new link").build();

        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response removeAllLinks(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return Response.serverError().entity("Selected network layer doesn't exist").build();

        netPlan.removeAllLinks(layer);
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links/index/{linkindex}")
    public Response getLinkByIndex(@PathParam("layerindex") int layerIndex, @PathParam("linkindex") int linkIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return Response.serverError().entity("Selected network layer doesn't exist").build();
        Link l = netPlan.getLink(linkIndex, layer);
        if(l == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(new RestLink(l)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links/id/{linkid}")
    public Response getLinkFromId(@PathParam("layerindex") int layerIndex, @PathParam("linkid") long linkId)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return Response.serverError().entity("Selected network layer doesn't exist").build();

        Link l = netPlan.getLinkFromId(linkId);
        if(l == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(new RestLink(l)).build();
    }


}
