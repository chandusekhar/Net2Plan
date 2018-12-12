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
import com.net2plan.gui.utils.*;
import com.net2plan.oaas.ClientUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("unchecked")
public class OaaSReportPane extends JSplitPane
{
	private final GUINetworkDesign mainWindow;
    private OaaSSelector reportSelector;
    private JTabbedPane reportContainer;
    private JButton closeAllReports;

	public OaaSReportPane(GUINetworkDesign mainWindow , int newOrientation)
	{
		super (newOrientation);

		this.mainWindow = mainWindow;

        ParameterValueDescriptionPanel reportParameters = new ParameterValueDescriptionPanel();
        reportSelector = new OaaSSelector(mainWindow, ClientUtils.ExecutionType.REPORT, reportParameters);
        reportContainer = new JTabbedPane();

        final JPanel pnl_buttons = new JPanel(new WrapLayout());

        reportContainer.setVisible(false);

        reportContainer.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                reportContainer.setVisible(true);
                setDividerLocation(0.5);

                for (Component component : pnl_buttons.getComponents())
                    if (component == closeAllReports)
                        return;

                pnl_buttons.add(closeAllReports);
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (reportContainer.getTabCount() == 0) {
                    reportContainer.setVisible(false);

                    for (Component component : pnl_buttons.getComponents())
                        if (component == closeAllReports)
                            pnl_buttons.remove(closeAllReports);
                }
            }
        });

        JButton btn_show = new JButton("Show");
        btn_show.setToolTipText("Show the report");
        btn_show.addActionListener(e ->
        {
        });

        closeAllReports = new JButton("Close all");
        closeAllReports.setToolTipText("Close all reports");
        closeAllReports.addActionListener(e -> reportContainer.removeAll());

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

        pnl_buttons.add(btn_show);

        JPanel pane = new JPanel(new BorderLayout());
        pane.add(reportSelector, BorderLayout.CENTER);
        pane.add(pnl_buttons, BorderLayout.SOUTH);
        setTopComponent(pane);

        setBottomComponent(reportContainer);
        addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
        setResizeWeight(0.5);
	}

	public JTabbedPane getReportContainer () { return reportContainer; }
}
