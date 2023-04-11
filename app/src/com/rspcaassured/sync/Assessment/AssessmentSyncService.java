package com.rspcaassured.sync.Assessment;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AssessmentSyncService extends Service {
    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private static AssessmentSyncAdapter ASSESSMENT_SYNC_ADAPTER = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (SYNC_ADAPTER_LOCK) {
            if (ASSESSMENT_SYNC_ADAPTER == null) {
                ASSESSMENT_SYNC_ADAPTER = new AssessmentSyncAdapter(getApplicationContext(),
                        true, false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ASSESSMENT_SYNC_ADAPTER.getSyncAdapterBinder();
    }
}
