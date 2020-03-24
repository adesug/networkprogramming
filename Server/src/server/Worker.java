/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.Socket;

import dependency.bean.AccountInfo;
import dependency.bean.ChatRequest;
import dependency.bean.ChatResult;
import dependency.bo.ITransmission;
import dependency.bo.ObjectAdapter;
import dependency.bo.Protocol;
import dependency.bo.SocketTransmission;
import dependency.utils.StreamUtilities;

/**
 *
 * @author asus
 */
public class Worker {

    private AccountInfo myAccount;
    private final ITransmission transmission;
    private final ObjectAdapter objectAdapter;
    private final Protocol protocol;
    private OnRequestReceivedListener mOnRequestReceivedListener;
    private OnAuthenticatedListener mOnAuthenticatedListener;

    public void setOnReceivedDataListener(OnRequestReceivedListener listener) {
        mOnRequestReceivedListener = listener;
    }

    public void setOnAuthenticatedListener(OnAuthenticatedListener listener) {
        mOnAuthenticatedListener = listener;
    }

    public void response(ChatResult result) throws IOException {
        protocol.sendObject(result);
    }

    public void startBridge() throws IOException {
        while (true) {
            Object receivedObject = protocol.receiveObject();
            if (receivedObject == null) {
                break;
            }
            if (receivedObject instanceof ChatRequest) {
                if (mOnRequestReceivedListener != null) {
                    response(mOnRequestReceivedListener.onRequestReceived(this, (ChatRequest) receivedObject));
                }
            }
        }
    }

    public void release() {
        StreamUtilities.tryCloseStream(transmission);
    }

    public Worker(Socket socket) throws IOException {
        transmission = new SocketTransmission(socket);
        objectAdapter = new ObjectAdapter();
        protocol = new Protocol(objectAdapter, transmission);
    }

    public void setAccount(AccountInfo accountInfo) {
        this.myAccount = accountInfo;
        if (accountInfo != null && mOnAuthenticatedListener != null) {
            mOnAuthenticatedListener.onAuthenticated(this);
        }
    }

    public AccountInfo getAccount() {
        return myAccount;
    }

    public interface OnAuthenticatedListener {

        void onAuthenticated(Worker worker);
    }

    public interface OnRequestReceivedListener {

        ChatResult onRequestReceived(Worker sender, ChatRequest request);
    }
}
