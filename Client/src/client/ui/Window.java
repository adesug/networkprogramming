/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import client.Application;
import dependency.bo.ResourceManager;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author asus
 */
public abstract class Window extends JFrame {

    protected abstract void initializeComponents();

    public Window() {
        Application.registerWindow(Window.this);
        setup();
    }

    private void setup() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon(ResourceManager.getInstance().getImageByName("chat-icon.png")).getImage()); 
        initializeComponents();
    }

    @Override
    public void setDefaultCloseOperation(int operation) {
        if (operation != EXIT_ON_CLOSE) {
            super.setDefaultCloseOperation(operation);
        }
    }

    protected void onWindowClosing() {
        Application.unregisterWindow(this);
    }

    @Override
    public void dispose() {
        onWindowClosing();
        super.dispose();
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING && getDefaultCloseOperation() == DISPOSE_ON_CLOSE) {
            onWindowClosing();
        }
        super.processWindowEvent(e);
    }
}
