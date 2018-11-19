package com.net2plan;

import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.RestDatabase;
import com.net2plan.utils.RestJAR;
import com.net2plan.utils.RestServerUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/OaaS")
public class Net2PlanOaaS
{
    public NetPlan netPlan = RestDatabase.netPlan;
    public Map<String, List<IAlgorithm>> jar2AlgorithmsMap = RestDatabase.jar2AlgorithmsMap;
    public Map<String, List<IReport>> jar2ReportsMap = RestDatabase.jar2ReportsMap;

    @GET
    @Path("/design")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDesign()
    {
        return RestServerUtils.OK(netPlan.saveToJSON());
    }

    @POST
    @Path("/JAR")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadJAR(RestJAR jar)
    {
        File jarFile = jar.getFile();
        //jar2AlgorithmsMap.put(jarFile.getName(), jar.getInternalAlgorithms());
        //jar2ReportsMap.put(jarFile.getName(), jar.getInternalReports());

        System.out.println(jar2AlgorithmsMap);
        System.out.println(jar2ReportsMap);

        return RestServerUtils.OK(jar);
    }

}
