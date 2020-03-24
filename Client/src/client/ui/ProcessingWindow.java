/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import javax.swing.SwingUtilities;

import dependency.bean.ChatRequest;
import dependency.bean.ChatResult;
import client.Client;
import client.Client.OnDataReceivedListener;
import dependency.utils.Task;

/**
 *
 * @author asus
 */
public abstract class ProcessingWindow extends Window implements OnDataReceivedListener {

    private int requestCode;

    public ProcessingWindow() {
        if (Client.getInstance() != null) {
            Client.getInstance().addOnDataReceivedListener(this);
        }
    }

    @Override
    protected void onWindowClosing() {
        super.onWindowClosing();
        if (Client.getInstance() != null) {
            Client.getInstance().removeOnDataReceivedListener(this);
        }
    }

    protected abstract void doneBackgoundTask(ChatResult result);

    protected void doInBackground(ChatRequest request) {
        doInBackground(request, "Processing...");
    }

    protected void doInBackground(final ChatRequest request, String processingMessage) {
        this.requestCode = request.getCode();
        setVisible(false);
        ProcessingDialog.showBox(this, processingMessage);
        Task.run(new Runnable() {
            @Override
            public void run() {
                Client.getInstance().request(request);
            }
        });
    }

    @Override
    public boolean onDataReceived(Client sender, final ChatResult receivedObject) {
        if (receivedObject.getRequestCode() == requestCode) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ProcessingDialog.hideBox();
                    doneBackgoundTask(receivedObject);
                }
            });

        }
        return true;
    }
}
