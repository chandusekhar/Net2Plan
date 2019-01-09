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

package com.net2plan.gui.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.net2plan.interfaces.networkDesign.*;
import com.net2plan.oaas.ClientUtils;
import com.net2plan.oaas.Net2PlanOaaSClient;
import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

import com.net2plan.gui.GUINet2Plan;
import com.net2plan.gui.plugins.GUINetworkDesignConstants.AJTableType;
import com.net2plan.gui.plugins.networkDesign.GUIWindow;
import com.net2plan.gui.plugins.networkDesign.NetworkDesignWindow;
import com.net2plan.gui.plugins.networkDesign.focusPane.FocusPane;
import com.net2plan.gui.plugins.networkDesign.interfaces.ITopologyCanvas;
import com.net2plan.gui.plugins.networkDesign.offlineExecPane.OfflineExecutionPanel;
import com.net2plan.gui.plugins.networkDesign.onlineSimulationPane.OnlineSimulationPane;
import com.net2plan.gui.plugins.networkDesign.topologyPane.TopologyPanel;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.CanvasFunction;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.GUILink;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.GUINode;
import com.net2plan.gui.plugins.networkDesign.topologyPane.jung.JUNGCanvas;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.ViewEditTopologyTablesPane;
import com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.controlTables.AdvancedJTable_abstractElement;
import com.net2plan.gui.plugins.networkDesign.viewReportsPane.ViewReportPane;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.PickManager;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.PickManager.PickStateInfo;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.UndoRedoManager;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.gui.plugins.networkDesign.whatIfAnalysisPane.WhatIfAnalysisPane;
import com.net2plan.gui.utils.ProportionalResizeJSplitPaneListener;
import com.net2plan.internal.Constants.NetworkElementType;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.internal.plugins.IGUIModule;
import com.net2plan.internal.plugins.PluginSystem;
import com.net2plan.internal.sim.SimCore.SimState;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Triple;

import net.miginfocom.swing.MigLayout;

/**
 * Targeted to evaluate the network designs generated by built-in or user-defined
 * static planning algorithms, deciding on aspects such as the network topology,
 * the traffic routing, link capacities, protection routes and so on. Algorithms
 * based on constrained optimization formulations (i.e. ILPs) can be fast-prototyped
 * using the open-source Java Optimization Modeler library, to interface
 * to a number of external solvers such as GPLK, CPLEX or IPOPT.
 *
 * @author Pablo
 */
public class GUINetworkDesign extends IGUIModule
{
    private static String TITLE = "Offline network design & Online network simulation";
    private final static int MAXSIZEUNDOLISTCHANGES = 0; // deactivate, not robust yet
    private final static int MAXSIZEUNDOLISTPICK = 10;

    private TopologyPanel topologyPanel;

    private FocusPane focusPanel;

    private ViewEditTopologyTablesPane viewEditTopTables;
    private ViewReportPane reportPane;
    private OfflineExecutionPanel executionPane;
    private OnlineSimulationPane onlineSimulationPane;
    private WhatIfAnalysisPane whatIfAnalysisPane;

    private PickManager pickManager;
    private VisualizationState vs;
    private UndoRedoManager undoRedoManager;

    private NetPlan currentNetPlan;

    private WindowController windowController;
    private GUIWindow tableControlWindow;

    private Net2PlanOaaSClient net2PlanOaaSClient;


    /**
     * Default constructor.
     *
     * @since 0.2.0
     */
    public GUINetworkDesign()
    {
        this(TITLE);
    }


    /**
     * Constructor that allows set a title for the tool in the top section of the panel.
     *
     * @param title Title of the tool (null or empty means no title)
     * @since 0.2.0
     */
    public GUINetworkDesign(String title)
    {
        super(title);
    }

    @Override
    public void start()
    {
        // Default start
        super.start();

        // Additional commands
        this.tableControlWindow.setLocationRelativeTo(this);
        this.tableControlWindow.showWindow(false);
    }

    @Override
    public void stop()
    {
        tableControlWindow.setVisible(false);
        windowController.hideAllWindows();
    }

