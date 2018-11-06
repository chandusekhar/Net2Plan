package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.Node;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestNode
{
    private String name;
    private double xCoord;
    private double yCoord;
    private RestMap attributesMap;

    public RestNode(){}

    public RestNode(String name, double xCoord, double yCoord, RestMap attributesMap)
    {
        this.name = name;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.attributesMap = attributesMap;
    }

    public RestNode(Node node)
    {
        this.name = node.getName();
        this.xCoord = node.getXYPositionMap().getX();
        this.yCoord = node.getXYPositionMap().getY();
        this.attributesMap = new RestMap(node.getAttributes());
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setxCoord(double xCoord)
    {
        this.xCoord = xCoord;
    }

    public double getxCoord()
    {
        return xCoord;
    }

    public void setyCoord(double yCoord)
    {
        this.yCoord = yCoord;
    }

    public double getyCoord()
    {
        return yCoord;
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
