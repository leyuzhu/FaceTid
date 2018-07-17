package com.zhuleyuhotmail.facetid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    ChatServer server;
    TextView infoip, msgView;
    EditText editMsg;
    Button sendButton;
    Button connectButton;
    ChatClient client;
    Handler mHandler;
    private String serverIp = "127.0.0.1";
    private String username = "Genius";
    private int port = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoip = (TextView) findViewById(R.id.infoip);
        msgView = (TextView) findViewById(R.id.msg);
        editMsg = (EditText) findViewById(R.id.msgEditText);
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
                client = new ChatClient(serverIp, port, username, mHandler);
                new Thread(client).start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
        server.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String info = (String) msg.obj;
                msgView.append(info + "\n");
            }
        };

        server = new ChatServer(port, mHandler);
        new Thread(server).start();

        infoip.setText(serverIp + ":" + port);
    }


    private class AsyncTaskSend extends AsyncTask<String, String, String> {
        private String resp;

        @Override
        protected String doInBackground(String... params) {
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

}
