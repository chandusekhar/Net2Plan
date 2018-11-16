package com.net2plan;

import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.RestDatabase;
import com.net2plan.utils.RestServerUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public NetPlan netPlan = RestDatabase.netPlan;

    @GET
    @Path("/netplan")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDesign()
    {
        return RestServerUtils.OK(netPlan.saveToJSON());
    }

}
