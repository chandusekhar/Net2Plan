package com.net2plan.gui.plugins.networkDesign.oaas;

import com.net2plan.gui.plugins.GUINetworkDesign;
import com.net2plan.gui.utils.ParameterValueDescriptionPanel;
import com.net2plan.gui.utils.StringLabeller;
import com.net2plan.gui.utils.WiderJComboBox;
import com.net2plan.interfaces.networkDesign.Net2PlanException;
import com.net2plan.internal.ErrorHandling;
import com.net2plan.oaas.Net2PlanOaaSClient;
import com.net2plan.utils.Triple;
import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.ParseException;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

/**
 * This class construct a panel that can be used to connect some algorithms or reports
 * from a Net2Plan OaaS instance,
 * view description, and configure parameters.
 *
 * @author César San-Nicolás
 * @since 0.7.0
 */

@SuppressWarnings("unchecked")
public class OaaSSelector extends JLabel
{
        private GUINetworkDesign callback;
        private JButton connect, loadCatalogs;
        private JComboBox catalogSelector, execSelector;
        private JTextField txt_file;
        private JTextArea txt_description;
        private ParameterValueDescriptionPanel parametersPanel;
        private String label;
        private LoginDialog loginDialog;
        private Net2PlanOaaSClient net2PlanOaaSClient;


        /**
         * Extends the default constructor to connect code from more than one class.
         * @param callback        GUINetworkDesign main instance
         * @param parametersPanel Reference to the panel where parameters can be modified
         * @since 0.2.0
         */
        public OaaSSelector(GUINetworkDesign callback, final ParameterValueDescriptionPanel parametersPanel)
        {
            this.label = "Connected to: ";
            this.parametersPanel = parametersPanel;

            this.callback = callback;
            loginDialog = new LoginDialog(callback);

            txt_description = new JTextArea();
            txt_description.setFont(new JLabel().getFont());
            txt_description.setLineWrap(true);
            txt_description.setWrapStyleWord(true);
            txt_description.setEditable(false);

            txt_file = new JTextField();
            txt_file.setEditable(false);
            catalogSelector = new WiderJComboBox();
            catalogSelector.addActionListener(e ->
            {
                if (catalogSelector.getItemCount() == 0 || catalogSelector.getSelectedIndex() == -1) return;

                try {

                } catch (Throwable ex) {
                    ex.printStackTrace();
                    ErrorHandling.showErrorDialog("Error selecting " + label.toLowerCase(getLocale()));
                }
            });

            connect = new JButton("Connect");
            connect.addActionListener(e ->
            {
                loginDialog.setVisible(true);
            });

            loadCatalogs = new JButton("Load Catalogs");
            loadCatalogs.addActionListener(e ->
            {
                net2PlanOaaSClient = callback.getNet2PlanOaaSClient();
                Response getCatalogsResponse = net2PlanOaaSClient.getCatalogs();
                String resp = getCatalogsResponse.readEntity(String.class);
                try {
                    JSONObject respJSON = JSON.parse(resp);
                    System.out.println(JSON.write(respJSON));
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

            });

            setLayout(new MigLayout("", "[][grow][]", "[][][][][grow]"));
            add(new JLabel(label));
            add(txt_file, "growx");
            add(connect, "wrap");
            add(new JLabel("Catalogs"));
            add(txt_file, "growx");
            add(loadCatalogs, "wrap");
            add(catalogSelector, "skip, growx, spanx 2, wrap, wmin 100");
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
            connect.setEnabled(enabled);
            parametersPanel.setEnabled(enabled);
        }

        /**
         * Returns the information required to call a runnable code.
         *
         * @return Runnable information
         * @since 0.2.0
         */
        public Triple<File, String, Class> getRunnable()
        {
            String filename = txt_file.getText();
            if (filename.isEmpty() || catalogSelector.getSelectedIndex() == -1 || execSelector.getSelectedIndex() == -1)
            {
                throw new Net2PlanException(label + " must be selected");
            }

            String algorithm = (String) ((StringLabeller) catalogSelector.getSelectedItem()).getObject();

            return Triple.of(new File(filename), algorithm, this.getClass());
        }

        /**
         * Returns the parameters introduced by user.
         *
         * @return Key-value map
         * @since 0.2.0
         */
        public Map<String, String> getRunnableParameters()
        {
            return new LinkedHashMap<String, String>(parametersPanel.getParameters());
        }

        /**
         * Resets the component.
         *
         * @since 0.2.0
         */
        public void reset()
        {
            catalogSelector.removeAllItems();
            execSelector.removeAllItems();
            txt_file.setText("");
            txt_description.setText("");
            parametersPanel.reset();
        }

        private class LoginDialog extends JDialog
        {
            private GUINetworkDesign callback;
            public LoginDialog(GUINetworkDesign callback)
            {
                this.callback = callback;
                initialize();
            }

            void initialize()
            {
                this.setTitle("OaaS Login");
                this.setModal(true);
                this.setModalityType(ModalityType.APPLICATION_MODAL);
                this.setResizable(false);
                this.setLayout(new BorderLayout());

                final JPanel middleJPanel = new JPanel(new MigLayout("fill, wrap 2"));

                final JButton btn_login = new JButton("Login");
                final JButton btn_cancel = new JButton("Cancel");

                JRootPane rootPane = SwingUtilities.getRootPane(this);
                rootPane.setDefaultButton(btn_login);
                rootPane.registerKeyboardAction(e -> {
                    dispose();
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

                btn_cancel.addActionListener(e -> { setVisible(false); dispose(); } );
                addWindowListener(new WindowAdapter()
                {
                    @Override
                    public void windowClosing(WindowEvent windowEvent)
                    {
                        setVisible(false);
                        dispose();
                    }
                });

                final JLabel ipLabel = new JLabel("IP Address");
                final JLabel portLabel = new JLabel("Port");
                final JLabel userLabel = new JLabel("User");
                final JLabel passwordLabel = new JLabel("Password");
                final JTextField ipField = new JTextField();
                ipField.setColumns(20);
                final JTextField portField = new JTextField();
                portField.setColumns(20);
                portField.setText("8080");
                final JTextField userField = new JTextField();
                userField.setColumns(20);
                final JPasswordField passwordField = new JPasswordField();
                passwordField.setColumns(20);


                btn_login.addActionListener(e ->
                {
                    try
                    {
                        callback.configureNet2PlanOaaSClient(ipField.getText(), Integer.parseInt(portField.getText()));
                        Net2PlanOaaSClient client = callback.getNet2PlanOaaSClient();
                        client.authenticateUser(userField.getText(), new String(passwordField.getPassword()));
                        this.setVisible(false);
                        dispose();
                    }
                    catch (Exception ex)
                    {
                        setVisible(false);
                        dispose();
                        ex.printStackTrace();
                    }
                });

                final JPanel buttonPanel = new JPanel(new MigLayout("fill, wrap 2"));
                final JPanel infoPanel = new JPanel(new MigLayout("fill, wrap 2"));
                buttonPanel.add(btn_login, "grow");
                buttonPanel.add(btn_cancel, "grow");

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
                add(buttonPanel, BorderLayout.SOUTH);
                pack();
                setLocationRelativeTo(SwingUtilities.getRoot(this));
            }
        }

}
