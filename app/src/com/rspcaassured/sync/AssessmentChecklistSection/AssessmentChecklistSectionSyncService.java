package com.rspcaassured.sync.AssessmentChecklistSection;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.rspcaassured.sync.AssessmentQuestion.AssessmentQuestionSyncAdapter;

public class AssessmentChecklistSectionSyncService extends Service {
    private static final Object SYNC_ADAPTER_LOCK = new Object();
    private static AssessmentChecklistSectionSyncAdapter ASSESSMENT_CHECKLIST_SECTION_SYNC_ADAPTER = null;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (SYNC_ADAPTER_LOCK) {
            if (ASSESSMENT_CHECKLIST_SECTION_SYNC_ADAPTER == null) {
                ASSESSMENT_CHECKLIST_SECTION_SYNC_ADAPTER = new AssessmentChecklistSectionSyncAdapter(getApplicationContext(),
                        true, false);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return ASSESSMENT_CHECKLIST_SECTION_SYNC_ADAPTER.getSyncAdapterBinder();
    }
}
