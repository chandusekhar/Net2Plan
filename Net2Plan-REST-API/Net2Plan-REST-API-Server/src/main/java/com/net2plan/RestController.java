package com.net2plan;

import com.net2plan.components.RestDemand;
import com.net2plan.components.RestLink;
import com.net2plan.components.RestNetworkLayer;
import com.net2plan.components.RestNode;
import com.net2plan.examples.ExamplesController;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.utils.Constants;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.RestUtils;
import com.net2plan.utils.Triple;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Root resource (exposed at "design" path)
 */
@Path("/design")
public class RestController
{
    private NetPlan netPlan = RestUtils.netPlan;

    // Design methods

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/")
    public Response getDesign()
    {
        return RestUtils.OK(netPlan.toString());
    }

    /*@POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response setDesign(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail)
    {
        String uploadedFileLocation = SystemUtils.getCurrentDir() + File.separator + fileDetail.getName();
        System.out.println(uploadedFileLocation);

        RestUtils.uploadFile(uploadedInputStream, uploadedFileLocation);

        File newDesign = new File(uploadedFileLocation);

        if(newDesign == null || !newDesign.exists())
            return Response.serverError().entity("Couldn't set a new design").build();

        RestUtils.netPlan = new NetPlan(newDesign);

        return Response.ok().build();

    }*/

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/execute/{algorithmname}")
    public Response executeAlgorithm(@PathParam("algorithmname") String algorithmName)
    {
        IAlgorithm algorithm = ExamplesController.getAlgorithm(algorithmName);
        if(algorithm == null)
            return RestUtils.NOT_FOUND;
        String response = algorithm.executeAlgorithm(netPlan, new LinkedHashMap<>(), new LinkedHashMap<>());
        return RestUtils.OK(response);
    }

