package com.net2plan.utils;



import com.net2plan.internal.SystemUtils;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class RestUtils
{

    /**
     * Directory where uploaded files will be stored while they are being analyzed
     */
    public final static File UPLOAD_DIR;
    static { UPLOAD_DIR = new File(SystemUtils.getCurrentDir().getAbsolutePath() + File.separator + "upload"); }

    /**
     * Creates a HTTP response 200, OK with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 200, OK
     */
    public static Response OK(Object message)
    {
        if(message == null)
            return Response.ok().build();
        else
            return Response.ok(message).build();
    }

    /**
     * Creates a HTTP response 404, NOT FOUND with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 404, NOT FOUND
     */
    public static Response NOT_FOUND(Object message)
    {
        if(message != null)
            return Response.status(Response.Status.NOT_FOUND).entity(message).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Creates a HTTP response 500, SERVER ERROR with a specific message
     * @param message message to return (null if no message is desired)
     * @return HTTP response 500, SERVER ERROR
     */
    public static Response SERVER_ERROR(Object message)
    {
        if(message == null)
            return Response.serverError().build();
        else
            return Response.serverError().entity(message).build();
    }

    /**
     * Decompresses a JAR file
     * @param jarFile JAR file to decompress
     */
    public static void decompressJarFile(File jarFile)
    {
        String destDir = jarFile.getParentFile().getAbsolutePath();
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> enumJar = jar.entries();
            while(enumJar.hasMoreElements())
            {
                JarEntry file = enumJar.nextElement();
                File f = new File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory())
                {
                    f.mkdir();
                }
            }
            enumJar = jar.entries();
            while(enumJar.hasMoreElements())
            {
                JarEntry file = enumJar.nextElement();
                File f = new File(destDir + java.io.File.separator + file.getName());
                if (file.isDirectory())
                {
                    continue;
                }
                InputStream is = jar.getInputStream(file);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0)
                {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
            jar.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all files and directories inside a directory
     * @param folder directory to leave empty
     * @param deleteFolder true if folder will be deleted, false if not
     */
    public static void cleanFolder(File folder, boolean deleteFolder)
    {
        if(!folder.isDirectory())
            return;
        File [] files = folder.listFiles();
        if(files != null)
        {
            for(File f: files)
            {
                if(f.isDirectory())
                {
                    cleanFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if(deleteFolder)
            folder.delete();
    }

    public static List<File> findClassFilesInFolder(File folder)
    {
        List<File> classFiles = new LinkedList<>();
        if(!folder.isDirectory())
            return Collections.EMPTY_LIST;
        File [] files = folder.listFiles();
        if(files != null)
        {
            for(File f: files)
            {
                if(f.isDirectory())
                {
                    classFiles.addAll(findClassFilesInFolder(f));
                } else {
                    if(f.getName().endsWith(".class") && !f.getName().contains("$"))
                        classFiles.add(f);
                }
            }
        }

        return classFiles;
    }


}
