package com.rspcaassured.sync.Application;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A simple service that binds with the correct sync adapter.
 *
 * @author shahedmiah
 */
public class ApplicationSyncService extends Service {

    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private static ApplicationSyncAdapter APPLICATION_SYNC_ADAPTER = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (SYNC_ADAPTER_LOCK) {
            if (APPLICATION_SYNC_ADAPTER == null) {
                APPLICATION_SYNC_ADAPTER = new ApplicationSyncAdapter(getApplicationContext(),
                        true, false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return APPLICATION_SYNC_ADAPTER.getSyncAdapterBinder();
    }
}
