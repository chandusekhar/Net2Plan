package com.net2plan.components;

import com.net2plan.interfaces.networkDesign.Node;
import com.net2plan.interfaces.networkDesign.Resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class RestResource
{
    private String type, name, capacityMeasurementUnits;
    private int hostNodeIndex;
    private double capacity, processingTimeToTraversingTrafficInMs;
    private RestMap<Integer, Double> capacityIOccupyInBaseResource;
    private RestMap<String, String> attributes;

    public RestResource(){}

    public RestResource(String type, String name, int hostNodeIndex, double capacity, String capacityMeasurementUnits,
                        RestMap<Integer, Double> capacityIOccupyInBaseResource, double processingTimeToTraversingTrafficInMs, RestMap<String, String> attributes)
    {
        this.type = type;
        this.name = name;
        this.hostNodeIndex = hostNodeIndex;
        this.capacity = capacity;
        this.capacityMeasurementUnits = capacityMeasurementUnits;
        this.capacityIOccupyInBaseResource = capacityIOccupyInBaseResource;
        this.processingTimeToTraversingTrafficInMs = processingTimeToTraversingTrafficInMs;
        this.attributes = attributes;
    }

    public RestResource(Resource res)
    {
        this.type = res.getType();
        this.name = res.getName();
        this.hostNodeIndex = (res.getHostNode().isPresent()) ? res.getHostNode().get().getIndex() : -1;
        this.capacity = res.getCapacity();
        this.capacityMeasurementUnits = res.getCapacityMeasurementUnits();

        Map<Resource, Double> capacityBaseResources = res.getCapacityOccupiedInBaseResourcesMap();
        Map<Integer, Double> capacityBaseResourceIndexes = new LinkedHashMap<>();
        for(Map.Entry<Resource, Double> entry : capacityBaseResources.entrySet())
        {
            capacityBaseResourceIndexes.put(entry.getKey().getIndex(), entry.getValue());
        }
        this.capacityIOccupyInBaseResource = new RestMap<>(capacityBaseResourceIndexes);
        this.attributes = new RestMap<>(res.getAttributes());
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setHostNodeIndex(int hostNodeIndex)
    {
        this.hostNodeIndex = hostNodeIndex;
    }

    public int getHostNodeIndex()
    {
        return hostNodeIndex;
    }

    public String getCapacityMeasurementUnits()
    {
        return capacityMeasurementUnits;
    }
}
