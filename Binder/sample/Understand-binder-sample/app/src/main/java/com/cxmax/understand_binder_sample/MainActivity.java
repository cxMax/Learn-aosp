package com.cxmax.understand_binder_sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IRemoteService iRemoteService = IRemoteService.Stub.asInterface(iBinder);
            try {
                iRemoteService.addUser(new User(1 , "test"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void bindService() {
        Intent intent = new Intent().setComponent(new ComponentName("com.cxmax.remoteservice", "com.cxmax.remoteservice.RemoteService"));
        bindService(intent,serviceConnection , Context.BIND_AUTO_CREATE);
    }
}
