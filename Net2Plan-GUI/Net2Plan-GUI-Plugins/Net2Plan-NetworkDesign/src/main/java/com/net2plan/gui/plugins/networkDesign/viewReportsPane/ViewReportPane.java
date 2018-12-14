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
package com.net2plan.gui.plugins.networkDesign.viewReportsPane;


import com.net2plan.gui.plugins.GUINetworkDesignConstants;
import com.net2plan.gui.plugins.networkDesign.oaas.OaaSSelector;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.gui.plugins.networkDesign.ReportBrowser;
import com.net2plan.gui.utils.RunnableSelector;
import com.net2plan.gui.plugins.networkDesign.ThreadExecutionController;
import com.net2plan.gui.utils.*;
import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.interfaces.networkDesign.Configuration;
import com.net2plan.interfaces.networkDesign.IReport;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.interfaces.networkDesign.NetPlan;
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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.io.File;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ViewReportPane extends JPanel implements ThreadExecutionController.IThreadExecutionHandler
{
	private final GUINetworkDesign mainWindow;
    private RunnableSelector reportSelector;
    private OaaSSelector remoteReportSelector;
    private ThreadExecutionController reportController;
    private JTabbedPane reportContainer, remoteReportContainer;
    private JButton closeAllReports_local, closeAllReports_remote;
    private JSplitPane localSplitPane, remoteSplitPane;
    final JRadioButton localButton, remoteButton;
    private GUINetworkDesignConstants.ExecutionMode mode;
    private JPanel cardPanel;

	public ViewReportPane (GUINetworkDesign mainWindow , int newOrientation)
	{
        setLayout(new MigLayout("insets 0 0 0 0", "[grow]", "[grow]"));

		this.mainWindow = mainWindow;
		this.mode = GUINetworkDesignConstants.ExecutionMode.LOCAL;
		localButton = new JRadioButton("Local");
		remoteButton = new JRadioButton("Remote");
		localSplitPane = new JSplitPane(newOrientation);
		remoteSplitPane = new JSplitPane(newOrientation);
	
        reportController = new ThreadExecutionController(this);

        File REPORTS_DIRECTORY = new File(IGUIModule.CURRENT_DIR + SystemUtils.getDirectorySeparator() + "workspace");
        REPORTS_DIRECTORY = REPORTS_DIRECTORY.isDirectory() ? REPORTS_DIRECTORY : IGUIModule.CURRENT_DIR;
        ParameterValueDescriptionPanel reportParameters = new ParameterValueDescriptionPanel();
        ParameterValueDescriptionPanel remoteReportParameters = new ParameterValueDescriptionPanel();
        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());

        reportSelector = new RunnableSelector("Report", null, IReport.class, REPORTS_DIRECTORY, reportParameters);
        remoteReportSelector = new OaaSSelector(mainWindow, ClientUtils.ExecutionType.REPORT, remoteReportParameters);
        reportContainer = new JTabbedPane();
        remoteReportContainer = new JTabbedPane();

        final JPanel pnl_radio = new JPanel(new MigLayout("", "[center, grow]", "[]"));
        final JPanel pnl_buttons_local = new JPanel(new WrapLayout());
        final JPanel pnl_buttons_remote = new JPanel(new WrapLayout());

        reportContainer.setVisible(false);
        remoteReportContainer.setVisible(false);

        JButton btn_show_local = new JButton("Show");
        btn_show_local.setToolTipText("Show the report");
        btn_show_local.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reportController.execute();
            }
        });

        JButton btn_show_remote = new JButton("Show");
        btn_show_remote.setToolTipText("Show the report");
        btn_show_remote.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reportController.execute();
            }
        });

        closeAllReports_local = new JButton("Close all");
        closeAllReports_local.setToolTipText("Close all reports");
        closeAllReports_local.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reportContainer.removeAll();
            }
        });

        closeAllReports_remote = new JButton("Close all");
        closeAllReports_remote.setToolTipText("Close all reports");
        closeAllReports_remote.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                remoteReportContainer.removeAll();
            }
        });

        reportContainer.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                reportContainer.setVisible(true);
                localSplitPane.setDividerLocation(0.5);

                for (Component component : pnl_buttons_local.getComponents())
                    if (component == closeAllReports_local)
                        return;

                pnl_buttons_local.add(closeAllReports_local);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (reportContainer.getTabCount() == 0) {
                    reportContainer.setVisible(false);

                    for (Component component : pnl_buttons_local.getComponents())
                        if (component == closeAllReports_local)
                            pnl_buttons_local.remove(closeAllReports_local);
                }
            }
        });

        remoteReportContainer.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                remoteReportContainer.setVisible(true);
                remoteSplitPane.setDividerLocation(0.5);

                for (Component component : pnl_buttons_remote.getComponents())
                    if (component == closeAllReports_remote)
                        return;

                pnl_buttons_local.add(closeAllReports_remote);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (remoteReportContainer.getTabCount() == 0) {
                    remoteReportContainer.setVisible(false);

                    for (Component component : pnl_buttons_remote.getComponents())
                        if (component == closeAllReports_remote)
                            pnl_buttons_remote.remove(closeAllReports_remote);
                }
            }
        });

        reportContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tabNumber = reportContainer.getUI().tabForCoordinate(reportContainer, e.getX(), e.getY());

                if (tabNumber >= 0) {
                    Rectangle rect = ((TabIcon) reportContainer.getIconAt(tabNumber)).getBounds();
                    if (rect.contains(e.getX(), e.getY())) reportContainer.removeTabAt(tabNumber);
                }
            }
        });

        remoteReportContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int tabNumber = remoteReportContainer.getUI().tabForCoordinate(remoteReportContainer, e.getX(), e.getY());

                if (tabNumber >= 0) {
                    Rectangle rect = ((TabIcon) remoteReportContainer.getIconAt(tabNumber)).getBounds();
                    if (rect.contains(e.getX(), e.getY())) remoteReportContainer.removeTabAt(tabNumber);
                }
            }
        });

        pnl_buttons_local.add(btn_show_local);
        pnl_buttons_remote.add(btn_show_remote);

        JPanel pane_local = new JPanel(new BorderLayout());
        pane_local.add(reportSelector, BorderLayout.CENTER);
        pane_local.add(pnl_buttons_local, BorderLayout.SOUTH);
        localSplitPane.setTopComponent(pane_local);
        localSplitPane.setBottomComponent(reportContainer);
        localSplitPane.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
        localSplitPane.setResizeWeight(0.5);

        JPanel pane_remote = new JPanel(new BorderLayout());
        pane_remote.add(remoteReportSelector, BorderLayout.CENTER);
        pane_remote.add(pnl_buttons_remote, BorderLayout.SOUTH);
        remoteSplitPane.setTopComponent(pane_remote);
        remoteSplitPane.setBottomComponent(remoteReportContainer);
        remoteSplitPane.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
        remoteSplitPane.setResizeWeight(0.5);


        pnl_radio.add(localButton);
        pnl_radio.add(remoteButton);

        cardPanel.add(pane_local, "Local");
        cardPanel.add(pane_remote, "Remote");

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

        add(pnl_radio, "dock north");
        add(cardPanel, "grow");

        localButton.setSelected(true);
	}
	
	@Override
	public Object execute(ThreadExecutionController controller) 
	{
        Pair<String, ? extends JPanel> aux = Pair.unmodifiableOf("",null);
	    switch(mode)
        {
            case LOCAL:
                Triple<File, String, Class> report = reportSelector.getRunnable();
                Map<String, String> reportParameters = reportSelector.getRunnableParameters();
                Map<String, String> net2planParameters = Configuration.getNet2PlanOptions();
                IReport instance = ClassLoaderUtils.getInstance(report.getFirst(), report.getSecond(), IReport.class , null);
                String title = null;
                try {
                    title = instance.getTitle();
                } catch (UnsupportedOperationException ex) {
                }
                if (title == null) title = "Untitled";

                 aux = Pair.of(title, new ReportBrowser(instance.executeReport(mainWindow.getDesign().copy(), reportParameters, net2planParameters)));
                try {
                    ((Closeable) instance.getClass().getClassLoader()).close();
                } catch (Throwable e) {
                }
                break;

            case REMOTE:
                try {
                    Pair<ClientUtils.ExecutionType, String> execInfo = remoteReportSelector.getExecutionInformation();
                    Map<String, String> execParameters = remoteReportSelector.getRunnableParameters();
                    NetPlan netPlan_copy = mainWindow.getDesign().copy();
                    Net2PlanOaaSClient client = mainWindow.getNet2PlanOaaSClient();
                    String reportName = execInfo.getSecond();
                    Response getReport = client.getReportByName(reportName);
                    if(getReport.getStatus() != 200)
                        throw new Net2PlanException("Report "+reportName+" not found in OaaS instance");

                    String getReportEntity = getReport.readEntity(String.class);
                    JSONObject getReportJSON = JSON.parse(getReportEntity);
                    String reportTitle = getReportJSON.get("title").getValue();
                    Response algorithmResponse = client.executeOperation(execInfo.getFirst(), reportName, execParameters, netPlan_copy);
                    String responseMessage = algorithmResponse.readEntity(String.class);
                    if(algorithmResponse.getStatus() != 200)
                        throw new Net2PlanException(JSON.parse(responseMessage).get("message").getValue());

                    JSONObject responseJSON = JSON.parse(responseMessage);
                    String response = responseJSON.get("executeResponse").getValue();
                    aux = Pair.unmodifiableOf(reportTitle, new ReportBrowser(response));

                } catch (Exception e)
                {
                    throw new Net2PlanException(e.getMessage());
                }
                break;

        }

        return aux;
	}
	@Override
	public void executionFinished(ThreadExecutionController controller, Object out) 
	{
        Pair<String, ? extends JPanel> aux = (Pair<String, ? extends JPanel>) out;
        switch(mode)
        {
            case LOCAL:
                reportContainer.addTab(aux.getFirst(), new TabIcon(TabIcon.IconType.TIMES_SIGN), aux.getSecond());
                reportContainer.setSelectedIndex(reportContainer.getTabCount() - 1);
                break;

            case REMOTE:
                remoteReportContainer.addTab(aux.getFirst(), new TabIcon(TabIcon.IconType.TIMES_SIGN), aux.getSecond());
                remoteReportContainer.setSelectedIndex(remoteReportContainer.getTabCount() - 1);
                break;
        }

	}
	@Override
	public void executionFailed(ThreadExecutionController controller) 
	{
        ErrorHandling.showErrorDialog("Error executing report");
	}

	public JTabbedPane getReportContainer ()
    {
        JTabbedPane cont = null;
        switch(mode)
        {
            case LOCAL:
                cont = reportContainer;
                break;

            case REMOTE:
                cont = remoteReportContainer;
                break;
        }
	    return cont;
    }
}
