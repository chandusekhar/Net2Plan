package com.net2plan.utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlRootElement
public class RestMap
{
    private Map<String, String> map;
    public RestMap()
    {
        this.map = new LinkedHashMap<>();
    }

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
