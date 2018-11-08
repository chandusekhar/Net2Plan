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
    private RestMap<String, String> attributesMap;

    public RestResource(){}

    public RestResource(String type, String name, int hostNodeIndex, double capacity, String capacityMeasurementUnits,
                        RestMap<Integer, Double> capacityIOccupyInBaseResource, double processingTimeToTraversingTrafficInMs, RestMap<String, String> attributesMap)
    {
        this.type = type;
        this.name = name;
        this.hostNodeIndex = hostNodeIndex;
        this.capacity = capacity;
        this.capacityMeasurementUnits = capacityMeasurementUnits;
        this.capacityIOccupyInBaseResource = capacityIOccupyInBaseResource;
        this.processingTimeToTraversingTrafficInMs = processingTimeToTraversingTrafficInMs;
        this.attributesMap = attributesMap;
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
        this.processingTimeToTraversingTrafficInMs = res.getProcessingTimeToTraversingTrafficInMs();
        this.attributesMap = new RestMap<>(res.getAttributes());
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

    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

    public double getCapacity()
    {
        return capacity;
    }

    public void setCapacityMeasurementUnits(String capacityMeasurementUnits)
    {
        this.capacityMeasurementUnits = capacityMeasurementUnits;
    }

    public String getCapacityMeasurementUnits()
    {
        return capacityMeasurementUnits;
    }

    public void setCapacityIOccupyInBaseResource(RestMap<Integer, Double> capacityIOccupyInBaseResource)
    {
        this.capacityIOccupyInBaseResource = capacityIOccupyInBaseResource;
    }

    public RestMap<Integer, Double> getCapacityIOccupyInBaseResource()
    {
        return capacityIOccupyInBaseResource;
    }

    public void setProcessingTimeToTraversingTrafficInMs(double processingTimeToTraversingTrafficInMs)
    {
        this.processingTimeToTraversingTrafficInMs = processingTimeToTraversingTrafficInMs;
    }

    public double getProcessingTimeToTraversingTrafficInMs()
    {
        return processingTimeToTraversingTrafficInMs;
    }

    public void setAttributesMap(RestMap<String, String> attributesMap)
    {
        this.attributesMap = attributesMap;
    }

    public RestMap<String, String> getAttributesMap() {
        return attributesMap;
    }
}
