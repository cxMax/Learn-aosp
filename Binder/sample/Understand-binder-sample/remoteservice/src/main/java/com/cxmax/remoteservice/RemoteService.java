package com.cxmax.remoteservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * @describe :
 * @usage :
 * <p>
 * </p>
 * Created by caixi on 17-4-16.
 */

public class RemoteService extends Service{

    public RemoteService() {
    }

    private IBinder iBinder = new IRemoteService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void addUser(User user) throws RemoteException {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
}
