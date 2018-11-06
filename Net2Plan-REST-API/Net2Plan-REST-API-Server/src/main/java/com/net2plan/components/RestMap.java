package com.net2plan.components;

import java.util.LinkedHashMap;
import java.util.Map;

public class RestMap<K,V>
{
    private Map<K, V> map;
    public RestMap()
    {
        this.map = new LinkedHashMap<>();
    }

    public RestMap(Map<K,V> map)
    {
        this.map = new LinkedHashMap<>();
        this.map.putAll(map);
    }

    public void setMap(Map<K,V> map)
    {
        this.map = map;
    }

    public Map<K,V> getMap()
    {
        return map;
    }
}
