package com.net2plan.gui.plugins.networkDesign.oaas;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.gui.utils.StringLabeller;
import com.net2plan.gui.utils.WiderJComboBox;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.oaas.ClientUtils;
import com.net2plan.oaas.Net2PlanOaaSClient;
import com.net2plan.utils.Pair;
import com.net2plan.utils.Quadruple;
import com.net2plan.utils.Triple;
import com.shc.easyjson.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * This class construct a panel that can be used to connect some algorithms or reports
 * from a Net2Plan OaaS instance,
 * view description, and configure parameters.
 *
 * @author César San-Nicolás
 * @since 0.7.0
 */

@SuppressWarnings("unchecked")
public class OaaSSelector extends JPanel
{
        private GUINetworkDesign callback;
        private JButton connect;
        private JComboBox catalogSelector, execSelector;
        private JTextField txt_connect;
        private JTextArea txt_description;
        private ParameterValueDescriptionPanel parametersPanel;
        private String label;
        private Net2PlanOaaSClient net2PlanOaaSClient;
        private ClientUtils.ExecutionType type;
        private LoginPanel loginPanel;


        /**
         * Extends the default constructor to connect code from more than one class.
         * @param callback        GUINetworkDesign main instance
         * @param parametersPanel Reference to the panel where parameters can be modified
         * @since 0.2.0
         */
        public OaaSSelector(GUINetworkDesign callback, final ClientUtils.ExecutionType type, final ParameterValueDescriptionPanel parametersPanel)
        {
            this.label = "Connected to: ";
            this.parametersPanel = parametersPanel;
            this.type = type;
            this.loginPanel = new LoginPanel();
            this.callback = callback;

            txt_description = new JTextArea();
            txt_description.setFont(new JLabel().getFont());
            txt_description.setLineWrap(true);
            txt_description.setWrapStyleWord(true);
            txt_description.setEditable(false);

            txt_connect = new JTextField();
            txt_connect.setEditable(false);
            catalogSelector = new WiderJComboBox();
            catalogSelector.addActionListener(e ->
            {
                if (catalogSelector.getItemCount() == 0 || catalogSelector.getSelectedIndex() == -1) return;

                try {
                    execSelector.removeAllItems();
                    txt_description.setText("");
                    parametersPanel.reset();

                    StringLabeller selected = (StringLabeller) catalogSelector.getSelectedItem();
                    JSONArray selectedFiles = (JSONArray) selected.getObject();
                    for(JSONValue sel : selectedFiles)
                    {
                        JSONObject execJSON = sel.getValue();
                        String execType = execJSON.get("type").getValue();
                        if(!execType.equalsIgnoreCase(type.toString()))
                            continue;
                        String name = execJSON.get("name").getValue();
                        String description = execJSON.get("description").getValue();
                        JSONArray parameters = execJSON.get("parameters").getValue();
                        StringLabeller executionLabeller = StringLabeller.unmodifiableOf(Pair.unmodifiableOf(parameters, description), name);
                        execSelector.addItem(executionLabeller);
                    }

                    if(execSelector.getItemCount() > 0)
                    {
                        execSelector.setSelectedIndex(0);
                        StringLabeller execSel = (StringLabeller) execSelector.getSelectedItem();
                        Pair<JSONArray, String> execSelParametersDescriptionPair = (Pair<JSONArray, String>) execSel.getObject();
                        String description = execSelParametersDescriptionPair.getSecond();
                        JSONArray parameters = execSelParametersDescriptionPair.getFirst();

                        txt_description.setText(description);
                        List<Triple<String, String, String>> parametersList = new LinkedList<>();
                        for(JSONValue param : parameters)
                        {
                            JSONObject paramJSON = param.getValue();
                            String paramName = paramJSON.get("name").getValue();
                            String paramDefaultValue = paramJSON.get("defaultValue").getValue();
                            String paramDescription = paramJSON.get("description").getValue();
                            parametersList.add(Triple.unmodifiableOf(paramName, paramDefaultValue, paramDescription));
                        }
                        parametersPanel.setParameters(parametersList);

                    }

                } catch (Throwable ex)
                {
                    throw new OaaSException(ex.getMessage());
                }
            });

            execSelector = new WiderJComboBox();
            execSelector.addActionListener(e ->
            {
                if (execSelector.getItemCount() == 0 || execSelector.getSelectedIndex() == -1) return;

                try {
                    txt_description.setText("");
                    parametersPanel.reset();

                    StringLabeller execSelected = (StringLabeller) execSelector.getSelectedItem();
                    Pair<JSONArray, String> execSelParametersDescriptionPair = (Pair<JSONArray, String>) execSelected.getObject();
                    String description = execSelParametersDescriptionPair.getSecond();
                    JSONArray parameters = execSelParametersDescriptionPair.getFirst();

                    txt_description.setText(description);
                    List<Triple<String, String, String>> parametersList = new LinkedList<>();
                    for(JSONValue param : parameters)
                    {
                        JSONObject paramJSON = param.getValue();
                        String paramName = paramJSON.get("name").getValue();
                        String paramDefaultValue = paramJSON.get("defaultValue").getValue();
                        String paramDescription = paramJSON.get("description").getValue();
                        parametersList.add(Triple.unmodifiableOf(paramName, paramDefaultValue, paramDescription));
                    }
                    parametersPanel.setParameters(parametersList);

                } catch (Throwable ex)
                {
                    throw new OaaSException(ex.getMessage());
                }
            });

            connect = new JButton("Connect");
            connect.addActionListener(e ->
            {
                this.reset();

                int opt = JOptionPane.showConfirmDialog(null, loginPanel, "OaaS Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(opt == JOptionPane.OK_OPTION)
                {
                    Quadruple<String, String, String, String> loginInfo = loginPanel.getLoginInformation();
                    String ip = loginInfo.getFirst();
                    String port = loginInfo.getSecond();
                    String user = loginInfo.getThird();
                    String password = loginInfo.getFourth();

                    callback.configureNet2PlanOaaSClient(ip, Integer.parseInt(port));
                    Net2PlanOaaSClient client = callback.getNet2PlanOaaSClient();
                    Response authResponse = client.authenticateUser(user, password);
                    if (authResponse.getStatus() == 500)
                        throw new RuntimeException();
                    txt_connect.setText("http://" + ip + ":" + port);
                    net2PlanOaaSClient = callback.getNet2PlanOaaSClient();
                    Response getCatalogsResponse = net2PlanOaaSClient.getCatalogs();
                    String resp = getCatalogsResponse.readEntity(String.class);
                    try {
                        JSONObject catalogsJSON = JSON.parse(resp);
                        JSONValue catalogsValue = catalogsJSON.get("catalogs");
                        if (catalogsValue == null)
                            throw new Net2PlanException();
                        JSONArray catalogsArray = catalogsValue.getValue();
                        for (JSONValue cat : catalogsArray)
                        {
                            JSONObject catalogJSON = cat.getValue();
                            String catalogName = catalogJSON.get("name").getValue();
                            JSONArray catalogFiles = catalogJSON.get("files").getValue();
                            StringLabeller catalogLabeller = StringLabeller.unmodifiableOf(catalogFiles, catalogName);
                            catalogSelector.addItem(catalogLabeller);
                        }

                        if (catalogSelector.getItemCount() > 0)
                        {
                            catalogSelector.setSelectedIndex(0);
                            StringLabeller selected = (StringLabeller) catalogSelector.getSelectedItem();
                            JSONArray selectedFiles = (JSONArray) selected.getObject();
                            for (JSONValue sel : selectedFiles)
                            {
                                JSONObject execJSON = sel.getValue();
                                String execType = execJSON.get("type").getValue();
                                if (!execType.equalsIgnoreCase(type.toString()))
                                    continue;
                                String name = execJSON.get("name").getValue();
                                String description = execJSON.get("description").getValue();
                                JSONArray parameters = execJSON.get("parameters").getValue();
                                StringLabeller executionLabeller = StringLabeller.unmodifiableOf(Pair.unmodifiableOf(parameters, description), name);
                                execSelector.addItem(executionLabeller);
                            }

                            if (execSelector.getItemCount() > 0) {
                                execSelector.setSelectedIndex(0);
                                StringLabeller execSel = (StringLabeller) execSelector.getSelectedItem();
                                Pair<JSONArray, String> execSelParametersDescriptionPair = (Pair<JSONArray, String>) execSel.getObject();
                                String description = execSelParametersDescriptionPair.getSecond();
                                JSONArray parameters = execSelParametersDescriptionPair.getFirst();

                                txt_description.setText(description);
                                List<Triple<String, String, String>> parametersList = new LinkedList<>();
                                for (JSONValue param : parameters) {
                                    JSONObject paramJSON = param.getValue();
                                    String paramName = paramJSON.get("name").getValue();
                                    String paramDefaultValue = paramJSON.get("defaultValue").getValue();
                                    String paramDescription = paramJSON.get("description").getValue();
                                    parametersList.add(Triple.unmodifiableOf(paramName, paramDefaultValue, paramDescription));
                                }
                                parametersPanel.setParameters(parametersList);

                            }
                        }

                    } catch (ParseException ex) {
                        throw new RuntimeException(ex.getMessage());
                    }
                }
                else {
                    return;
                }

            });

            setLayout(new MigLayout("", "[][grow][]", "[][][][][grow]"));
            add(new JLabel(label));
            add(txt_connect, "growx");
            add(connect, "wrap");
            //add(new JLabel("Catalogs"));
            add(catalogSelector, "skip, growx, spanx 2, wrap, wmin 100");
            //add(new JLabel("Executions"));
            add(execSelector, "skip, growx, spanx 2, wrap, wmin 100");
            add(new JLabel("Description"), "top");
            add(new JScrollPane(txt_description), "height 100::, spanx 2, grow, wrap");
            add(new JLabel("Parameters"), "spanx 3, wrap");
            add(parametersPanel, "spanx 3, grow");

        }

        @Override
        public void setEnabled(boolean enabled)
        {
            super.setEnabled(enabled);

            catalogSelector.setEnabled(enabled);
            execSelector.setEnabled(enabled);
            parametersPanel.setEnabled(enabled);
        }

        /**
         * Returns the information required to execute an algorithm or report using an OaaS instance
         *
         * @return Execution information
         * @since 0.7.0
         */
        public Pair<ClientUtils.ExecutionType, String> getExecutionInformation()
        {
            return Pair.unmodifiableOf(type, ((StringLabeller)execSelector.getSelectedItem()).getLabel());
        }

        /**
         * Returns the parameters introduced by user.
         *
         * @return Key-value map
         * @since 0.7.0
         */
        public Map<String, String> getRunnableParameters()
        {
            return new LinkedHashMap<>(parametersPanel.getParameters());
        }

        /**
         * Resets the component.
         *
         * @since 0.7.0
         */
        public void reset()
        {
            catalogSelector.removeAllItems();
            execSelector.removeAllItems();
            txt_connect.setText("");
            txt_description.setText("");
            parametersPanel.reset();
        }

        private class LoginPanel extends JPanel
        {
            private JTextField ipField, portField, userField;
            private JPasswordField passwordField;
            public LoginPanel()
            {
                super();
                initialize();
            }

            private void initialize()
            {
                setLayout(new BorderLayout());

                final JPanel middleJPanel = new JPanel(new MigLayout("fill, wrap 2"));


                final JLabel ipLabel = new JLabel("IP Address");
                final JLabel portLabel = new JLabel("Port");
                final JLabel userLabel = new JLabel("User");
                final JLabel passwordLabel = new JLabel("Password");
                ipField = new JTextField();
                ipField.setColumns(20);
                portField = new JTextField();
                portField.setColumns(20);
                portField.setText("8080");
                userField = new JTextField();
                userField.setColumns(20);
                passwordField = new JPasswordField();
                passwordField.setColumns(20);

                final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2"));

                infoPanel.add(ipLabel, "grow");
                infoPanel.add(ipField, "grow");
                infoPanel.add(portLabel, "grow");
                infoPanel.add(portField, "grow");
                infoPanel.add(userLabel, "grow");
                infoPanel.add(userField, "grow");
                infoPanel.add(passwordLabel, "grow");
                infoPanel.add(passwordField, "grow");

                add(infoPanel, BorderLayout.NORTH);
                add(middleJPanel, BorderLayout.CENTER);
            }

            public Quadruple<String, String, String, String> getLoginInformation()
            {
                return Quadruple.unmodifiableOf(ipField.getText(), portField.getText(), userField.getText(), new String(passwordField.getPassword()));
            }
        }

}
