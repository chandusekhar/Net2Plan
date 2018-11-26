package com.net2plan;


import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import java.util.Map;

public class ClientUtils
{
    private ClientUtils(){}

    protected static JSONArray parseUserParameters(Map<String, String> params)
    {
        JSONArray array = new JSONArray();
        for(Map.Entry<String, String> entry : params.entrySet())
        {
            JSONObject this_param = new JSONObject();
            this_param.put("name",new JSONValue(entry.getKey()));
            this_param.put("value", new JSONValue(entry.getValue()));
            array.add(new JSONValue(this_param));
        }
        return array;
    }
}

