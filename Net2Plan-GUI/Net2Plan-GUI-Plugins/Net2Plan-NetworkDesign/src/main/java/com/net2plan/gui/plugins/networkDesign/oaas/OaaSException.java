package com.net2plan.gui.plugins.networkDesign.oaas;

public class OaaSException extends RuntimeException
{
    private String message;

    public OaaSException()
    {
        super();
    }

    public OaaSException(String message)
    {
        super(message);
    }
}

