package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.Link;
import com.net2plan.interfaces.networkDesign.Route;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class RestRoute
{
    private int demandIndex;
    private double carriedTraffic, occupiedLinkCapacity;
    private List<Integer> sequenceOfLinkIndexes;
    private RestMap attributesMap;

    public RestRoute(){}

    public RestRoute(int demandIndex, double carriedTraffic, double occupiedLinkCapacity, List<Integer> sequenceOfLinkIndexes, RestMap attributesMap)
    {
        this.demandIndex = demandIndex;
        this.carriedTraffic = carriedTraffic;
        this.occupiedLinkCapacity = occupiedLinkCapacity;
        this.sequenceOfLinkIndexes = new LinkedList<>();
        this.sequenceOfLinkIndexes.addAll(sequenceOfLinkIndexes);
        this.attributesMap = attributesMap;
    }

    public RestRoute(Route route)
    {
        this.demandIndex = route.getDemand().getIndex();
        this.carriedTraffic = route.getCarriedTraffic();
        this.occupiedLinkCapacity = route.getOccupiedCapacity();
        this.sequenceOfLinkIndexes = new LinkedList<>();

        List<Link> routeLinks = route.getSeqLinks();
        routeLinks.stream().forEach(l -> this.sequenceOfLinkIndexes.add(l.getIndex()));

        this.attributesMap = new RestMap(route.getAttributes());
    }

    public void setDemandIndex(int demandIndex)
    {
        this.demandIndex = demandIndex;
    }

    public int getDemandIndex()
    {
        return demandIndex;
    }

    public void setCarriedTraffic(double carriedTraffic)
    {
        this.carriedTraffic = carriedTraffic;
    }

    public double getCarriedTraffic()
    {
        return carriedTraffic;
    }

    public void setOccupiedLinkCapacity(double occupiedLinkCapacity)
    {
        this.occupiedLinkCapacity = occupiedLinkCapacity;
    }

    public double getOccupiedLinkCapacity()
    {
        return occupiedLinkCapacity;
    }

    public void setSequenceOfLinkIndexes(List<Integer> sequenceOfLinkIndexes)
    {
        this.sequenceOfLinkIndexes = sequenceOfLinkIndexes;
    }

    public List<Integer> getSequenceOfLinkIndexes()
    {
        return sequenceOfLinkIndexes;
    }

    public void setAttributesMap(RestMap attributesMap)
    {
        this.attributesMap = attributesMap;
    }

    public RestMap getAttributesMap()
    {
        return attributesMap;
    }
}
