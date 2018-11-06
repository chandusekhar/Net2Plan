package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.NetworkLayer;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class RestNetworkLayer
{
    private String name, description, linkCapacityUnitsName, demandTrafficUnitsName;
    private RestMap attributesMap;

    public RestNetworkLayer(){}

    public RestNetworkLayer(String name, String description, String linkCapacityUnitsName, String demandTrafficUnitsName, RestMap attributesMap)
    {
        this.name = name;
        this.description = description;
        this.linkCapacityUnitsName = linkCapacityUnitsName;
        this.demandTrafficUnitsName = demandTrafficUnitsName;
        this.attributesMap = attributesMap;
    }

    public RestNetworkLayer(NetworkLayer layer)
    {
        this.name = layer.getName();
        this.description = layer.getDescription();
        this.linkCapacityUnitsName = layer.getLinkCapacityUnits();
        this.demandTrafficUnitsName = layer.getDemandTrafficUnits();
        this.attributesMap = new RestMap(layer.getAttributes());
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setLinkCapacityUnitsName(String linkCapacityUnitsName)
    {
        this.linkCapacityUnitsName = linkCapacityUnitsName;
    }

    public String getLinkCapacityUnitsName()
    {
        return linkCapacityUnitsName;
    }

    public void setDemandTrafficUnitsName(String demandTrafficUnitsName)
    {
        this.demandTrafficUnitsName = demandTrafficUnitsName;
    }

    public String getDemandTrafficUnitsName()
    {
        return demandTrafficUnitsName;
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
