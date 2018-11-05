package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.utils.Constants;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class RestDemand
{
    private int ingressNodeIndex, egressNodeIndex, layerIndex;
    private double offeredTraffic;
    private String routingType;

    public RestDemand(){}

    public RestDemand(int ingressNodeIndex, int egressNodeIndex, int layerIndex, double offeredTraffic, String routingType)
    {
        this.ingressNodeIndex = ingressNodeIndex;
        this.egressNodeIndex = egressNodeIndex;
        this.layerIndex = layerIndex;
        this.offeredTraffic = offeredTraffic;
        this.routingType = routingType;
    }

    public RestDemand(Demand demand)
    {
        this.ingressNodeIndex = demand.getIngressNode().getIndex();
        this.egressNodeIndex = demand.getEgressNode().getIndex();
        this.layerIndex = demand.getLayer().getIndex();
        this.offeredTraffic = demand.getOfferedTraffic();
        this.routingType = demand.getRoutingType().toString();
    }

    public void setIngressNodeIndex(int ingressNodeIndex)
    {
        this.ingressNodeIndex = ingressNodeIndex;
    }

    public int getIngressNodeIndex()
    {
        return ingressNodeIndex;
    }

    public void setEgressNodeIndex(int egressNodeIndex)
    {
        this.egressNodeIndex = egressNodeIndex;
    }

    public int getEgressNodeIndex()
    {
        return egressNodeIndex;
    }

    public void setLayerIndex(int layerIndex)
    {
        this.layerIndex = layerIndex;
    }

    public int getLayerIndex()
    {
        return layerIndex;
    }

    public void setOfferedTraffic(double offeredTraffic)
    {
        this.offeredTraffic = offeredTraffic;
    }

    public double getOfferedTraffic()
    {
        return offeredTraffic;
    }

    public void setRoutingType(String routingType)
    {
        this.routingType = routingType;
    }

    public String getRoutingType()
    {
        return routingType;
    }
}
