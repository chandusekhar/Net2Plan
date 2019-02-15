package com.net2plan.oaas;


import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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

    protected static Client createHTTPSClient()
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
        HostnameVerifier hv = (urlHostName, session) -> true;

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        Client client = ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier(hv).build().register(MultiPartFeature.class);

        return client;
    }

    /**
     * Execution Type in Net2Plan OaaS (ALGORITHM or REPORT)
     */
    public enum ExecutionType
    {
        /**
         * ALGORITHM type
         */
        ALGORITHM("ALGORITHM"),
        /**
         * REPORT type
         */
        REPORT("REPORT");

        private String label;

        ExecutionType(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return this.label;
        }
    }

    /**
     * Net2Plan OaaS Client working mode (HTTP or HTTPS)
     */
    public enum ClientMode
    {
        /**
         * HTTP mode
         */
        HTTP("http"),
        /**
         * HTTPS mode
         */
        HTTPS("https");

        private String label;

        ClientMode(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return this.label;
        }

        public ClientMode getModeFromId(String id)
        {
            ClientMode mode = null;
            switch(id)
            {
                case "http":
                    mode = ClientMode.HTTP;
                    break;
                case "https":
                    mode = ClientMode.HTTPS;
                    break;
            }
            return mode;
        }
    }

    public enum Category
    {
        /**
         * INVITED category
         */
        INVITED("INVITED"),


        /**
         * MASTER category
         */
        MASTER("MASTER");

        private String label;

        Category(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return this.label;
        }
    }

}

