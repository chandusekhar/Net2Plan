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
package com.net2plan.gui.plugins.networkDesign.oaasExecPane;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.ThreadExecutionController;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.utils.OaaSSelector;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.gui.utils.RunnableSelector;
import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.SystemUtils;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.utils.ClassLoaderUtils;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections15.BidiMap;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.util.HashSet;
import java.util.Map;

public class OaaSExecutionPanel extends JPanel
{
	private final GUINetworkDesign mainWindow;
    private OaaSSelector algorithmSelector;
    final JButton btn_execute;
	
	public OaaSExecutionPanel(GUINetworkDesign mainWindow)
	{
		super ();

		this.mainWindow = mainWindow;
		
		setLayout(new MigLayout("insets 0 0 0 0", "[grow]", "[grow]"));

        ParameterValueDescriptionPanel algorithmParameters = new ParameterValueDescriptionPanel();
        algorithmSelector = new OaaSSelector("Algorithm", algorithmParameters);
        JPanel pnl_buttons = new JPanel(new MigLayout("", "[center, grow]", "[]"));

        btn_execute = new JButton("Execute");
        pnl_buttons.add(btn_execute);
        btn_execute.addActionListener(e ->
        {

        });

        add(algorithmSelector, "grow");
        add(pnl_buttons, "dock south");

		
	}
}
