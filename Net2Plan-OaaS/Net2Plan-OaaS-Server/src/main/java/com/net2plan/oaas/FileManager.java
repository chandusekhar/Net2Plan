package com.net2plan.oaas;

import com.shc.easyjson.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileManager {

    public static JSONObject writePersistenceFile (String data, String algorithmName, String userToken)
    {

        JSONObject response = new JSONObject();

        try  {

            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + algorithmName + "_persistenceFile_"+userToken+".n2p"), "".getBytes());
            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + algorithmName + "_persistenceFile_"+userToken+".n2p"), data.getBytes());
            response.put("status",new JSONValue("OK"));
            response.put("content",new JSONValue("No content for this function"));
        }catch(Exception ex){
            response.put("status", new JSONValue("ERROR"));
            response.put("content", new JSONValue(ex.getMessage()));
            ex.printStackTrace();
        }
        return response;
    }
    public static JSONObject readPersistenceFile (String algorithmName, String userToken)
    {
        JSONObject response = new JSONObject();

        try{
            byte [] bytes = Files.readAllBytes(new File(ServerUtils.TOMCAT_FILES_DIR + File.separator+algorithmName + "_persistenceFile_"+userToken+".n2p").toPath());
            String everything = new String(bytes, StandardCharsets.UTF_8);

            JSONObject jsonObject = JSON.parse(everything);
            response.put("status", new JSONValue("OK"));
            response.put("content", new JSONValue(jsonObject));

        } catch (Exception ex) {
            response.put("status", new JSONValue("ERROR"));
            response.put("content", new JSONValue(ex.getMessage()));
            ex.printStackTrace();
        }
        return response;
    }

}
