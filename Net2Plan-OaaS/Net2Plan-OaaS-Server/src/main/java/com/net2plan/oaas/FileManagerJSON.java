package com.net2plan.oaas;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.internal.IExternal;
import com.net2plan.utils.ClassLoaderUtils;
import com.net2plan.utils.Quadruple;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileManagerJSON {

    public static void writeCatalog(List<Quadruple<String, String, List<IAlgorithm>, List<IReport>>> entry){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("catalog",entry);
        try  {

            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + "catalog.n2p"), "".getBytes());
            FileUtils.writeByteArrayToFile(new File(ServerUtils.TOMCAT_FILES_DIR+ File.separator + "catalog.n2p"), jsonObject.toString().getBytes());
            }catch(Exception ex){
            ex.printStackTrace();
        }

    }
    public  List<Quadruple<String, String, List<IAlgorithm>, List<IReport>>> readCatalog(){

        List<Quadruple<String, String, List<IAlgorithm>, List<IReport>>> entry = new ArrayList<>();

        try{
            byte [] bytes = Files.readAllBytes(new File(ServerUtils.TOMCAT_FILES_DIR + File.separator+"catalog.n2p").toPath());
            String everything = new String(bytes, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(everything);
            JSONArray catalog = jsonObject.getJSONArray("catalog");

            for(Object jsonObject1: catalog) {
                JSONObject catalog2 = (JSONObject) jsonObject1;
                System.out.println(catalog2);
                String catalogName=(String)catalog2.getString("first");
                String catalogCategory=catalog2.getString("second");
                List<IAlgorithm> algorithms = new LinkedList<>();
                List<IReport> reports = new LinkedList<>();

                File uploadedFile = new File(ServerUtils.TOMCAT_FILES_DIR + File.separator + catalog2.get("first"));

                URLClassLoader cl = new URLClassLoader(new URL[]{uploadedFile.toURI().toURL()}, this.getClass().getClassLoader());
                List<Class<IExternal>> classes = ClassLoaderUtils.getClassesFromFile(uploadedFile, IExternal.class, cl);
                for (Class<IExternal> _class : classes) {
                    IExternal ext = _class.newInstance();
                    if (ext instanceof IAlgorithm) {
                        IAlgorithm alg = (IAlgorithm) ext;
                        algorithms.add(alg);
                    } else if (ext instanceof IReport) {
                        IReport rep = (IReport) ext;
                        reports.add(rep);
                    }
                }


                entry.add(Quadruple.unmodifiableOf(catalogName, catalogCategory, algorithms, reports));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(entry);
        return entry;

    }

    public static void main(String [] args){

        FileManagerJSON fileManagerJSON = new FileManagerJSON();
        fileManagerJSON.readCatalog();

    }
}
