package com.net2plan.oaas;


import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ClientUtils
{
    private ClientUtils(){}

    protected static JSONArray parseUserParameters(Map<String, String> params)
    {
        JSONArray array = new JSONArray();
        for(Map.Entry<String, String> entry : params.entrySet())
        {
            JSONObject this_param = new JSONObject();
            this_param.put("name",new JSONValue(entry.getKey()));
            this_param.put("value", new JSONValue(entry.getValue()));
            array.add(new JSONValue(this_param));
        }
        return array;
    }

    protected static void configureSecureClient()
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        return;
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                    {
                        return;
                    }
                }
        };


        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = (urlHostName, session) -> {
            boolean verify = false;
            if (urlHostName.equalsIgnoreCase(session.getPeerHost()))
            {
                verify = true;
            }
            return verify;
        };

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

    }

}

