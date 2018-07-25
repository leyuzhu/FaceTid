package com.zhuleyuhotmail.facetid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogActivity extends AppCompatActivity {

    //ChatServer server;
    TextView msgView;
    EditText editMsg, peerIp;
    Button sendButton;
    Button connectButton;
    ChatClient client;
    Thread clientThread;
    Handler mHandler;
    //private String serverIp = "127.0.0.1";
    private String username = "Genius";
    private int port = 8080;

    static DialogActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        peerIp = (EditText) findViewById(R.id.serverIp);
        msgView = (TextView) findViewById(R.id.msg);
        editMsg = (EditText) findViewById(R.id.msgEditText);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String info = (String) msg.obj;
                msgView.append(info + "\n");
            }
        };

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textStr = editMsg.getText().toString();
                AsyncTaskSend sender = new AsyncTaskSend();
                sender.execute(textStr);
                msgView.append(textStr + "\n");
                editMsg.getText().clear();
            }
        });

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (client != null) {
                    client.disconnect();
                }
                if (clientThread != null) {
                    try {
                        clientThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //TODO: better check for IP
                String ipAddress = peerIp.getText().toString();
                if(ipAddress != null) {
                    msgView.append("Connect with " + ipAddress + "...\n");
                    client = new ChatClient(ipAddress, port, username, mHandler);
                    clientThread = new Thread(client);
                    clientThread.start();
                } else {
                    msgView.append("Empty Ip Address, please input correct address and redo the connect!\n");
                }
            }
        });

        Log.i("DialogActivity", "onCreate!");

        instance = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.disconnect();
        }

        Log.i("DialogActivity", "onDestroy!");
        //Stop/Restart the background service
        //stopService(new Intent(getBaseContext(), MyService.class));
        //server.stop();
    }

    private class AsyncTaskSend extends AsyncTask<String, String, String> {
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            if (client == null) {
                Message message = new Message();
                message.obj = "Client is not set up yet !\n";
                mHandler.sendMessage(message);
                return resp;
            }
            client.sendToServer(params[0]);
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    // Method to stop the service
    public void stopDialog(View view) {
        if (client != null) {
            client.disconnect();
        }
        msgView.append("Disconnect with Peer!\n");
    }

    public Handler getMsgHandler() {
        return mHandler;
    }

    static DialogActivity getInstance() {
        return instance;
    }
}
