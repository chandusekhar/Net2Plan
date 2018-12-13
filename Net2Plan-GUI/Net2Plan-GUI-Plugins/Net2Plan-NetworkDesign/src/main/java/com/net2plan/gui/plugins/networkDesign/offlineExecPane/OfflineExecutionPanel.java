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
package com.net2plan.gui.plugins.networkDesign.offlineExecPane;

import com.net2plan.gui.plugins.GUINetworkDesignConstants;
import com.net2plan.gui.plugins.networkDesign.ThreadExecutionController;
import com.net2plan.gui.plugins.networkDesign.oaas.OaaSException;
import com.net2plan.gui.plugins.networkDesign.oaas.OaaSSelector;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.gui.utils.RunnableSelector;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.SystemUtils;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.oaas.ClientUtils;
import com.net2plan.oaas.Net2PlanOaaSClient;
import com.net2plan.utils.ClassLoaderUtils;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.ParseException;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections15.BidiMap;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Closeable;
import java.io.File;
import java.util.HashSet;
import java.util.Map;

public class OfflineExecutionPanel extends JPanel implements ThreadExecutionController.IThreadExecutionHandler
{
	private final GUINetworkDesign mainWindow;
    private ThreadExecutionController algorithmController;
    private RunnableSelector algorithmSelector;
    private OaaSSelector remoteAlgorithmSelector;
    private JPanel cardPanel;
    private long start;
    final JButton btn_solve;
    final JRadioButton localButton, remoteButton;
    private GUINetworkDesignConstants.ExecutionMode mode;
	
	public OfflineExecutionPanel (GUINetworkDesign mainWindow)
	{
		super ();

		this.mainWindow = mainWindow;
		this.mode = GUINetworkDesignConstants.ExecutionMode.LOCAL;
		
		setLayout(new MigLayout("insets 0 0 0 0", "[grow]", "[grow]"));
        
        File ALGORITHMS_DIRECTORY = new File(IGUIModule.CURRENT_DIR + SystemUtils.getDirectorySeparator() + "workspace");
        ALGORITHMS_DIRECTORY = ALGORITHMS_DIRECTORY.isDirectory() ? ALGORITHMS_DIRECTORY : IGUIModule.CURRENT_DIR;

        ParameterValueDescriptionPanel algorithmParameters = new ParameterValueDescriptionPanel();
        ParameterValueDescriptionPanel remoteAlgorithmParameters = new ParameterValueDescriptionPanel();
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());

        algorithmSelector = new RunnableSelector("Algorithm", null, IAlgorithm.class, ALGORITHMS_DIRECTORY, algorithmParameters);
        remoteAlgorithmSelector = new OaaSSelector(mainWindow, ClientUtils.ExecutionType.ALGORITHM, remoteAlgorithmParameters);
        algorithmController = new ThreadExecutionController(this);

        JPanel pnl_radio = new JPanel(new MigLayout("", "[center, grow]", "[]"));
        JPanel pnl_buttons = new JPanel(new MigLayout("", "[center, grow]", "[]"));

        btn_solve = new JButton("Execute");
        pnl_buttons.add(btn_solve);
        btn_solve.addActionListener(e -> algorithmController.execute());

        localButton = new JRadioButton("Local");
        remoteButton = new JRadioButton("Remote");

        pnl_radio.add(localButton);
        pnl_radio.add(remoteButton);

        cardPanel.add(algorithmSelector, "Local");
        cardPanel.add(remoteAlgorithmSelector, "Remote");

        add(pnl_radio, "dock north");
		add(cardPanel, "grow");
        add(pnl_buttons, "dock south");

