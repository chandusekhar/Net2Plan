package com.net2plan;

import com.net2plan.components.*;
import com.net2plan.examples.ExamplesController;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.internal.SystemUtils;
import com.net2plan.utils.Constants;
import com.net2plan.utils.RestUtils;
import com.net2plan.utils.StringUtils;
import com.net2plan.utils.Triple;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Root resource (exposed at "design" path)
 */
@Path("/design")
public class Net2PlanAPI
{
    private NetPlan netPlan = RestUtils.netPlan;

    // Design methods

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/")
    public Response getDesign()
    {
        String xmlResponse = netPlan.save();
        return RestUtils.OK(xmlResponse);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response setDesign(RestNetPlanFile file)
    {
        RestUtils.netPlan = new NetPlan(file.getFile());
        return RestUtils.OK(null);
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/algorithm/{algorithmname}")
    public Response executeAlgorithm(@PathParam("algorithmname") String algorithmName, RestMap parametersMap)
    {
        IAlgorithm algorithm = ExamplesController.getAlgorithm(algorithmName);
        if(algorithm == null)
            return RestUtils.NOT_FOUND(null);

        List<Triple<String, String, String>> algorithmParameters_raw = algorithm.getParameters();
        Map<String, String> algorithmParameters = new LinkedHashMap<>();
        for(Triple<String, String, String> t : algorithmParameters_raw)
        {
            String paramName = t.getFirst();
            String paramDefaultValue = t.getSecond();
            algorithmParameters.put(paramName, paramDefaultValue);
        }

        Map<String, String> userParametersMap = (parametersMap == null) ? null : parametersMap.getMap();
        if(userParametersMap != null)
        {
            for(Map.Entry<String, String> entry : userParametersMap.entrySet())
            {
                String paramName = entry.getKey();
                String userParamValue = entry.getValue();
                if(algorithmParameters.containsKey(paramName))
                {
                    String paramDefaultValue = algorithmParameters.get(paramName);
                    if(paramDefaultValue.startsWith("#select#"))
                    {
                        List<String> possibleValues = StringUtils.toList(StringUtils.split(paramDefaultValue.replace("#select# ","")));
                        if(possibleValues.contains(userParamValue))
                        {
                            algorithmParameters.put(paramName, userParamValue);
                        }
                        else{
                            return RestUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are: "+possibleValues);
                        }
                    }
                    else if(paramDefaultValue.startsWith("#boolean#"))
                    {
                        if(userParamValue.equals("true") || userParamValue.equals("false"))
                        {
                            algorithmParameters.put(paramName, userParamValue);
                        }
                        else{
                            return RestUtils.SERVER_ERROR("Parameter "+paramName+ " can't be set as "+userParamValue+". Its possible values are true or false");
                        }
                    }
                    else{
                        algorithmParameters.put(paramName, userParamValue);
                    }
                }
                else{
                    return RestUtils.SERVER_ERROR("Undefined parameter "+paramName+" for this algorithm: "+algorithm.getClass().getName());
                }
            }
        }
        else{
            for(Map.Entry<String, String> entry : algorithmParameters.entrySet())
            {
                String paramName = entry.getKey();
                String paramDefaultValue = entry.getValue();
                String paramValue = "";
                if(paramDefaultValue.startsWith("#select#"))
                {
                    paramValue = StringUtils.split(paramDefaultValue.replace("#select# ",""))[0];
                }
                else if(paramDefaultValue.startsWith("#boolean#"))
                {
                    paramValue = paramDefaultValue.replace("#boolean# ","");
                }
                else{
                    paramValue = paramDefaultValue;
                }

                algorithmParameters.put(paramName, paramValue);
            }
        }

        List<Triple<String, String, String>> net2planParameters_raw = Configuration.getNet2PlanParameters();
        Map<String, String> net2planParameters = new LinkedHashMap<>();
        net2planParameters_raw.stream().forEach(t -> net2planParameters.put(t.getFirst(), t.getSecond()));

        String response = algorithm.executeAlgorithm(netPlan, algorithmParameters, net2planParameters);

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
        Map<String, String> attributes = (node.getAttributesMap() == null) ? null : node.getAttributesMap().getMap();
        Node n = netPlan.addNode(node.getxCoord(), node.getyCoord(), node.getName(), attributes);
        if(n == null)
            return RestUtils.SERVER_ERROR("Couldn't add a new node");
        return RestUtils.OK(new RestNode(n));
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
        return RestUtils.OK(new RestNode(n));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/index/{index}")
    public Response removeNodeByIndex(@PathParam("index") int index)
    {
        Node n = netPlan.getNode(index);
        if(n == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
        return RestUtils.OK(new RestNode(n));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nodes/id/{id}")
    public Response removeNodeFromId(@PathParam("id") long id)
    {
        Node n = netPlan.getNodeFromId(id);
        if(n == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
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
        Map<String, String> attributes = (layer.getAttributesMap() == null) ? null : layer.getAttributesMap().getMap();
        NetworkLayer networkLayer = netPlan.addLayer(layer.getName(), layer.getDescription(), layer.getLinkCapacityUnitsName(), layer.getDemandTrafficUnitsName(),null, attributes);
        if(networkLayer == null)
            return RestUtils.SERVER_ERROR("Couldn't add a new network layer");
        return RestUtils.OK(new RestNetworkLayer(networkLayer));
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        return RestUtils.OK(new RestNetworkLayer(layer));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/index/{index}")
    public Response removeLayerByIndex(@PathParam("index") int index)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(index);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
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
        if(originNode == null || destinationNode == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Map<String, String> attributes = (link.getAttributesMap() == null) ? null : link.getAttributesMap().getMap();
        Link l = netPlan.addLink(originNode, destinationNode, link.getCapacity(), link.getLengthInKm(), link.getPropagationSpeedInKmPerSecond(), attributes, layer);
        if(l == null)
            return RestUtils.SERVER_ERROR("Couldn't add a new link");
        return RestUtils.OK(new RestLink(l));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links")
    public Response removeAllLinks(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Link l = netPlan.getLink(linkIndex, layer);
        if(l == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LINK);
        return RestUtils.OK(new RestLink(l));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/links/index/{linkindex}")
    public Response removeLinkByIndex(@PathParam("layerindex") int layerIndex, @PathParam("linkindex") int linkIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Link l = netPlan.getLink(linkIndex, layer);
        if(l == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LINK);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LINK);
        return RestUtils.OK(new RestLink(l));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/links/id/{linkid}")
    public Response removeLinkFromId(@PathParam("linkid") long linkId)
    {
        Link l = netPlan.getLinkFromId(linkId);
        if(l == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LINK);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
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
        if(ingressNode == null || egressNode == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.NODE);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Map<String, String> attributes = (demand.getAttributesMap() == null) ? null : demand.getAttributesMap().getMap();
        Demand d = netPlan.addDemand(ingressNode,egressNode,demand.getOfferedTraffic(), Constants.RoutingType.valueOf(demand.getRoutingType()),attributes, layer);
        if(d == null)
            return RestUtils.SERVER_ERROR("Couldn't add a new demand");
        return RestUtils.OK(new RestDemand(d));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands")
    public Response removeAllDemands(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Demand d = netPlan.getDemand(demandIndex, layer);
        if(d == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.DEMAND);
        return RestUtils.OK(new RestDemand(d));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/demands/index/{demandindex}")
    public Response removeDemandByIndex(@PathParam("layerindex") int layerIndex, @PathParam("demandindex") int demandIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Demand d = netPlan.getDemand(demandIndex, layer);
        if(d == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.DEMAND);
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
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.DEMAND);
        return RestUtils.OK(new RestDemand(d));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/demands/id/{demandid}")
    public Response removeDemandFromId(@PathParam("demandid") long demandId)
    {
        Demand d = netPlan.getDemandFromId(demandId);
        if(d == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.DEMAND);
        d.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/routes")
    public Response getRoutes(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        List<Route> routes = netPlan.getRoutes(layer);
        List<RestRoute> restRoutes = new LinkedList<>();
        routes.stream().forEach(r -> restRoutes.add(new RestRoute(r)));
        return RestUtils.OK(restRoutes);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/routes")
    public Response addRoute(@PathParam("layerindex") int layerIndex, RestRoute route)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Demand demand = netPlan.getDemand(route.getDemandIndex(), layer);
        if(demand == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.DEMAND);
        List<Integer> linkIndexes = route.getSequenceOfLinkIndexes();
        List<Link> links = new LinkedList<>();
        linkIndexes.stream().forEach(linkIndex -> links.add(netPlan.getLink(linkIndex)));
        Map<String, String> attributes = (route.getAttributesMap() == null) ? null : route.getAttributesMap().getMap();
        Route r = netPlan.addRoute(demand, route.getCarriedTraffic(), route.getOccupiedLinkCapacity(), links, attributes);
        if(r == null)
            return RestUtils.SERVER_ERROR("Couldn't add a new route");
        return RestUtils.OK(new RestRoute(r));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/routes")
    public Response removeAllRoutes(@PathParam("layerindex") int layerIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        netPlan.removeAllRoutes(layer);
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/routes/index/{routeindex}")
    public Response getRouteByIndex(@PathParam("layerindex") int layerIndex, @PathParam("routeindex") int routeIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Route r = netPlan.getRoute(routeIndex, layer);
        if(r == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.ROUTE);
        return RestUtils.OK(new RestRoute(r));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/layers/{layerindex}/routes/index/{routeindex}")
    public Response removeRouteByIndex(@PathParam("layerindex") int layerIndex, @PathParam("routeindex") int routeIndex)
    {
        NetworkLayer layer = netPlan.getNetworkLayer(layerIndex);
        if(layer == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.LAYER);
        Route r = netPlan.getRoute(routeIndex, layer);
        if(r == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.ROUTE);
        r.remove();
        return RestUtils.OK(null);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/routes/id/{routeid}")
    public Response getRouteFromId(@PathParam("routeid") long routeId)
    {
        Route r = netPlan.getRouteFromId(routeId);
        if(r == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.ROUTE);
        return RestUtils.OK(new RestRoute(r));
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/routes/id/{routeid}")
    public Response removeRouteFromId(@PathParam("routeid") long routeId)
    {
        Route r = netPlan.getRouteFromId(routeId);
        if(r == null)
            return RestUtils.NOT_FOUND(com.net2plan.internal.Constants.NetworkElementType.ROUTE);
        r.remove();
        return RestUtils.OK(null);
    }




}
