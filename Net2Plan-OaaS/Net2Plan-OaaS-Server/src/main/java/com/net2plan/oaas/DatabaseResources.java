package com.net2plan.oaas;

import com.net2plan.utils.Pair;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;

/**
 * Database resources
 */

@Path("/database")
public class DatabaseResources
{
    private DatabaseController dbController = ServerUtils.dbController;
    /**
     * Establishes the user and the password of the Database admin (URL: /database/connection, Operation: POST, Consumes: APPLICATION/JSON, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @POST
    @Path("/connection")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setDatabaseConfiguration(String confJSON)
    {
        JSONObject json = new JSONObject();
        try {
            JSONObject conf = JSON.parse(confJSON);
            String user = conf.get("username").getValue();
            String pass = conf.get("password").getValue();
            String ipPort = conf.get("ipport").getValue();
            ServerUtils.dbController = new DatabaseController(ipPort, user, pass);
        } catch (Exception e)
        {
            json.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(json);
        }

        json.put("message",new JSONValue("SQL Connection established successfully"));
        return ServerUtils.OK(json);
    }

    /**
     * Authenticates an user with its password (URL: /database/authenticate, Operation: POST, Consumes: APPLICATION/JSON, Produces: APPLICATION/JSON)
     * @return HTTP Response
     */
    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(String authJSON)
    {
        JSONObject json = new JSONObject();
        Response resp = null;
        try {
            JSONObject conf = JSON.parse(authJSON);
            String user = conf.get("username").getValue();
            String pass = conf.get("password").getValue();
            Pair<Boolean, ResultSet> auth = dbController.authenticate(user, pass);
            boolean authBool = auth.getFirst();
            if(authBool)
            {
                ResultSet set = auth.getSecond();
                if(set != null)
                {
                    if(set.first())
                    {
                        String username = set.getString("user");
                        long id = set.getLong("id");
                        String category = set.getString("category");

                        String token = ServerUtils.addToken(username, id, category);
                        json.put("message", new JSONValue("Authenticated"));
                        json.put("token", new JSONValue(token));
                        resp = ServerUtils.OK(json);
                    }
                    else{
                        json.put("message", new JSONValue("Error retrieving information from Database"));
                        return ServerUtils.SERVER_ERROR(json);
                    }
                }
                else{
                    json.put("message", new JSONValue("Error retrieving information from Database"));
                    return ServerUtils.SERVER_ERROR(json);
                }
            }
            else{
                json.put("message", new JSONValue("No authenticated"));
                return ServerUtils.SERVER_ERROR(json);
            }
        } catch (Exception e)
        {
            json.put("message", new JSONValue(e.getMessage()));
            return ServerUtils.SERVER_ERROR(json);
        }

        return resp;
    }

}
