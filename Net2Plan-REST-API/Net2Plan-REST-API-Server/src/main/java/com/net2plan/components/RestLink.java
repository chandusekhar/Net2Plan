package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.Link;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestLink
{
    private int originNodeIndex, destinationNodeIndex;
    private double capacity, lengthInKm, propagationSpeedInKmPerSecond;

    public RestLink(){}

    public RestLink(int originNodeIndex, int destinationNodeIndex, double capacity, double lengthInKm, double propagationSpeedInKmPerSecond)
    {
        this.originNodeIndex = originNodeIndex;
        this.destinationNodeIndex = destinationNodeIndex;
        this.capacity = capacity;
        this.lengthInKm = lengthInKm;
        this.propagationSpeedInKmPerSecond = propagationSpeedInKmPerSecond;
    }

    public RestLink(Link link)
    {
        this.originNodeIndex = link.getOriginNode().getIndex();
        this.destinationNodeIndex = link.getDestinationNode().getIndex();
        this.capacity = link.getCapacity();
        this.lengthInKm = link.getLengthInKm();
        this.propagationSpeedInKmPerSecond = link.getPropagationSpeedInKmPerSecond();
    }

    public void setOriginNodeIndex(int originNodeIndex)
    {
        this.originNodeIndex = originNodeIndex;
    }

    public int getOriginNodeIndex()
    {
        return originNodeIndex;
    }

    public void setDestinationNodeIndex(int destinationNodeIndex)
    {
        this.destinationNodeIndex = destinationNodeIndex;
    }

    public int getDestinationNodeIndex()
    {
        return destinationNodeIndex;
    }

    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

    public double getCapacity()
    {
        return capacity;
    }

    public void setLengthInKm(double lengthInKm)
    {
        this.lengthInKm = lengthInKm;
    }

    public double getLengthInKm()
    {
        return lengthInKm;
    }

    public void setPropagationSpeedInKmPerSecond(double propagationSpeedInKmPerSecond)
    {
        this.propagationSpeedInKmPerSecond = propagationSpeedInKmPerSecond;
    }

    public double getPropagationSpeedInKmPerSecond()
    {
        return propagationSpeedInKmPerSecond;
    }
}
