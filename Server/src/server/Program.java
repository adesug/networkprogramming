/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import server.ui.Homechat;

/**
 *
 * @author asus
 */
public final class Program {

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
        }
    }

    public static void main(String[] args) {
        setSystemLookAndFeel();
        new Homechat().setVisible(true);
    }
}
