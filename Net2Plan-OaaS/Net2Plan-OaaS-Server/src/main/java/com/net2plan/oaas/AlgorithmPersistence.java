package com.net2plan.oaas;

import com.shc.easyjson.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmPersistence {

    private Map<String,Object> persistenceData = new HashMap();
    private JSONObject persistenceDataJSON = new JSONObject();

    public void addPersistenceData(String key, Object value)
    {

        this.persistenceData.put(key,value);

    }
    public void clear()
    {

        this.persistenceData.clear();

    }
    public String getPersistenceData()
    {

        persistenceDataJSON = new JSONObject();

        for (Map.Entry<String, Object> entry : persistenceData.entrySet()) {

            String newKey = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List){
                JSONValue jsonValue = addListObject(value);
                if(jsonValue!=null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }else if (value instanceof Map){
                JSONValue jsonValue = addMapObject(value);
                if(value!=null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }else {
                JSONValue jsonValue= addSimpleObject(value);
                if(jsonValue != null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }

        }
        return JSON.write(persistenceDataJSON);

    }
    private JSONValue addSimpleObject(Object value)
    {

        JSONValue jsonValue = null;

        if (value instanceof String) {
            jsonValue = new JSONValue((String) value);
        } else if (value instanceof Integer) {
            jsonValue = new JSONValue((Integer) value);
        } else if (value instanceof Double) {
            jsonValue = new JSONValue((Double) value);
        } else if (value instanceof Long) {
            jsonValue = new JSONValue((Long) value);
        }
        return jsonValue;
    }
    private JSONValue addListObject(Object value)
    {

        JSONArray jsonArray = new JSONArray();

        if(value instanceof List){

            List<Object> list = (List<Object>)value;

            if (list == null || list.size() == 0)
                return new JSONValue(jsonArray);

            if(list.get(0) instanceof List ){
                list.stream().forEach(n-> jsonArray.add(addListObject(n)));

            }else if( list.get(0) instanceof Map){
                list.stream().forEach(n->{ Map<String,Object> map = (Map<String,Object>)n; jsonArray.add(addMapObject(map));});

            } else{
                list.stream().forEach(n-> jsonArray.add(addSimpleObject(n)));

            }

        }

        return new JSONValue(jsonArray);

    }
    private JSONValue addMapObject(Object value)
    {
        Map<String,Object> map = (Map<String,Object>) value;

        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String newKey = entry.getKey();
            Object newValue = entry.getValue();

            if (newValue instanceof List){
                JSONValue newJsonA = addListObject(newValue);
                if(newJsonA!=null)
                    jsonObject.put(newKey,newJsonA);

            }else if (newValue instanceof Map){
                JSONValue newJsonA = addMapObject(newValue);
                if(newJsonA!=null)
                    jsonObject.put(newKey,newJsonA);

            }else {
                JSONValue jsonValue= addSimpleObject(newValue);
                if(jsonValue != null)
                    jsonObject.put(newKey,jsonValue);
            }


        }
        return new JSONValue(jsonObject);
    }



    public static void main(String [] args){

        /*Example to add information to save*/
        AlgorithmPersistence algorithmPersistence = new AlgorithmPersistence();

        /*Add simple objects*/
        algorithmPersistence.addPersistenceData("latencia",22.1);
        algorithmPersistence.addPersistenceData("origen",2);
        algorithmPersistence.addPersistenceData("destino",3);

        /*Add Lists<Object> or Maps<String,Object> objects*/
        /*Add List of simple object*/
        List<Integer> numbers = new ArrayList();
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);
        algorithmPersistence.addPersistenceData("links_ids",numbers);

        /*Add Map of simple object*/
        Map<String,Object> map = new HashMap<>();
        map.put("id",1);
        map.put("cost",200);
        map.put("distance",3005);
        algorithmPersistence.addPersistenceData("path",map);

        /*Add List of List/Maps object*/
        Map<String,Object> map_level_1_a = new HashMap<>();
        Map<String,Object> map_level_1_b = new HashMap<>();
        Map<String,Object> map_level_2_a = new HashMap<>();
        Map<String,Object> map_level_2_b = new HashMap<>();

        map_level_1_a.put("id",3);
        map_level_1_a.put("capacity",30.0);
        map_level_2_a.put("n_origin",1);
        map_level_2_a.put("n_destin",2);
        map_level_1_a.put("nodes",map_level_2_a);

        map_level_1_b.put("id",4);
        map_level_1_b.put("capacity",25);
        map_level_2_b.put("n_origin",6);
        map_level_2_b.put("n_destin",5);
        map_level_1_b.put("nodes",map_level_2_b);

        List<Map<String,Object>> maps = new ArrayList<>();
        maps.add(map_level_1_a);
        maps.add(map_level_1_b);

        algorithmPersistence.addPersistenceData("links_features",maps);

        /*Get string of JSONObject of persistence information*/
        String data = algorithmPersistence.getPersistenceData();

        /*Write persistence file*/
        String responseWrite = JSON.write(FileManager.writePersistenceFile(data,"demo","token"));
        //System.out.println(responseWrite);

        /*Read persistence file*/
        String responseRead = JSON.write(FileManager.readPersistenceFile("demo","token"));
       /* System.out.println("GET RESPONSE OF PERSISTENCE FILE");
        System.out.println(responseRead);*/

        /*Retrieve some data*/
        try {
            /* Parse response to JSON*/
            JSONObject responseReadJSON = JSON.parse(responseRead);

            /* Retrive response content*/
            JSONObject content = responseReadJSON.get("content").getValue();
           /* System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("GET CONTENT FROM RESPONSE");*/
            System.out.println(JSON.write(content));

            /*Retrive our field/s*/
            JSONArray links_features = content.get("links_features").getValue();
            JSONArray links_ids = content.get("links_ids").getValue();
            /*
            for(JSONValue id:links_ids){
                System.out.println(id.getValue().toString());
            }*/

           /* System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("FIELD LINKS_FEATURES");*/

            for(JSONValue link: links_features){
                JSONObject enlace = link.getValue();
               /* System.out.println("");
                System.out.println(JSON.write(enlace));
                System.out.println("");
                System.out.println("-Link: "+enlace.get("id").getValue());
                System.out.println("Capacity: "+ enlace.get("capacity").getValue().toString());*/
                JSONObject nodes = enlace.get("nodes").getValue();
               /* System.out.println("N_origin: "+ nodes.get("n_origin").getValue().toString());
                System.out.println("N_destin: "+nodes.get("n_destin").getValue().toString());
                System.out.println("");*/
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
