package com.zhuleyuhotmail.facetid;


import java.net.*;
import java.io.*;
import android.os.Handler;
import android.os.Message;


/*
 * The Client that can be run both as a console or a GUI
 */
public class ChatClient implements Runnable {

    Handler mHandler;

    // for I/O
    private ObjectInputStream sInput; // to read from the socket
    private ObjectOutputStream sOutput; // to write on the socket
    private Socket socket;

    // the server, the port and the username
    private String server, username;
    private int port;


    ChatClient(String server, int port, String username, Handler handler) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.mHandler = handler;
    }

    public void connect() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("Error connecting to server:" + ec);
            return;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        /* Creating both Data Stream */
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
        }

        // creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try {
            if(sOutput != null) {
                sOutput.writeObject(username);
            } else {
                System.out.println("sOutput is null");
            }
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
        }
    }

    /*
     * To send a message to the console or the GUI
     */
    private void display(String msg) {
        Message message = new Message();
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    /*
     * To send a message to the server
     */
    void sendMessage(ChatMessage msg) {
        if(sOutput == null) {
            display("Network connection with receiver is wrong!");
            return;
        }

        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /*
     * When something goes wrong Close the Input/Output streams and disconnect
     * not much to do in the catch clause
     */
    public void disconnect() {
        try {
            if (sInput != null)
                sInput.close();
        } catch (Exception e) {
        }

        // not much else I can do
        try {
            if (sOutput != null)
                sOutput.close();
        } catch (Exception e) {
        }

        // not much else I can do
        try {
            if (socket != null)
                socket.close();
        } catch (Exception e) {
        }
    }


    public void run() {
        connect();
    }

    public void sendToServer(String str) {

        if (str.equalsIgnoreCase("LOGOUT")) {
            sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
        }
        // message WhoIsIn
        else if (str.equalsIgnoreCase("WHOISIN")) {
            sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
        } else { // default to ordinary message
            sendMessage(new ChatMessage(ChatMessage.MESSAGE, str));
        }

    }


    class ListenFromServer extends Thread {

        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    display(msg);
                } catch (IOException e) {
                    display("Server has close the connection: " + e);
                    break;
                }
                // can't happen with a String object but need the catch anyhow
                catch (ClassNotFoundException e2) {
                }
            }
        }
    }
}
