package com.rspcaassured.loaders;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import android.content.AsyncTaskLoader;

import com.rspcaassured.objects.ApplicationObject;
import com.rspcaassured.objects.AssessmentObject;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AssessmentListLoader extends AsyncTaskLoader<List<AssessmentObject>>{

    public static final String ASSESSMENT_SOUP = "assessments";
    public static final String LOAD_COMPLETE_INTENT_ACTION = "com.rspcaassured.loaders.ASSESSMENT_LOAD_COMPLETE";
    private static final String TAG = "AssessmentListLoader";
    private static final String SYNC_DOWN_NAME = "syncDownAssessments";
    private static final String SYNC_UP_NAME = "syncUpAssessments";
    private static final Integer LIMIT = 10000;

    private SmartStore smartStore;
    private SyncManager syncMgr;
    private String applicationId;

    public AssessmentListLoader(Context context, UserAccount account,
                                String applicationId) {
        super(context);
        this.applicationId = applicationId;
        MobileSyncSDKManager sdkManager = MobileSyncSDKManager.getInstance();
        smartStore = sdkManager.getSmartStore(account);;
        syncMgr = SyncManager.getInstance(account);
    }

    @Override
    public List<AssessmentObject> loadInBackground() {
        if (TextUtils.isEmpty(applicationId)) {
            return null;
        }
        if (!smartStore.hasSoup(ASSESSMENT_SOUP)) {
            return null;
        }
        final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(ASSESSMENT_SOUP, AssessmentObject.APPLICATION_ID, applicationId, AssessmentObject.NAME, QuerySpec.Order.ascending,LIMIT);
        JSONArray results;
        List<AssessmentObject> assessments = new ArrayList<>();
        try {
            results = smartStore.query(querySpec, 0);
            for (int i = 0; i < results.length(); i++) {
                assessments.add(new AssessmentObject(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }
        return assessments;
    }

    /**
     * Pushes local changes up to the server.
     */
    public synchronized void syncUp() {
        try {
            SyncState ss = syncMgr.getSyncStatus(SYNC_UP_NAME);
            Log.d(TAG,"Sync Status for Assessment Sync Up: " + ss.getStatus().toString());
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
    }

    /**
     * Pulls the latest records from the server.
     */
    public synchronized void syncDown() {
        try {
            SyncState ss = syncMgr.getSyncStatus(SYNC_DOWN_NAME);
            Log.d(TAG,"Sync Status for Assessment Sync Down: " + ss.getStatus().toString());
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
    }

    /**
     * Fires an intent notifying a registered receiver that fresh data is
     * available. This is for the special case where the data change has
     * been triggered by a background sync, even though the consuming
     * activity is in the foreground. Loaders don't trigger callbacks in
     * the activity unless the load has been triggered using a LoaderManager.
     */
    private void fireLoadCompleteIntent() {
        final Intent intent = new Intent(LOAD_COMPLETE_INTENT_ACTION);
        SalesforceSDKManager.getInstance().getAppContext().sendBroadcast(intent);
    }

}
