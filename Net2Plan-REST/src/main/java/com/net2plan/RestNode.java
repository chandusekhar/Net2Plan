package com.net2plan;

import com.net2plan.interfaces.networkDesign.Node;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RestNode
{
    @XmlElement
    private String name;
    @XmlElement
    private double xCoord;
    @XmlElement
    private double yCoord;

    public RestNode(){}

    public RestNode(String name, double xCoord, double yCoord)
    {
        this.name = name;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    public RestNode(Node node)
    {
        this.name = node.getName();
        this.xCoord = node.getXYPositionMap().getX();
        this.yCoord = node.getXYPositionMap().getY();
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setXCoord(double xCoord)
    {
        this.xCoord = xCoord;
    }

    public double getXCoord()
    {
        return xCoord;
    }

    public void setYCoord(double yCoord)
    {
        this.yCoord = yCoord;
    }

    public double getYCoord()
    {
        return yCoord;
    }
}
