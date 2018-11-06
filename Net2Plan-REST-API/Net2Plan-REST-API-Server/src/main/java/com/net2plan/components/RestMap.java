package com.net2plan.components;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestMap
{
    private Map<String, String> map;
    public RestMap(){}

    public RestMap(Map<String, String> map)
    {
        this.map = new LinkedHashMap<>();
        this.map.putAll(map);
    }

    public void setMap(Map<String, String> map)
    {
        this.map = map;
    }

    public Map<String, String> getMap()
    {
        return map;
    }
}
