/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import dependency.bean.AccountInfo;
import dependency.bo.ResourceManager;
import client.ui.ChatWindow;
import client.ui.ConnectionWindow;
import client.ui.MessageBox;
import client.ui.Window;

/**
 *
 * @author asus
 */
public final class Application {

    private static List<Window> windows = new LinkedList<>();

    public static void exitIfNotWindowActived() {
        if (windows.isEmpty()) {
            exit();
        }
    }

    public static void exit() {
        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);
            window.dispose();
        }
        Client.destroyInstance();
        ResourceManager.destroyInstance();
        System.exit(0);
    }

    public static void registerWindow(Window window) {
        windows.add(window);
    }

    public static void unregisterWindow(Window window) {
        windows.remove(window);
    }

    public static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
        }
    }

    public static void run() {
        ((ConnectionWindow) Application.showWindow(ConnectionWindow.class))
                .setOnCreatedClientListener(new ConnectionWindowAdapter());
    }

    public static Window showWindow(Class<? extends Window> clazz) {
        Window existsWindow = null;
        for (Window window : windows) {
            if (window.getClass().equals(clazz)) {
                existsWindow = window;
                break;
            }
        }
        if (existsWindow == null) {
            try {
                existsWindow = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
            }
            if (existsWindow != null) {
                existsWindow.setLocationRelativeTo(null);
                existsWindow.setVisible(true);
            }
        }
        if (existsWindow != null) {
            existsWindow.toFront();
        }
        return existsWindow;
    }

    public static ChatWindow showChatWindow(AccountInfo who) {
        ChatWindow chatWindow;
        for (Window window : windows) {
            if (window instanceof ChatWindow) {
                chatWindow = (ChatWindow) window;
                if (chatWindow.getAccountId() == who.getAccountId()) {
                    chatWindow.requestFocus();
                    return chatWindow;
                }
            }
        }
        chatWindow = new ChatWindow(who);
        chatWindow.setLocationRelativeTo(null);
        chatWindow.setVisible(true);
        return chatWindow;
    }

    public static void closeAllWindows() {
        while (windows.size() > 0) {
            Window window = windows.get(0);
            window.dispose();
        }
    }

    private static class ConnectionWindowAdapter implements ConnectionWindow.OnCreatedClientListener {

        @Override
        public void onCreatedClient(Client client) {
            client.setOnConnectionHasProblemListener(new Client.OnConnectionHasProblemListener() {
                @Override
                public void onConnectionHasProblem(String message) {
                    closeAllWindows();
                    MessageBox.showMessageBoxInUIThread(null, "Connection has problem: " + message, MessageBox.MESSAGE_ERROR);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ((ConnectionWindow) Application.showWindow(ConnectionWindow.class))
                                    .setOnCreatedClientListener(new ConnectionWindowAdapter());
                        }
                    });

                }
            });
            client.startLooper();
        }
    }
}
