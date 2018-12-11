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
package com.net2plan.gui.plugins.networkDesign.oaas;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.oaas.OaaSSelector;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.oaas.ClientUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

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
        algorithmSelector = new OaaSSelector(mainWindow, ClientUtils.ExecutionType.ALGORITHM, algorithmParameters);
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
