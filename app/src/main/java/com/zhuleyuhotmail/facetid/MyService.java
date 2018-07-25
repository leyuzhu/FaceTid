package com.zhuleyuhotmail.facetid;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {

    private int port = 8080;
    BackgroundServer server;
    Thread serverThread;
    DialogActivity activity;
    private Handler mHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

/*
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
        Log.i("Service Created", "Ohhhhhhh");
        cleanUpServer();
        server = new BackgroundServer(port, this);
        serverThread = new Thread(server);
        serverThread.start();
    }
*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        Log.i("Service Started", "Ohhhhhhh");
        cleanUpServer();
        server = new BackgroundServer(port, this);
        serverThread = new Thread(server);
        serverThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        Log.i("Service Destroyed", "Ohhhhhhh");
        //Log.i("Before CleanUP", "Ohhhhhhh");
        cleanUpServer();
        //Log.i("After CleanUP", "Ohhhhhhh");
        /*Intent intent = new Intent("com.android.techtrainner");
        intent.putExtra("yourvalue", "torestore");
        Log.i("Before Broadcast", "Ohhhhhhh");
        sendBroadcast(intent);
        Log.i("After BroadCast", "Ohhhhhhh");*/
    }

    private void cleanUpServer() {
        if(server != null) {
            server.stop();
        }
        if(serverThread != null) {
            try {
                serverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void StartDialogActivity() {
        activity = DialogActivity.getInstance();
        if(activity != null) {
            mHandler = activity.getMsgHandler();
        } else {
            startActivity(new Intent(this, DialogActivity.class));
            activity = DialogActivity.getInstance();
            mHandler = activity.getMsgHandler();
        }
    }

    public void displayMsg(Message msg) {
        if(mHandler != null) {
            mHandler.sendMessage(msg);
        } else {
            Log.e("MyService", "mHandler is null");
        }
    }
}
