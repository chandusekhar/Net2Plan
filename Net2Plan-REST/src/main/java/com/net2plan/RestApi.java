package com.net2plan;

import com.net2plan.interfaces.networkDesign.NetPlan;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "design" path)
 */
@Path("design")
public class RestApi
{
    private NetPlan netPlan = new NetPlan();
    /**
     * Method handling HTTP GET request.
     *
     * @return Current Net2Plan design
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDesign()
    {
        return netPlan.toString();
    }
    
}
