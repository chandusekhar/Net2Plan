package com.net2plan;


import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

public class PruebaCliente
{
    public static void main(String [] args)
    {
        Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget target = client.target("http://localhost:8080/net2plan-oaas-server-0.7.0-SNAPSHOT/OaaS/").path("JAR");

        File f = new File("C:\\Users\\César\\Desktop\\Net2Plan-0.6.1\\workspace\\BuiltInExamples.jar");
        FileDataBodyPart body = new FileDataBodyPart("file",f);
        MultiPart multi = new MultiPart();
        multi.bodyPart(body);

        Invocation.Builder inv = target.request(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON);
        Response r = inv.post(Entity.entity(multi,MediaType.MULTIPART_FORM_DATA));

        System.out.println(r.getStatus());
        System.out.println(r.getStatusInfo());
        System.out.println(r.readEntity(String.class));
    }
}
