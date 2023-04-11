package com.rspcaassured.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rspcaassured.objects.AssessmentChecklistSectionObject;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.manager.SyncManager;
import com.salesforce.androidsdk.mobilesync.util.SyncState;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartSqlHelper;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AssessmentChecklistSectionLoader extends AsyncTaskLoader<List<AssessmentChecklistSectionObject>> {
    public static final String ASSESSMENT_CHECKLIST_SECTION_SOUP = "assessmentChecklistSection";
    public static final String LOAD_COMPLETE_INTENT_ACTION = "com.rspcaassured.loaders.ASSESSMENT_CHECKLIST_SECTION_LOAD_COMPLETE";
    private static final String TAG = "AssessmentChecklistSectionLoader";
    private static final String SYNC_DOWN_NAME = "syncDownAssessmentChecklistSection";
    private static final String SYNC_UP_NAME = "syncUpAssessmentChecklistSection";
    private static final Integer LIMIT = 10000;

    private SmartStore smartStore;
    private SyncManager syncMgr;
    private String mAssessmentId;


    public AssessmentChecklistSectionLoader(Context context, UserAccount account, String assessmentId) {
        super(context);
        MobileSyncSDKManager sdkManager = MobileSyncSDKManager.getInstance();
        smartStore = sdkManager.getSmartStore(account);
        syncMgr = SyncManager.getInstance(account);

        mAssessmentId = assessmentId;
    }

    @Override
    public List<AssessmentChecklistSectionObject> loadInBackground() {
        if (!smartStore.hasSoup(ASSESSMENT_CHECKLIST_SECTION_SOUP)) {
            return null;
        }
        final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(ASSESSMENT_CHECKLIST_SECTION_SOUP, AssessmentChecklistSectionObject.ASSESSMENT_LOOKUP, mAssessmentId, AssessmentChecklistSectionObject.SECTION_ORDER,QuerySpec.Order.ascending, LIMIT);
        JSONArray results;
        List<AssessmentChecklistSectionObject> assessmentChecklistSectionObjects = new ArrayList<>();
        try {
            results = smartStore.query(querySpec, 0);
            for (int i = 0; i < results.length(); i++) {
                assessmentChecklistSectionObjects.add(new AssessmentChecklistSectionObject(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }
        return assessmentChecklistSectionObjects;
    }

    /**
     * Pushes local changes up to the server.
     */
    public synchronized void syncUp() {
        Log.d(TAG, "Starting AssessmentChecklistSectionLoader Sync Up Process");
        try {
            SyncState ss = syncMgr.getSyncStatus(SYNC_UP_NAME);
            Log.d(TAG,"Sync Status for Assessment Checklist Section Sync Up: " + ss.getStatus().toString());
            if(ss.getStatus() != SyncState.Status.RUNNING){
                syncMgr.reSync(SYNC_UP_NAME /* see usersyncs.json */, new SyncManager.SyncUpdateCallback() {

                    @Override
                    public void onUpdate(SyncState sync) {
                        if (SyncState.Status.DONE.equals(sync.getStatus())) {
                            syncDown();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SyncManager.MobileSyncException e) {
            Log.e(TAG, "MobileSyncException occurred while attempting to sync up", e);
        }
        Log.d(TAG, "Finishing AssessmentChecklistSectionLoader Sync Up Process");
    }

    /**
     * Pulls the latest records from the server.
     */
    public synchronized void syncDown() {
        Log.d(TAG, "Starting AssessmentChecklistSectionLoader Sync Down Process");
        try {
            SyncState ss = syncMgr.getSyncStatus(SYNC_DOWN_NAME);
            Log.d(TAG,"Sync Status for Assessment Checklist Section Sync Down: " + ss.getStatus().toString());
            if(ss.getStatus() != SyncState.Status.RUNNING){
                syncMgr.reSync(SYNC_DOWN_NAME /* see usersyncs.json */, new SyncManager.SyncUpdateCallback() {

                    @Override
                    public void onUpdate(SyncState sync) {
                        if (SyncState.Status.DONE.equals(sync.getStatus())) {
                            fireLoadCompleteIntent();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SyncManager.MobileSyncException e) {
            Log.e(TAG, "MobileSyncException occurred while attempting to sync down", e);
        }
        Log.d(TAG, "Finishing AssessmentChecklistSectionLoader Sync Down Process");
    }

    /**
     * Fires an intent notifying a registered receiver that fresh data is
     * available. This is for the special case where the data change has
     * been triggered by a background sync, even though the consuming
     * activity is in the foreground. Loaders don't trigger callbacks in
     * the activity unless the load has been triggered using a LoaderManager.
     */
    private void fireLoadCompleteIntent() {
        Log.d(TAG, "fireLoadCompleteIntent");
        final Intent intent = new Intent(LOAD_COMPLETE_INTENT_ACTION);
        SalesforceSDKManager.getInstance().getAppContext().sendBroadcast(intent);
    }
}