        localButton.addItemListener(e ->
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                this.mode = GUINetworkDesignConstants.ExecutionMode.LOCAL;
                remoteButton.setSelected(false);
                CardLayout cl = (CardLayout)cardPanel.getLayout();
                cl.show(cardPanel, "Local");
            }
        });

        remoteButton.addItemListener(e ->
        {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                this.mode = GUINetworkDesignConstants.ExecutionMode.REMOTE;
                localButton.setSelected(false);
                CardLayout cl = (CardLayout)cardPanel.getLayout();
                cl.show(cardPanel, "Remote");
            }
        });

        localButton.setSelected(true);

		
	}
	
	public void reset ()
	{
		algorithmSelector.reset();
	}

	@Override
	public Object execute(ThreadExecutionController controller)
	{
        start = System.nanoTime();
        String out = "";
        switch(mode)
        {
            case LOCAL:
                final Triple<File, String, Class> algorithm = algorithmSelector.getRunnable();
                final Map<String, String> algorithmParameters = algorithmSelector.getRunnableParameters();
                Configuration.updateSolverLibraryNameParameter(algorithmParameters); // put default path to libraries if solverLibraryName is ""
                final Map<String, String> net2planParameters = Configuration.getNet2PlanOptions();
                NetPlan netPlan = mainWindow.getDesign().copy();
                IAlgorithm instance = ClassLoaderUtils.getInstance(algorithm.getFirst(), algorithm.getSecond(), IAlgorithm.class , null);
                out = instance.executeAlgorithm(netPlan, algorithmParameters, net2planParameters);
                try {
                    ((Closeable) instance.getClass().getClassLoader()).close();
                } catch (Throwable e) {
                }
                netPlan.setNetworkLayerDefault(netPlan.getNetworkLayer((int) 0));
                mainWindow.getDesign().assignFrom(netPlan);
                break;
            case REMOTE:
                try {
                Pair<ClientUtils.ExecutionType, String> execInfo = remoteAlgorithmSelector.getExecutionInformation();
                Map<String, String> execParameters = remoteAlgorithmSelector.getRunnableParameters();
                NetPlan netPlan_copy = mainWindow.getDesign().copy();
                Net2PlanOaaSClient client = mainWindow.getNet2PlanOaaSClient();
                Response algorithmResponse = client.executeOperation(execInfo.getFirst(), execInfo.getSecond(), execParameters, netPlan_copy);
                String responseMessage = algorithmResponse.readEntity(String.class);
                if(algorithmResponse.getStatus() != 200)
                    throw new Net2PlanException(JSON.parse(responseMessage).get("message").getValue());

                JSONObject responseJSON = JSON.parse(responseMessage);
                out = responseJSON.get("executeResponse").getValue();
                JSONObject responseNetPlanJSON = responseJSON.get("outputNetPlan").getValue();
                NetPlan responseNetPlan = new NetPlan(responseNetPlanJSON);

                responseNetPlan.setNetworkLayerDefault(responseNetPlan.getNetworkLayer(0));
                mainWindow.getDesign().assignFrom(responseNetPlan);

                } catch (Exception e)
                {
                    throw new Net2PlanException(e.getMessage());
                }
        }

        return out;
	}

	@Override
	public void executionFinished(ThreadExecutionController controller, Object out) 
	{
        try {
            double execTime = (System.nanoTime() - start) / 1e9;
            final VisualizationState vs = mainWindow.getVisualizationState();
            final NetPlan netPlan = mainWindow.getDesign();
    		Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer,Boolean>> res = 
    				vs.suggestCanvasUpdatedVisualizationLayerInfoForNewDesign(new HashSet<> (netPlan.getNetworkLayers()));
    		vs.setCanvasLayerVisibilityAndOrder(netPlan, res.getFirst() , res.getSecond());
            mainWindow.updateVisualizationAfterNewTopology();
            mainWindow.addNetPlanChange();
            String outMessage = String.format("Algorithm executed successfully%nExecution time: %.3g s%nExit message: %s", execTime, out);
            JOptionPane.showMessageDialog(null, outMessage, "Solve design", JOptionPane.PLAIN_MESSAGE);
        } catch (Throwable ex) {
            ErrorHandling.addErrorOrException(ex, OfflineExecutionPanel.class);
            ErrorHandling.showErrorDialog("Error executing algorithm");
        }
	}

	@Override
	public void executionFailed(ThreadExecutionController controller) 
	{
		ErrorHandling.showErrorDialog("Error executing algorithm");
	}

    public void doClickInExecutionButton () { btn_solve.doClick();}
}