    // Node Methods

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response getNodes()
    {
        List<Node> nodes = netPlan.getNodes();
        List<RestNode> restNodes = new LinkedList<>();
        nodes.stream().forEach(n -> restNodes.add(new RestNode(n)));
        return RestUtils.OK(restNodes);
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
        return RestUtils.OK(null);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes")
    public Response removeAllNodes()
    {
        netPlan.removeAllNodes();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/index/{index}")
    public Response getNodeByIndex(@PathParam("index") int index)
    {
        Node n = netPlan.getNode(index);
        if(n == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestNode(n));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/index/{index}")
    public Response removeNodeByIndex(@PathParam("index") int index)
    {
        Node n = netPlan.getNode(index);
        if(n == null)
            return RestUtils.NOT_FOUND;
        n.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/id/{id}")
    public Response getNodeFromId(@PathParam("id") long id)
    {
        Node n = netPlan.getNodeFromId(id);
        if(n == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestNode(n));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/id/{id}")
    public Response removeNodeFromId(@PathParam("id") long id)
    {
        Node n = netPlan.getNodeFromId(id);
        if(n == null)
            return RestUtils.NOT_FOUND;
        n.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/name/{name}")
    public Response getNodesByName(@PathParam("name") String name)
    {
        List<Node> nodes = netPlan.getNodeByNameAllNodes(name);
        if(nodes.isEmpty())
            return RestUtils.NOT_FOUND;
        List<RestNode> restNodes = new LinkedList<>();
        nodes.stream().forEach(n -> restNodes.add(new RestNode(n)));
        return RestUtils.OK(restNodes);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/name/{name}")
    public Response removeNodesByName(@PathParam("name") String name)
    {
        List<Node> nodes = netPlan.getNodeByNameAllNodes(name);
        if(nodes.isEmpty())
            return RestUtils.NOT_FOUND;
        List<Node> nodesToRemove = new LinkedList<>(nodes);
        nodesToRemove.stream().forEach(n -> n.remove());
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers")
    public Response getLayers()
    {
        List<NetworkLayer> layers = netPlan.getNetworkLayers();
        List<RestNetworkLayer> restLayers = new LinkedList<>();
        layers.stream().forEach(layer -> restLayers.add(new RestNetworkLayer(layer)));
        return RestUtils.OK(restLayers);
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
        return RestUtils.OK(null);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers")
    public Response removeAllLayers()
    {
        netPlan.removeAllNetworkLayers();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/index/{index}")
    public Response getLayerByIndex(@PathParam("index") int index)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(index);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestNetworkLayer(layer));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/index/{index}")
    public Response removeLayerByIndex(@PathParam("index") int index)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(index);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        netPlan.removeNetworkLayer(layer);
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response getLinks(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        List<Link> links = netPlan.getLinks(layer);
        List<RestLink> restLinks = new LinkedList<>();
        links.stream().forEach(l -> restLinks.add(new RestLink(l)));
        return RestUtils.OK(restLinks);
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
        if(originNode == null || destinationNode == null || layer == null)
            return RestUtils.NOT_FOUND;
        Link l = netPlan.addLink(originNode, destinationNode, link.getCapacity(), link.getLengthInKm(), link.getPropagationSpeedInKmPerSecond(), null, layer);
        if(l == null)
            return Response.serverError().entity("Couldn't add a new link").build();
        return RestUtils.OK(null);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response removeAllLinks(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        netPlan.removeAllLinks(layer);
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links/index/{linkindex}")
    public Response getLinkByIndex(@PathParam("layerindex") int layerIndex, @PathParam("linkindex") int linkIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        Link l = netPlan.getLink(linkIndex, layer);
        if(l == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestLink(l));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links/index/{linkindex}")
    public Response removeLinkByIndex(@PathParam("layerindex") int layerIndex, @PathParam("linkindex") int linkIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        Link l = netPlan.getLink(linkIndex, layer);
        if(l == null)
            return RestUtils.NOT_FOUND;
        l.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/links/id/{linkid}")
    public Response getLinkFromId(@PathParam("linkid") long linkId)
    {
        Link l = netPlan.getLinkFromId(linkId);
        if(l == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestLink(l));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/links/id/{linkid}")
    public Response removeLinkFromId(@PathParam("linkid") long linkId)
    {
        Link l = netPlan.getLinkFromId(linkId);
        if(l == null)
            return RestUtils.NOT_FOUND;
        l.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands")
    public Response getDemands(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        List<Demand> demands = netPlan.getDemands(layer);
        List<RestDemand> restDemands = new LinkedList<>();
        demands.stream().forEach(d -> restDemands.add(new RestDemand(d)));
        return RestUtils.OK(restDemands);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands")
    public Response addDemand(@PathParam("layerindex") int layerIndex, RestDemand demand)
    {
        Node ingressNode = netPlan.getNode(demand.getIngressNodeIndex());
        Node egressNode = netPlan.getNode(demand.getEgressNodeIndex());
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(ingressNode == null || egressNode == null || layer == null)
            return RestUtils.NOT_FOUND;
        Demand d = netPlan.addDemand(ingressNode,egressNode,demand.getOfferedTraffic(), Constants.RoutingType.valueOf(demand.getRoutingType()),null, layer);
        if(d == null)
            return Response.serverError().entity("Couldn't add a new demand").build();
        return RestUtils.OK(null);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands")
    public Response removeAllDemands(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        netPlan.removeAllDemands(layer);
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands/index/{demandindex}")
    public Response getDemandByIndex(@PathParam("layerindex") int layerIndex, @PathParam("demandindex") int demandIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        Demand d = netPlan.getDemand(demandIndex, layer);
        if(d == null)
            return RestUtils.NOT_FOUND;
        return RestUtils.OK(new RestDemand(d));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands/index/{demandindex}")
    public Response removeDemandByIndex(@PathParam("layerindex") int layerIndex, @PathParam("demandindex") int demandIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND;
        Demand d = netPlan.getDemand(demandIndex, layer);
        if(d == null)
            return RestUtils.NOT_FOUND;
        d.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demands/id/{demandid}")
    public Response getDemandFromId(@PathParam("demandid") long demandId)
    {
        Demand d = netPlan.getDemandFromId(demandId);
        if(d == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return RestUtils.OK(new RestDemand(d));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demands/id/{demandid}")
    public Response removeDemandFromId(@PathParam("demandid") long demandId)
    {
        Demand d = netPlan.getDemandFromId(demandId);
        if(d == null)
            return RestUtils.NOT_FOUND;
        d.remove();
        return RestUtils.OK(null);
    }


}
