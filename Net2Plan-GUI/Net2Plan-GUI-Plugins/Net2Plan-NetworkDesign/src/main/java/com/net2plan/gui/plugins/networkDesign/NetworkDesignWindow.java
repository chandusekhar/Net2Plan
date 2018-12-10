/*******************************************************************************
 * Copyright (c) 2017 Pablo Pavon Marino and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 2-clause BSD License 
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/BSD-2-Clause
 *
 * Contributors:
 *     Pablo Pavon Marino and others - initial API and implementation
 *******************************************************************************/
package com.net2plan.gui.plugins.networkDesign;

/**
 * Created by Jorge San Emeterio on 7/03/17.
 */
public enum NetworkDesignWindow
{
    network(NetworkDesignWindow.networkWindowName),
    offline(NetworkDesignWindow.offlineWindowName),
    oassalgorithms(NetworkDesignWindow.oaasAlgorithmsWindowName),
    online(NetworkDesignWindow.onlineWindowName),
    whatif(NetworkDesignWindow.whatifWindowName),
    report(NetworkDesignWindow.reportWindowName),
    oaasreports(NetworkDesignWindow.oaasReportsWindowName);

    private final static String networkWindowName = "View/Edit network state";
    private final static String offlineWindowName = "Offline algorithms";
    private final static String oaasAlgorithmsWindowName = "OaaS algorithms";
    private final static String onlineWindowName = "Online simulation";
    private final static String whatifWindowName = "What-if analysis";
    private final static String reportWindowName = "View reports";
    private final static String oaasReportsWindowName = "View OaaS reports";

    private final String text;

    NetworkDesignWindow(final String text)
    {
        this.text = text;
    }

    public static NetworkDesignWindow parseString(final String text)
    {
        switch (text)
        {
            case NetworkDesignWindow.networkWindowName:
                return network;
            case NetworkDesignWindow.offlineWindowName:
                return offline;
            case NetworkDesignWindow.oaasAlgorithmsWindowName:
                return oassalgorithms;
            case NetworkDesignWindow.onlineWindowName:
                return online;
            case NetworkDesignWindow.whatifWindowName:
                return whatif;
            case NetworkDesignWindow.reportWindowName:
                return report;
            case NetworkDesignWindow.oaasReportsWindowName:
                return oaasreports;
        }

        return null;
    }

    public static String getWindowName(final NetworkDesignWindow tab)
    {
        switch (tab)
        {
            case network:
                return NetworkDesignWindow.networkWindowName;
            case offline:
                return NetworkDesignWindow.offlineWindowName;
            case oassalgorithms:
                return NetworkDesignWindow.oaasAlgorithmsWindowName;
            case online:
                return NetworkDesignWindow.onlineWindowName;
            case whatif:
                return NetworkDesignWindow.whatifWindowName;
            case report:
                return NetworkDesignWindow.reportWindowName;
            case oaasreports:
                return NetworkDesignWindow.oaasReportsWindowName;
        }

        return null;
    }
}
