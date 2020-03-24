/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dependency.bo.ResourceManager;

/**
 *
 * @author asus
 */
public final class MessageBox {

    public static final int MESSAGE_ERROR = 1;
    public static final int MESSAGE_INFO = 2;

    MessageBox() {
    }

    public static void showMessageBoxInUIThread(final Component parentComponent, final Object message,
            final int messageType) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showMessageBox(parentComponent, message, messageType);
            }
        });
    }

    private static ImageIcon getImageIconByType(int type) {
        switch (type) {
            case MESSAGE_ERROR:
                return new ImageIcon(ResourceManager.getInstance().getImageByName("error-message.png"));
            case MESSAGE_INFO:
                return new ImageIcon(ResourceManager.getInstance().getImageByName("info-message.png"));
        }
        return null;
    }

    public static void showMessageBox(final Component parentComponent, final Object message, int messageType) {
        JLabel labelMessage = new JLabel(message.toString(), getImageIconByType(messageType), JLabel.HORIZONTAL);
        final JComponent[] components = new JComponent[]{labelMessage};
        JOptionPane.showMessageDialog(parentComponent, components, "Message", JOptionPane.PLAIN_MESSAGE);
    }
}
