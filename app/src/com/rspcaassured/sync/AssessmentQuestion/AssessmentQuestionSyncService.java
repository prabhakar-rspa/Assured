package com.rspcaassured.sync.AssessmentQuestion;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rspcaassured.sync.Assessment.AssessmentSyncAdapter;

public class AssessmentQuestionSyncService extends Service {
    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private static AssessmentQuestionSyncAdapter ASSESSMENT_QUESTION_SYNC_ADAPTER = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (SYNC_ADAPTER_LOCK) {
            if (ASSESSMENT_QUESTION_SYNC_ADAPTER == null) {
                ASSESSMENT_QUESTION_SYNC_ADAPTER = new AssessmentQuestionSyncAdapter(getApplicationContext(),
                        true, false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ASSESSMENT_QUESTION_SYNC_ADAPTER.getSyncAdapterBinder();
    }
}
