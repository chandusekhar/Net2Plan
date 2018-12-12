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

import com.net2plan.gui.plugins.GUINetworkDesign;;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.NetworkLayer;
import com.net2plan.oaas.ClientUtils;
import com.net2plan.oaas.Net2PlanOaaSClient;
import com.net2plan.utils.Pair;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONObject;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections15.BidiMap;

import javax.swing.*;
import javax.ws.rs.core.Response;
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
        algorithmSelector = new OaaSSelector(mainWindow, ClientUtils.ExecutionType.ALGORITHM, algorithmParameters);
        JPanel pnl_buttons = new JPanel(new MigLayout("", "[center, grow]", "[]"));

        btn_execute = new JButton("Execute");
        pnl_buttons.add(btn_execute);
        btn_execute.addActionListener(e ->
        {
            try {
            Pair<ClientUtils.ExecutionType, String> execInfo = algorithmSelector.getExecutionInformation();
            Map<String, String> execParameters = algorithmSelector.getRunnableParameters();
            NetPlan netPlan = mainWindow.getDesign().copy();
            Net2PlanOaaSClient client = mainWindow.getNet2PlanOaaSClient();
            Response algorithmResponse = client.executeOperation(execInfo.getFirst(), execInfo.getSecond(), execParameters, netPlan);
            String responseMessage = algorithmResponse.readEntity(String.class);
            if(algorithmResponse.getStatus() != 200)
                throw new OaaSException(responseMessage);


            JSONObject responseJSON = JSON.parse(responseMessage);
            String response = responseJSON.get("executeResponse").getValue();
            JSONObject responseNetPlanJSON = responseJSON.get("outputNetPlan").getValue();
            NetPlan responseNetPlan = new NetPlan(responseNetPlanJSON);

            responseNetPlan.setNetworkLayerDefault(responseNetPlan.getNetworkLayer(0));
            mainWindow.getDesign().assignFrom(responseNetPlan);

            final VisualizationState vs = mainWindow.getVisualizationState();
            final NetPlan newNetPlan = mainWindow.getDesign();
            Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer,Boolean>> res = vs.suggestCanvasUpdatedVisualizationLayerInfoForNewDesign(new HashSet<>(newNetPlan.getNetworkLayers()));
            vs.setCanvasLayerVisibilityAndOrder(newNetPlan, res.getFirst() , res.getSecond());
            mainWindow.updateVisualizationAfterNewTopology();
            mainWindow.addNetPlanChange();
            String outMessage = String.format("Algorithm executed successfully%nExecution time: %.3g s%nExit message: %s", 0, response);
            JOptionPane.showMessageDialog(null, outMessage, "Solve design", JOptionPane.PLAIN_MESSAGE);


            } catch (Throwable e1)
            {
                e1.printStackTrace();
            }

        });

        add(algorithmSelector, "grow");
        add(pnl_buttons, "dock south");

		
	}
}
