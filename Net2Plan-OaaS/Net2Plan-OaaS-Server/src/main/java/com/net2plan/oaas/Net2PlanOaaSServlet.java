package com.net2plan.oaas;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Net2PlanOaaSServlet extends HttpServlet
{
    private OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException,
    {
        try {
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

            String token = oauthIssuerImpl.accessToken();

            OAuthResponse oauthResponse = OAuthASResponse
                    .authorizationResponse(request, HttpServletResponse.SC_OK)
                    .setAccessToken(token)
                    .setExpiresIn("3600")
                    .buildJSONMessage();

            response.setStatus(oauthResponse.getResponseStatus());
        } catch (OAuthSystemException e)
        {
            try {
                OAuthResponse r = OAuthResponse
                        .errorResponse(401)
                        .error(OAuthProblemException.error(e.getMessage()))
                        .buildBodyMessage();
            } catch (OAuthSystemException e1)
            {
                e1.printStackTrace();
            }
        } catch (OAuthProblemException e)
        {

            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

    }
}
