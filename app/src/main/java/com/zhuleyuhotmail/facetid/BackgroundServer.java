package com.zhuleyuhotmail.facetid;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * The server that can be run both as a console application or a GUI
 */
public class BackgroundServer implements Runnable {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> al;

   // private Handler mHandler;

    // to display time
    private SimpleDateFormat sdf;

    // the port number to listen for connection
    private int port;

    // the boolean that will be turned of to stop the server
    private boolean ServerKeepGoing = true;

    private ServerSocket serverSocket;


    public BackgroundServer(int port) {
        //this.mHandler = handler;
        // the port
        this.port = port;
        // to display hh:mm:ss
        sdf = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
    }

    public void run() {
        connect();
    }

    public void connect() {
        /* create socket server and wait for connection requests */
        try {
            // the socket used by the server
            if(serverSocket != null) {
                serverSocket.close();
            }

            serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (ServerKeepGoing) {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept(); // accept connection
                // if was asked to stop
                if (!ServerKeepGoing)
                    break;
                ClientThread clientThread = new ClientThread(socket); // make a thread
                al.add(clientThread); // save it in the ArrayList
                clientThread.start();
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    /*
     * Stop the server
     */
    public void stop() {
        ServerKeepGoing = false;
        // asked to stop
        try {
            if(serverSocket != null ) {
                serverSocket.close();
            }

            for (int i = 0; i < al.size(); ++i) {
                ClientThread tc = al.get(i);
                tc.stopRunning();
            }
        } catch (Exception e) {
            display("Exception closing the server and clients: " + e);
        }

    }

    /*
     * Display message
     */
    private void display(String msg) {
        /*String msgWithTime = sdf.format(new Date()) + " " + msg;
        System.out.println(msgWithTime);
        //activity.msg.append(msgWithTime + "\n");
        Message message = new Message();
        message.obj = msg;
        //mHandler.sendMessage(message);*/
    }

    /*
     * To broadcast a message to all Clients
     */
    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        //System.out.print("IN Server:" + messageLf);
        //activity.msg.append(messageLf); // append in the room window
        display(messageLf);

        // Loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for (int i = al.size(); --i >= 0; ) {
            ClientThread ct = al.get(i);
            // try to write to the Client if it fails remove it from the list
            if (!ct.writeMsg(messageLf)) {
                al.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it
            if (ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }

    /**
     * One instance of this thread will run for each client
     */
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // the only type of message a will receive
        ChatMessage cm;
        // the date I connect
        String date;

        private boolean keepGoing = true;

        // Constructor
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            /* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            //boolean keepGoing = true;
            while (keepGoing) {
                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // the message part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch (cm.getType()) {
                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                        // scan al the users connected
                        for (int i = 0; i < al.size(); ++i) {
                            ClientThread ct = al.get(i);
                            writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
        }

        // Try to close everything
        private void close() {
            // try to close the connection
            try {
                if (sOutput != null)
                    sOutput.close();
            } catch (Exception e) {
            }
            try {
                if (sInput != null)
                    sInput.close();
            } catch (Exception e) {
            }
            ;
            try {
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
            }
        }

        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }

        void stopRunning() {
            keepGoing = false;
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }
    }
}

