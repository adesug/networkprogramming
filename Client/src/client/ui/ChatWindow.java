/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.lang3.StringEscapeUtils;

import dependency.bean.AccountInfo;
import dependency.bean.ChatMessage;
import dependency.bean.ChatRequest;
import dependency.bean.ChatResult;
import dependency.bo.ResourceManager;
import client.Client;
import client.Client.OnDataReceivedListener;
import dependency.utils.Task;
import java.awt.Adjustable;

import javax.swing.border.EmptyBorder;
import javax.swing.ImageIcon;
import javax.swing.JScrollBar;

/**
 *
 * @author asus
 */
public class ChatWindow extends Window implements ActionListener, WindowStateListener, OnDataReceivedListener /*, AdjustmentListener*/ {

    private static final String STYLE_SHEET = ".chat-box {margin: 2px;}"
            + ".chat-msg1 {border-radius:25px;background-color: #add8e6;padding: 7px; margin-top: 2px; margin-bottom: 2px; word-wrap: break-word;margin-right:50px;}"
            + ".chat-msg2 {border-radius:25px;background-color: #abd69a;padding: 7px; margin-top: 2px; margin-bottom: 2px; word-wrap: break-word;margin-left:50px;}";
    private static final String DEFAULT_HTML_FORMAT = "<html><head><style>" + STYLE_SHEET + "</style></head>"
            + "<body><div id=content class=chat-box> </div></body></html>";
    private static final String DEFAULT_FRIEND_CHAT_FORMAT = "<p class=chat-msg1>%s</p>";
    private static final String DEFAULT_WE_CHAT_FORMAT = "<p class=chat-msg2>%s</p>";
    private JTextField inputField;
    JScrollPane scrollPane;
    private JTextPane dispField;
    private final AccountInfo yourFriend;

    @Override
    protected void initializeComponents() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout(5, 5));

        inputField = new JTextField();
        inputField.setToolTipText("Enter your chat content then enter to send");
        panel.add(inputField, BorderLayout.CENTER);
        inputField.addActionListener(this);

        JButton btnSend = new JButton("");
        btnSend.setIcon(new ImageIcon(ResourceManager.getInstance().getImageByName("button-send.png")));
        btnSend.setToolTipText("Press to send your chat content");
        btnSend.addActionListener(this);
        panel.add(btnSend, BorderLayout.EAST);

        dispField = new JTextPane();
        dispField.setContentType("text/html");
        dispField.setEditable(false);
        dispField.setText(DEFAULT_HTML_FORMAT);

        scrollPane = new JScrollPane(dispField);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        //scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

        setSize(280, 400);

    }

    public ChatWindow(AccountInfo yourFriend) {
        setTitle(yourFriend.getDisplayName());

        this.yourFriend = yourFriend;

        Client.getInstance().addOnDataReceivedListener(this);
    }

    private void appendRawHtmlChatContent(String html) {
        HTMLDocument doc = (HTMLDocument) dispField.getDocument();
        Element contentElement = doc.getElement("content");
        try {
            if (contentElement.getElementCount() > 0) {
                Element lastElement = contentElement.getElement(contentElement.getElementCount() - 1);
                doc = (HTMLDocument) contentElement.getDocument();
                doc.insertAfterEnd(lastElement, html);
            } else {
                doc.insertAfterStart(contentElement, html);
            }
        } catch (BadLocationException | IOException e) {
            MessageBox.showMessageBoxInUIThread(this, "Internal error: " + e.getMessage(), MessageBox.MESSAGE_ERROR);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        performSending();
    }

    private void performSending() {
        String content = inputField.getText();
        if (content != null && (content = content.trim()).length() > 0) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setWhoId(yourFriend.getAccountId());
            final ChatRequest request = new ChatRequest(ChatRequest.CODE_CHAT_MESSAGE, chatMessage);
            Task.run(new Runnable() {
                @Override
                public void run() {
                    Client.getInstance().request(request);
                }
            });
            showWeChat(content);
            inputField.setText("");
        } else {
            MessageBox.showMessageBoxInUIThread(this,
                    "What do you want to send to '" + yourFriend.getDisplayName() + "'?", MessageBox.MESSAGE_INFO);
        }
    }

    public int getAccountId() {
        return yourFriend.getAccountId();
    }

    @Override
    protected void onWindowClosing() {
        super.onWindowClosing();
        Client.getInstance().removeOnDataReceivedListener(this);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == WindowEvent.WINDOW_ACTIVATED) {
            inputField.requestFocus();
        }
    }

    private String prepareHtmlString(String rawContent) {
        return StringEscapeUtils.escapeHtml3(rawContent);
    }

    private void focusMe() {
        requestFocus();
        inputField.requestFocus();
    }
    
    private void scrollToBottom(JScrollPane scrollPane) {
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    private void showWeChat(String content) {
        appendRawHtmlChatContent(String.format(DEFAULT_WE_CHAT_FORMAT, prepareHtmlString(content)));
        scrollToBottom(scrollPane); 
        focusMe();
    }

    private void showFriendChat(String content) {
        appendRawHtmlChatContent(String.format(DEFAULT_FRIEND_CHAT_FORMAT, prepareHtmlString(content)));
        scrollToBottom(scrollPane); 
        focusMe();
    }

    private void showFriendChat(final ChatMessage chatMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showFriendChat(chatMessage.getContent());
            }
        });
    }

    @Override
    public boolean onDataReceived(Client sender, ChatResult receivedObject) {
        if (receivedObject.getRequestCode() == ChatRequest.CODE_CHAT_MESSAGE) {
            if (receivedObject.getExtra() instanceof ChatMessage) {
                ChatMessage chatMessage = (ChatMessage) receivedObject.getExtra();
                if (chatMessage.getWhoId() == yourFriend.getAccountId()) {
                    showFriendChat(chatMessage);
                }
            } else {
                if (receivedObject.getCode() != ChatResult.CODE_OK) {
                    MessageBox.showMessageBoxInUIThread(this, receivedObject.getExtra(), MessageBox.MESSAGE_ERROR);
                }
            }
        } else if (receivedObject.getRequestCode() == ChatRequest.CODE_CHANGE_DISPNAME) {
            final AccountInfo accountInfo = (AccountInfo) receivedObject.getExtra();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setTitle(accountInfo.getDisplayName());
                }
            });
        }
        return true;
    }
    /*
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        //e.getAdjustable().setValue(e.getAdjustable().getMaximum());
        
    }*/
}