    @Override
    public void configure(JPanel contentPane)
    {
        // Configuring PluginSystem for this plugin...
        try
        {
            // Add canvas plugin
            PluginSystem.addExternalPlugin(ITopologyCanvas.class);

            /* Add default canvas systems */
            PluginSystem.addPlugin(ITopologyCanvas.class, JUNGCanvas.class);
        } catch (RuntimeException ignored)
        {
            // NOTE: ITopologyCanvas has already been added. Meaning that JUNGCanvas has already been too.
        }

        this.currentNetPlan = new NetPlan();
        
        BidiMap<NetworkLayer, Integer> mapLayer2VisualizationOrder = new DualHashBidiMap<>();
        Map<NetworkLayer, Boolean> layerVisibilityMap = new HashMap<>();
        for (NetworkLayer layer : currentNetPlan.getNetworkLayers())
        {
            mapLayer2VisualizationOrder.put(layer, mapLayer2VisualizationOrder.size());
            layerVisibilityMap.put(layer, true);
        }
        this.vs = new VisualizationState(this, mapLayer2VisualizationOrder, layerVisibilityMap, MAXSIZEUNDOLISTPICK);
        this.pickManager = new PickManager(this, GUINetworkDesignConstants.PICKMANAGER_DEFAULTNUMBERELEMENTSPICKMEMORY);

        topologyPanel = new TopologyPanel(this, JUNGCanvas.class);

        JPanel leftPane = new JPanel(new BorderLayout());
        JPanel logSection = configureLeftBottomPanel();
        if (logSection == null)
        {
            leftPane.add(topologyPanel, BorderLayout.CENTER);
        } else
        {
            JSplitPane splitPaneTopology = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPaneTopology.setTopComponent(topologyPanel);
            splitPaneTopology.setBottomComponent(logSection);
            splitPaneTopology.addPropertyChangeListener(new ProportionalResizeJSplitPaneListener());
            splitPaneTopology.setBorder(new LineBorder(contentPane.getBackground()));
            splitPaneTopology.setOneTouchExpandable(true);
            splitPaneTopology.setDividerSize(7);
            leftPane.add(splitPaneTopology, BorderLayout.CENTER);
        }
        contentPane.add(leftPane, "grow");

        viewEditTopTables = new ViewEditTopologyTablesPane(GUINetworkDesign.this);

        reportPane = new ViewReportPane(GUINetworkDesign.this, JSplitPane.VERTICAL_SPLIT);

        setDesign(currentNetPlan);
        Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> res = VisualizationState.generateCanvasDefaultVisualizationLayerInfo(getDesign());
        vs.setCanvasLayerVisibilityAndOrder(getDesign(), res.getFirst(), res.getSecond());

        /* Initialize the undo/redo manager, and set its initial design */
        this.undoRedoManager = new UndoRedoManager(this, MAXSIZEUNDOLISTCHANGES);
        this.undoRedoManager.addNetPlanChange();

        onlineSimulationPane = new OnlineSimulationPane(this);
        executionPane = new OfflineExecutionPanel(this);
        whatIfAnalysisPane = new WhatIfAnalysisPane(this);

        final JTabbedPane tabPane = new JTabbedPane();
        tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.network), viewEditTopTables);
        tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.offline), executionPane);
        tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.online), onlineSimulationPane);
        tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.whatif), whatIfAnalysisPane);
        tabPane.add(NetworkDesignWindow.getWindowName(NetworkDesignWindow.report), reportPane);

        // Installing customized mouse listener
        MouseListener[] ml = tabPane.getListeners(MouseListener.class);

        for (MouseListener mouseListener : ml)
        {
            tabPane.removeMouseListener(mouseListener);
        }

        // Left click works as usual, right click brings up a pop-up menu.
        tabPane.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                JTabbedPane tabPane = (JTabbedPane) e.getSource();

                int tabIndex = tabPane.getUI().tabForCoordinate(tabPane, e.getX(), e.getY());

                if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex))
                {
                    if (tabIndex == tabPane.getSelectedIndex())
                    {
                        if (tabPane.isRequestFocusEnabled())
                        {
                            tabPane.requestFocus();

                            tabPane.repaint(tabPane.getUI().getTabBounds(tabPane, tabIndex));
                        }
                    } else
                    {
                        tabPane.setSelectedIndex(tabIndex);
                    }

                    if (!tabPane.isEnabled() || SwingUtilities.isRightMouseButton(e))
                    {
                        final JPopupMenu popupMenu = new JPopupMenu();

                        final JMenuItem popWindow = new JMenuItem("Pop window out");
                        popWindow.addActionListener(e1 ->
                        {
                            final int selectedIndex = tabPane.getSelectedIndex();
                            final String tabName = tabPane.getTitleAt(selectedIndex);

                            // Pops up the selected tab.
                            final NetworkDesignWindow networkDesignWindow = NetworkDesignWindow.parseString(tabName);

                            if (networkDesignWindow != null)
                            {
                                switch (networkDesignWindow)
                                {
                                    case offline:
                                        windowController.showOfflineWindow(true);
                                        break;
                                    case online:
                                        windowController.showOnlineWindow(true);
                                        break;
                                    case whatif:
                                        windowController.showWhatifWindow(true);
                                        break;
                                    case report:
                                        windowController.showReportWindow(true);
                                        break;
                                    default:
                                        return;
                                }
                            }

                            tabPane.setSelectedIndex(0);
                        });

                        // Disabling the pop up button for the network state tab.
                        if (NetworkDesignWindow.parseString(tabPane.getTitleAt(tabPane.getSelectedIndex())) == NetworkDesignWindow.network)
                        {
                            popWindow.setEnabled(false);
                        }

                        popupMenu.add(popWindow);

                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Building windows
        this.tableControlWindow = new GUIWindow(tabPane)
        {
            @Override
            public String getTitle()
            {
                return "Net2Plan - Design tables and control window";
            }
        };

        // Building tab controller
        this.windowController = new WindowController(executionPane, onlineSimulationPane, whatIfAnalysisPane, reportPane);

        addKeyCombinationActions();
        updateVisualizationAfterNewTopology();
    }

    public PickManager getPickManager () { return this.pickManager; }
    
    private JPanel configureLeftBottomPanel()
    {
        this.focusPanel = new FocusPane(this);
        final JPanel focusPanelContainer = new JPanel(new BorderLayout());
        final JToolBar navigationToolbar = new JToolBar(JToolBar.VERTICAL);
        navigationToolbar.setRollover(true);
        navigationToolbar.setFloatable(false);
        navigationToolbar.setOpaque(false);

        final JScrollPane scPane = new JScrollPane(focusPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scPane.getVerticalScrollBar().setUnitIncrement(20);
        scPane.getHorizontalScrollBar().setUnitIncrement(20);
        scPane.setBorder(BorderFactory.createEmptyBorder());

        // Control the scroll
        scPane.getHorizontalScrollBar().addAdjustmentListener(e ->
        {
            // Repaints the panel each time the horizontal scroll bar is moves, in order to avoid ghosting.
            focusPanelContainer.revalidate();
            focusPanelContainer.repaint();
        });

        focusPanelContainer.add(navigationToolbar, BorderLayout.WEST);
        focusPanelContainer.add(scPane, BorderLayout.CENTER);

        JPanel pane = new JPanel(new MigLayout("fill, insets 0 0 0 0"));
        pane.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.BLACK), "Focus panel"));

        pane.add(focusPanelContainer, "grow");
        return pane;
    }

    @Override
    public String getDescription()
    {
        return getName();
    }

    @Override
    public KeyStroke getKeyStroke()
    {
        return KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK);
    }

    @Override
    public String getMenu()
    {

        return "Tools|" + TITLE;
    }

    @Override
    public String getName()
    {
        return TITLE + " (GUI)";
    }

    @Override
    public List<Triple<String, String, String>> getParameters()
    {
        return null;
    }

    @Override
    public int getPriority()
    {
        return Integer.MAX_VALUE;
    }


    public NetPlan getDesign()
    {
        if (inOnlineSimulationMode()) return onlineSimulationPane.getSimKernel().getCurrentNetPlan();
        else return currentNetPlan;
    }

    public NetPlan getInitialDesign()
    {
        if (inOnlineSimulationMode()) return onlineSimulationPane.getSimKernel().getInitialNetPlan();
        else return null;
    }

    public WhatIfAnalysisPane getWhatIfAnalysisPane()
    {
        return whatIfAnalysisPane;
    }

    public void addNetPlanChange()
    {
        undoRedoManager.addNetPlanChange();
    }

    public void requestUndoAction()
    {
        if (inOnlineSimulationMode()) return;

        final Triple<NetPlan, Map<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> back = undoRedoManager.getNavigationBackElement();
        if (back == null) return;
        this.currentNetPlan = back.getFirst();
        this.vs.setCanvasLayerVisibilityAndOrder(this.currentNetPlan, back.getSecond(), back.getThird());
        updateVisualizationAfterNewTopology();
    }

    public void requestRedoAction()
    {
        if (inOnlineSimulationMode()) return;

        final Triple<NetPlan, Map<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> forward = undoRedoManager.getNavigationForwardElement();
        if (forward == null) return;
        this.currentNetPlan = forward.getFirst();
        this.vs.setCanvasLayerVisibilityAndOrder(this.currentNetPlan, forward.getSecond(), forward.getThird());
        updateVisualizationAfterNewTopology();
    }

    public void setDesign(NetPlan netPlan)
    {
        if (ErrorHandling.isDebugEnabled()) netPlan.checkCachesConsistency();
        this.currentNetPlan = netPlan;
    }


    public VisualizationState getVisualizationState()
    {
        return vs;
    }

    public void showTableControlWindow()
    {
        tableControlWindow.showWindow(true);
    }

    private void resetButton()
    {
        try
        {
            final int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset? This will remove all unsaved data", "Reset", JOptionPane.YES_NO_OPTION);
            if (result != JOptionPane.YES_OPTION) return;

            if (inOnlineSimulationMode())
            {
                switch (onlineSimulationPane.getSimKernel().getSimCore().getSimulationState())
                {
                    case NOT_STARTED:
                    case STOPPED:
                        break;
                    default:
                        onlineSimulationPane.getSimKernel().getSimCore().setSimulationState(SimState.STOPPED);
                        break;
                }
                onlineSimulationPane.getSimKernel().reset();
                setDesign(onlineSimulationPane.getSimKernel().getCurrentNetPlan());
            } else
            {
                setDesign(new NetPlan());
                //algorithmSelector.reset();
                executionPane.reset();
            }
//            reportSelector.reset();
//            reportContainer.removeAll();
        } catch (Throwable ex)
        {
            ErrorHandling.addErrorOrException(ex, GUINetworkDesign.class);
            ErrorHandling.showErrorDialog("Unable to reset");
        }
        Pair<BidiMap<NetworkLayer, Integer>, Map<NetworkLayer, Boolean>> res = VisualizationState.generateCanvasDefaultVisualizationLayerInfo(getDesign());
        vs.setCanvasLayerVisibilityAndOrder(getDesign(), res.getFirst(), res.getSecond());
        updateVisualizationAfterNewTopology();
        undoRedoManager.addNetPlanChange();
    }

    public void resetPickedStateAndUpdateView()
    {
        pickManager.reset();
        topologyPanel.getCanvas().cleanSelection();
        viewEditTopTables.resetPickedState();
        focusPanel.reset();
    }

    /**
     * Indicates whether or not the initial {@code NetPlan} object is stored to be
     * compared with the current one (i.e. after some simulation steps).
     *
     * @return {@code true} if the initial {@code NetPlan} object is stored. Otherwise, {@code false}.
     * @since 0.3.0
     */
    public boolean inOnlineSimulationMode()
    {
        if (onlineSimulationPane == null) return false;
        final SimState simState = onlineSimulationPane.getSimKernel().getSimCore().getSimulationState();
        return simState == SimState.PAUSED || simState == SimState.RUNNING || simState == SimState.STEP;
    }

    private void addKeyCombinationActions()
    {
        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Resets the tool", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                resetButton();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));

        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Outputs current design to console", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println(getDesign().toString());
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_DOWN_MASK));

        /* FROM THE OFFLINE ALGORITHM EXECUTION */

        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Execute algorithm", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                executionPane.doClickInExecutionButton();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));

        /* FROM REPORT */
        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Close selected report (Local)", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int tab = reportPane.getReportContainer().getSelectedIndex();
                if (tab == -1) return;
                reportPane.getReportContainer().remove(tab);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));


        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Close all reports (Local)", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reportPane.getReportContainer().removeAll();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));


        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Close selected report (Remote)", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int tab = reportPane.getRemoteReportContainer().getSelectedIndex();
                if (tab == -1) return;
                reportPane.getRemoteReportContainer().remove(tab);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));


        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Close all reports (Remote)", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                reportPane.getRemoteReportContainer().removeAll();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));


        /* Online simulation */
        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW , "Run simulation", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    if (onlineSimulationPane.isRunButtonEnabled()) onlineSimulationPane.runSimulation(false);
                } catch (Net2PlanException ex)
                {
                    if (ErrorHandling.isDebugEnabled())
                        ErrorHandling.addErrorOrException(ex, OnlineSimulationPane.class);
                    ErrorHandling.showErrorDialog(ex.getMessage(), "Error executing simulation");
                } catch (Throwable ex)
                {
                    ErrorHandling.addErrorOrException(ex, OnlineSimulationPane.class);
                    ErrorHandling.showErrorDialog("An error happened");
                }

            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        
        /* Pick navigator */
        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW,"Pick previous element picked", new AbstractAction()
        {
        	@Override
        	public void actionPerformed(ActionEvent e)
        	{
        		final PickStateInfo backOrForwardNewNp = pickManager.getPickNavigationBackElement(getDesign ()).orElse(null);
                if (backOrForwardNewNp == null) return;
                else backOrForwardNewNp.applyVisualizationInCurrentDesign();
                GUINetworkDesign.this.updateVisualizationAfterPick();
        	}
        }, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_MASK));
        
        addKeyCombinationAction(JComponent.WHEN_IN_FOCUSED_WINDOW,"Pick next element picked", new AbstractAction()
        {
        	@Override
        	public void actionPerformed(ActionEvent e)
        	{
        		final PickStateInfo backOrForwardNewNp = pickManager.getPickNavigationForwardElement (getDesign ()).orElse(null);
                if (backOrForwardNewNp == null) return;
                else backOrForwardNewNp.applyVisualizationInCurrentDesign();
                GUINetworkDesign.this.updateVisualizationAfterPick();
        	}
        }, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_MASK));

        // Windows
        addKeyCombinationAction(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT , "Show control window", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tableControlWindow.showWindow(true);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK));

        GUINet2Plan.addGlobalActions(this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), this.getActionMap());

        viewEditTopTables.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
        viewEditTopTables.setActionMap(this.getActionMap());

        reportPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
        reportPane.setActionMap(this.getActionMap());

        executionPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
        executionPane.setActionMap(this.getActionMap());

        onlineSimulationPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
        onlineSimulationPane.setActionMap(this.getActionMap());

        whatIfAnalysisPane.setInputMap(WHEN_IN_FOCUSED_WINDOW, this.getInputMap(WHEN_IN_FOCUSED_WINDOW));
        whatIfAnalysisPane.setActionMap(this.getActionMap());
    }

    public void putTransientColorInElementTopologyCanvas(Collection<? extends NetworkElement> linksAndNodes, Color color)
    {
        for (NetworkElement e : linksAndNodes)
        {
            if (e instanceof Link)
            {
                final GUILink gl = vs.getCanvasAssociatedGUILink((Link) e);
                if (gl != null)
                {
                    gl.setEdgeDrawPaint(color);
                }
            } else if (e instanceof Node)
            {
                for (GUINode gn : vs.getCanvasVerticallyStackedGUINodes((Node) e))
                {
                    gn.setBorderPaint(color);
                    gn.setFillPaint(color);
                }
            } else throw new RuntimeException();
        }

        resetPickedStateAndUpdateView();
    }
   	
    public void updateVisualizationAfterPick()
    {
        final PickStateInfo pick = pickManager.getCurrentPick(getDesign ()).orElse(null);
        if (pick == null) 
        	pickManager.reset(); 
        else
        {
        	pick.applyVisualizationInCurrentDesign();
            final Pair<NetworkElementType,NetworkLayer> typeAndLayerInfo = pick.getElementTypeOfMainElement().orElse(null);
            if (typeAndLayerInfo != null)
            	viewEditTopTables.selectTabAndGivenItems(typeAndLayerInfo.getFirst(), typeAndLayerInfo.getSecond() , pick.getStateOnlyNeFr());
        }
        topologyPanel.getCanvas().refresh(); // needed with or w.o. pick, since maybe you unpick with an undo
        topologyPanel.updateTopToolbar();
        focusPanel.updateView();
    }

    public void updateVisualizationAfterChanges()
    {
        topologyPanel.updateMultilayerPanel();
        topologyPanel.getCanvas().rebuildGraph();
        viewEditTopTables.updateView();
        focusPanel.updateView();
    }

    public void updateVisualizationAfterNewTopology()
    {
    	vs.setCanvasLayerVisibilityAndOrder(this.getDesign(), null, null);
        vs.updateTableRowFilter(null, null);
        topologyPanel.updateMultilayerPanel();
        topologyPanel.getCanvas().rebuildGraph();
        topologyPanel.getCanvas().zoomAll();
        viewEditTopTables.updateView();
        focusPanel.updateView();
    }

    public void updateVisualizationAfterCanvasState()
    {
        topologyPanel.updateTopToolbar();
    }

    public void clearFocusPanel()
    {
        focusPanel.reset();
    }

    public void updateVisualizationJustCanvasLinkNodeVisibilityOrColor()
    {
        topologyPanel.getCanvas().refresh();
    }

    public void runCanvasOperation(CanvasFunction operation)
    {
        switch (operation)
        {
            case ZOOM_ALL:
                topologyPanel.getCanvas().zoomAll();
                break;
            case ZOOM_IN:
                topologyPanel.getCanvas().zoomIn();
                break;
            case ZOOM_OUT:
                topologyPanel.getCanvas().zoomOut();
                break;
        }
    }

    public void updateVisualizationJustTables()
    {
        viewEditTopTables.updateView();
    }

    private class WindowController
    {
        private GUIWindow reportWindow;
        private GUIWindow offlineWindow;
        private GUIWindow onlineWindow;
        private GUIWindow whatifWindow;
        private GUIWindow oaasAlgorithmsWindow;
        private GUIWindow oaasReportsWindow;

        private final JComponent offlineWindowComponent, onlineWindowComponent;
        private final JComponent whatitWindowComponent, reportWindowComponent;

        WindowController(final JComponent offlineWindowComponent,
                         final JComponent onlineWindowComponent, final JComponent whatifWindowComponent,
                         final JComponent reportWindowComponent)
        {

            this.offlineWindowComponent = offlineWindowComponent;
            this.onlineWindowComponent = onlineWindowComponent;
            this.whatitWindowComponent = whatifWindowComponent;
            this.reportWindowComponent = reportWindowComponent;
        }

        private void buildOfflineWindow(final JComponent component)
        {
            final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.offline);

            offlineWindow = new GUIWindow(component)
            {
                @Override
                public String getTitle()
                {
                    return "Net2Plan - " + tabName;
                }
            };

            offlineWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
        }

        void showOfflineWindow(final boolean gainFocus)
        {
            buildOfflineWindow(offlineWindowComponent);

            if (offlineWindow != null)
            {
                offlineWindow.showWindow(gainFocus);
                offlineWindow.setLocationRelativeTo(tableControlWindow);
            }
        }


        private void buildOnlineWindow(final JComponent component)
        {
            final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.online);

            onlineWindow = new GUIWindow(component)
            {
                @Override
                public String getTitle()
                {
                    return "Net2Plan - " + tabName;
                }
            };

            onlineWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
        }

        void showOnlineWindow(final boolean gainFocus)
        {
            buildOnlineWindow(onlineWindowComponent);

            if (onlineWindow != null)
            {
                onlineWindow.showWindow(gainFocus);
                onlineWindow.setLocationRelativeTo(tableControlWindow);
            }
        }

        private void buildWhatifWindow(final JComponent component)
        {
            final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.whatif);

            whatifWindow = new GUIWindow(component)
            {
                @Override
                public String getTitle()
                {
                    return "Net2Plan - " + tabName;
                }
            };

            whatifWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
        }

        void showWhatifWindow(final boolean gainFocus)
        {
            buildWhatifWindow(whatitWindowComponent);
            if (whatifWindow != null)
            {
                whatifWindow.showWindow(gainFocus);
                whatifWindow.setLocationRelativeTo(tableControlWindow);
            }
        }

        private void buildReportWindow(final JComponent component)
        {
            final String tabName = NetworkDesignWindow.getWindowName(NetworkDesignWindow.report);

            reportWindow = new GUIWindow(component)
            {
                @Override
                public String getTitle()
                {
                    return "Net2Plan - " + tabName;
                }
            };

            reportWindow.addWindowListener(new CloseWindowAdapter(tabName, component));
        }

        void showReportWindow(final boolean gainFocus)
        {
            buildReportWindow(reportWindowComponent);
            if (reportWindow != null)
            {
                reportWindow.showWindow(gainFocus);
                reportWindow.setLocationRelativeTo(tableControlWindow);
            }
        }


        void hideAllWindows()
        {
            if (offlineWindow != null)
                offlineWindow.dispatchEvent(new WindowEvent(offlineWindow, WindowEvent.WINDOW_CLOSING));
            if (onlineWindow != null)
                onlineWindow.dispatchEvent(new WindowEvent(onlineWindow, WindowEvent.WINDOW_CLOSING));
            if (whatifWindow != null)
                whatifWindow.dispatchEvent(new WindowEvent(whatifWindow, WindowEvent.WINDOW_CLOSING));
            if (reportWindow != null)
                reportWindow.dispatchEvent(new WindowEvent(reportWindow, WindowEvent.WINDOW_CLOSING));
        }

        private class CloseWindowAdapter extends WindowAdapter
        {
            private final String tabName;
            private final JComponent component;

            private final NetworkDesignWindow[] tabCorrectOrder =
                    {NetworkDesignWindow.network, NetworkDesignWindow.offline, NetworkDesignWindow.online, NetworkDesignWindow.whatif, NetworkDesignWindow.report};

            CloseWindowAdapter(final String tabName, final JComponent component)
            {
                this.tabName = tabName;
                this.component = component;
            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                addTabToControlWindow(tabName, component);
            }

            private void addTabToControlWindow(final String newTabName, final JComponent newTabComponent)
            {
                final JTabbedPane tabPane = (JTabbedPane) tableControlWindow.getInnerComponent();

                final Map<String, Component> toSortTabs = new HashMap<>();
                toSortTabs.put(newTabName, newTabComponent);

                for (int i = 0; i < tabPane.getTabCount(); i = 0)
                {
                    toSortTabs.put(tabPane.getTitleAt(i), tabPane.getComponentAt(i));
                    tabPane.remove(i);
                }

                for (int i = 0; i < tabCorrectOrder.length; i++)
                {
                    final String tabName = NetworkDesignWindow.getWindowName(tabCorrectOrder[i]);

                    if (toSortTabs.containsKey(tabName))
                    {
                        final Component tabComponent = toSortTabs.get(tabName);

                        tabPane.addTab(tabName, tabComponent);
                    }
                }
            }
        }
    }

    public SortedSet<?> getSelectedElements (AJTableType tableType , NetworkLayer layer)
    {
        final AdvancedJTable_abstractElement table = viewEditTopTables.getNetPlanViewTable(layer).get(tableType);
        if (table == null) return new TreeSet<> ();
        return table.getSelectedElements();
    }

    public void configureNet2PlanOaaSClient(ClientUtils.ClientMode mode, String ipAddress, int port)
    {
        net2PlanOaaSClient = new Net2PlanOaaSClient(mode, ipAddress, port);
    }

    public Net2PlanOaaSClient getNet2PlanOaaSClient()
    {
        return net2PlanOaaSClient;
    }

}
