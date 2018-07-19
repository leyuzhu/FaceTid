package com.zhuleyuhotmail.facetid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
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
    Thread clientThread;
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
                if(client != null) {
                    client.disconnect();
                }
                if(clientThread != null) {
                    try {
                        clientThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                client = new ChatClient(serverIp, port, username, mHandler);
                clientThread = new Thread(client);
                clientThread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
        //server.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(getBaseContext(), MyService.class));


     /*
        server = new ChatServer(port, mHandler);
        new Thread(server).start();
        infoip.setText(serverIp + ":" + port);
     */
    }


    private class AsyncTaskSend extends AsyncTask<String, String, String> {
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            if(client == null ){
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

    public void startService(View view) {
        //addNotification();
        startService(new Intent(getBaseContext(), MyService.class));
    }

    // Method to stop the service
    public void stopService(View view) {
        stopService(new Intent(getBaseContext(), MyService.class));
    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        //Intent notificationIntent = new Intent(this, MainActivity.class);
        Intent notificationIntent = new Intent(this, NotificationView.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
