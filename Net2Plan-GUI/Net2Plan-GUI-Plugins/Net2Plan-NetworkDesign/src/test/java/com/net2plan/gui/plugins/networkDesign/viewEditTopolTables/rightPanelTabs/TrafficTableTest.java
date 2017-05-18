package com.net2plan.gui.plugins.networkDesign.viewEditTopolTables.rightPanelTabs;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.plugins.networkDesign.visualizationControl.VisualizationState;
import com.net2plan.interfaces.networkDesign.Demand;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.interfaces.networkDesign.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jorge San Emeterio
 * @date 16/05/17
 */
public class TrafficTableTest
{
    private static GUINetworkDesign callback = mock(GUINetworkDesign.class);
    private static VisualizationState vs = mock(VisualizationState.class);

    private static NetPlan netPlan;

    private static NetPlanViewTableComponent_trafficMatrix component;
    private static JTable trafficTable;

    private static Demand exampleDemand;

    @BeforeClass
    public static void setUp()
    {
        // NetPlan
        netPlan = new NetPlan();

        final Node node0 = netPlan.addNode(0, 0, "Node 0", null);
        final Node node1 = netPlan.addNode(0, 0, "Node 1", null);
        final Node node2 = netPlan.addNode(0, 0, "Node 2", null);

        netPlan.addDemand(node0, node1, 1, null);
        netPlan.addDemand(node0, node1, 2, null);
        netPlan.addDemand(node1, node0, 3, null);
        netPlan.addDemand(node0, node2, 4, null);

        exampleDemand = netPlan.addDemand(node1, node2, 5, null);

        // Mock
        when(callback.getDesign()).thenReturn(netPlan);
        when(callback.getVisualizationState()).thenReturn(vs);
        when(vs.isWhatIfAnalysisActive()).thenReturn(false);

        component = new NetPlanViewTableComponent_trafficMatrix(callback);
        trafficTable = component.getTable();
    }

    @Test
    public void tableSizeTest()
    {
        assertThat(trafficTable.getRowCount()).isEqualTo(4);
        assertThat(trafficTable.getColumnCount()).isEqualTo(5);
    }

    @Test
    public void aggregationCellsEditTest()
    {
        for (int i = 0; i < trafficTable.getColumnCount(); i++)
            assertThat(trafficTable.isCellEditable(trafficTable.getRowCount() - 1, i)).isFalse();

        for (int i = 0; i < trafficTable.getRowCount(); i++)
            assertThat(trafficTable.isCellEditable(i, trafficTable.getColumnCount() - 1)).isFalse();
    }

    @Test
    public void nodeColumnEditTest()
    {
        for (int i = 0; i < trafficTable.getRowCount(); i++)
            assertThat(trafficTable.isCellEditable(i, 0)).isFalse();
    }

    @Test
    public void diagonalCellTest()
    {
        for (int i = 1; i < trafficTable.getRowCount() - 1; i++)
        {
            assertThat(trafficTable.isCellEditable(i, i + 1)).isFalse();
            assertThat(trafficTable.getValueAt(i, i + 1)).isEqualTo(0d);
        }
    }

    @Test
    public void multipleDemandCellEditTest()
    {
        assertThat(trafficTable.isCellEditable(0, 2)).isFalse();
    }

    @Test
    public void demandEditTest()
    {
        double offeredTraffic = 100d;
        trafficTable.setValueAt(offeredTraffic, 1, 3);

        assertThat(exampleDemand.getOfferedTraffic()).isEqualTo(offeredTraffic);
    }

    @Test
    public void getTrafficMatrixTest()
    {
        final double[][] trafficMatrix = component.getTrafficMatrix();

        // Is square
        for (int i = 0; i < trafficMatrix.length; i++)
            assertThat(trafficMatrix[i].length).isEqualTo(trafficMatrix.length);

        // Size
        assertThat(trafficMatrix.length).isEqualTo(netPlan.getNumberOfNodes());

        // Content
        for (int i = 0; i < trafficMatrix.length; i++)
        {
            for (int j = 0; j < trafficMatrix[i].length; j++)
            {
                assertThat(trafficMatrix[i][j]).isInstanceOf(Double.class);

                final double offTraffic = netPlan.getNodePairDemands(netPlan.getNode(i), netPlan.getNode(j), false)
                        .stream()
                        .mapToDouble(e -> e.getOfferedTraffic())
                        .sum();

                assertThat(trafficMatrix[i][j]).isEqualTo(offTraffic);
            }
        }
    }
}