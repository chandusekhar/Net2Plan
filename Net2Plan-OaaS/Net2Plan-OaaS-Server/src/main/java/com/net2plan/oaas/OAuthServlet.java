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
import java.io.PrintWriter;

public class OAuthServlet extends HttpServlet
{
    private OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try {
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

            String token = oauthIssuerImpl.accessToken();

            OAuthResponse oauthResponse = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(token)
                    .setExpiresIn("3600")
                    .buildJSONMessage();

            response.setStatus(oauthResponse.getResponseStatus());

            PrintWriter pw = response.getWriter();
            pw.print(oauthResponse.getBody());
            pw.flush();
            pw.close();

        } catch (OAuthSystemException e)
        {
            try {
                OAuthResponse r = OAuthResponse
                        .errorResponse(401)
                        .error(OAuthProblemException.error(e.getMessage()))
                        .buildBodyMessage();

                response.setStatus(r.getResponseStatus());

                PrintWriter pw = response.getWriter();
                pw.print(r.getBody());
                pw.flush();
                pw.close();

                response.sendError(401);
            } catch (OAuthSystemException e1)
            {
                e1.printStackTrace();
            }
        } catch (OAuthProblemException e)
        {

            try {
                OAuthResponse r = OAuthResponse
                        .errorResponse(401)
                        .error(OAuthProblemException.error(e.getMessage()))
                        .buildBodyMessage();

                response.setStatus(r.getResponseStatus());

                PrintWriter pw = response.getWriter();
                pw.print(r.getBody());
                pw.flush();
                pw.close();

                response.sendError(401);
            } catch (OAuthSystemException e1)
            {
                e1.printStackTrace();
            }
        }
    }
}
