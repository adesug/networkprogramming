/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import dependency.bo.ResourceManager;
import client.Application;
import client.Client;
import dependency.utils.Task;
import dependency.view.FitImageJLabel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 *
 * @author asus
 */
public class ConnectionWindow extends Window implements ActionListener {

    private JTextField portField;
    private JTextField ipField;
    private OnCreatedClientListener mOnCreatedClientListener = null;

    public void setOnCreatedClientListener(OnCreatedClientListener listener) {
        mOnCreatedClientListener = listener;
    }

    @Override
    protected void initializeComponents() {
        setResizable(false);
        getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Address:");
        lblNewLabel.setBounds(384, 85, 43, 23);
        getContentPane().add(lblNewLabel);

        ipField = new JTextField();
        ipField.setBounds(437, 85, 99, 23);
        ipField.setText("localhost");
        ipField.addActionListener(this);
        getContentPane().add(ipField);
        ipField.setColumns(10);

        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(384, 119, 43, 23);
        getContentPane().add(lblPort);

        portField = new JTextField();
        portField.setBounds(437, 119, 99, 23);
        portField.setText("3393");
        portField.addActionListener(this);
        getContentPane().add(portField);
        portField.setColumns(10);

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBounds(437, 153, 99, 23);
        btnConnect.addActionListener(this);
        getContentPane().add(btnConnect);

        FitImageJLabel bannerField = new FitImageJLabel();
        bannerField.setBounds(10, 11, 346, 242);
        bannerField.setIcon(new ImageIcon(ResourceManager.getInstance().getImageByName("connection-banner.png")));
        getContentPane().add(bannerField);

        JLabel copyrightField = new JLabel("Client-Server Application");
        copyrightField.setForeground(Color.GRAY);
        copyrightField.setBounds(10, 343, 172, 14);
        getContentPane().add(copyrightField);

        setSize(560, 397);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (checkInput()) {
            startConnect();
        } else {
            JOptionPane.showMessageDialog(ConnectionWindow.this, "Re-enter IP address or port number!");
        }
    }

    private boolean checkInput() {
        String port = portField.getText();
        String ip = ipField.getText();
        try {
            int portNumber = Integer.parseInt(port);
            if (portNumber <= 0) {
                return false;
            }
            return ip != null && ip.length() > 0;
        } catch (NumberFormatException e) {
        }
        return false;
    }

    public ConnectionWindow() {
        setTitle("Welcome to Simple Chat");
    }

    @Override
    protected void onWindowClosing() {
        super.onWindowClosing();
        Application.exitIfNotWindowActived();
    }

    private void startConnect() {
        ProcessingDialog.showBox(this, "Connecting...");
        Task.run(new Runnable() {
            @Override
            public void run() {
                try {
                    Client.createInstance(ipField.getText(), Integer.parseInt(portField.getText()));
                    if (mOnCreatedClientListener != null) {
                        mOnCreatedClientListener.onCreatedClient(Client.getInstance());
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Application.showWindow(LoginWindow.class);
                            dispose();
                        }
                    });
                } catch (UnknownHostException e) {
                    MessageBox.showMessageBoxInUIThread(ConnectionWindow.this, "Has problem: " + e.getMessage(), MessageBox.MESSAGE_ERROR);
                } catch (NumberFormatException ex) {
                    MessageBox.showMessageBoxInUIThread(ConnectionWindow.this, "Has problem: " + ex.getMessage(), MessageBox.MESSAGE_ERROR);
                } catch (IOException exx) {
                    MessageBox.showMessageBoxInUIThread(ConnectionWindow.this, "Has problem: " + exx.getMessage(), MessageBox.MESSAGE_ERROR);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ProcessingDialog.hideBox();
                    }
                });
            }
        });
    }

    public interface OnCreatedClientListener {

        void onCreatedClient(Client client);
    }
}
