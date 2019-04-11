package com.net2plan.oaas;

import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class FileManager {


    private Map<String,Object> persistenceData = new HashMap();
    private JSONObject persistenceDataJSON = new JSONObject();

    public JSONObject writePersistenceFile (String data, String algorithmName, String userToken) {

        JSONObject response = new JSONObject();

        try  {

            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + algorithmName + "_persistenceFile_"+userToken+".n2p"), "".getBytes());
            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + algorithmName + "_persistenceFile.n2p"), data.getBytes());
            response.put("status",new JSONValue("OK"));
            response.put("content",new JSONValue("No content for this function"));
        }catch(Exception ex){
            response.put("status", new JSONValue("ERROR"));
            response.put("content", new JSONValue(ex.getMessage()));
            ex.printStackTrace();
        }
        return response;
    }

    public JSONObject readPersistenceFile (String algorithmName, String userToken)
    {
        JSONObject response = new JSONObject();

        try{
            byte [] bytes = Files.readAllBytes(new File(ServerUtils.TOMCAT_FILES_DIR + File.separator+algorithmName + "_persistenceFile_"+userToken+".n2p").toPath());
            String everything = new String(bytes, StandardCharsets.UTF_8);

            JSONObject jsonObject = JSON.parse(everything);
            response.put("status", new JSONValue("OK"));
            response.put("content", new JSONValue(jsonObject));

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return response;
    }

    public void addPersistenceData(String key, Object value){

        this.persistenceData.put(key,value);

        if(value instanceof Map){
            JSONObject mapObject = new JSONObject();
            Map<String,Object> newMap = (Map<String, Object>)value;

            for (Map.Entry<String, Object> entry : newMap.entrySet()) {
                System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
                String newKey = entry.getKey();
                Object object = entry.getValue();

                if (value instanceof String) {
                    mapObject.put(newKey, new JSONValue((String) object));
                } else if (value instanceof Integer) {
                    mapObject.put(newKey, new JSONValue((Integer) object));
                } else if (value instanceof Double) {
                    mapObject.put(newKey, new JSONValue((Double) object));
                } else if (value instanceof Long) {
                    mapObject.put(newKey, new JSONValue((Long) object));
                }
            }
            persistenceDataJSON.put(key,new JSONValue(mapObject));
        }else{
            if(value instanceof String){
                persistenceDataJSON.put(key,new JSONValue((String)value));
            }else if(value instanceof Integer){
                persistenceDataJSON.put(key,new JSONValue((Integer)value));
            }else if(value instanceof Double){
                persistenceDataJSON.put(key,new JSONValue((Double)value));
            }else if(value instanceof Long){
                persistenceDataJSON.put(key,new JSONValue((Long)value));
            }

        }

        System.out.println(persistenceDataJSON);
    }


    public void write(){

        persistenceDataJSON = new JSONObject();

        for (Map.Entry<String, Object> entry : persistenceData.entrySet()) {
            System.out.println("clave=" + entry.getKey() + ", valor=" + entry.getValue());
            String newKey = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Collections){
                persistenceDataJSON.put(newKey,new JSONValue(addCollectionObject()));

            }else {
                persistenceDataJSON.put(newKey,addNoCollectionObject());
            }

        }

    }

    public JSONValue addNoCollectionObject(){

        if (value instanceof String) {
            mapObject.put(newKey, new JSONValue((String) value));
        } else if (value instanceof Integer) {
            mapObject.put(newKey, new JSONValue((Integer) object));
        } else if (value instanceof Double) {
            mapObject.put(newKey, new JSONValue((Double) object));
        } else if (value instanceof Long) {
            mapObject.put(newKey, new JSONValue((Long) object));
        }
    }

    public JSONArray addCollectionObject(){

    }





    public static void main(String [] args){

      FileManager fileManager = new FileManager();
      fileManager.addPersistenceData("latencia",20.0);
      fileManager.addPersistenceData("origen",2);

      Map<String,Object> map = new HashMap<>();
      List<Integer> numbers = new ArrayList();
      map.put("id",1);
      map.put("id",2);
      map.put("id",3);

      fileManager.addPersistenceData("links",map);
      fileManager.addPersistenceData("destino",3);

    }
}
