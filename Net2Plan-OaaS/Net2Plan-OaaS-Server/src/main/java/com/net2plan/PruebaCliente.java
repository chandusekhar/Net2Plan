package com.net2plan;


import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class PruebaCliente
{
    public static void main(String [] args)
    {
        Client client = ClientBuilder.newClient().register(MultiPartFeature.class);
        WebTarget target = client.target("http://localhost:8080/net2plan-oaas-server-0.7.0-SNAPSHOT/OaaS/").path("JAR");


        Invocation.Builder inv = target.request().accept(MediaType.APPLICATION_JSON);
        Response r = inv.get();

        System.out.println(r.getStatus());
        System.out.println(r.getStatusInfo());
    }
}
