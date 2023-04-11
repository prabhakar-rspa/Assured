package com.rspcaassured.Utility;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.PersistableBundle;
import android.util.Log;

import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.manager.SyncManager;

import org.json.JSONException;

public class BackgroundJobService extends JobService {
    private static final String TAG = "BackgroundJobService";
    private boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true; // Returning true to keep the operation running while background thread is open
    }

    private void doBackgroundWork(JobParameters params) {
        PersistableBundle bundle =  params.getExtras();
        Log.d(TAG, "Extras: " + bundle.get("SyncName"));
        // Do the background work in a new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                // background work
                if(jobCancelled){
                    return;
                }

                Log.d(TAG, "Starting Clean Ghost Record Process");
                Object sName = bundle.get("SyncName");
                if(sName != null){
                    cleanGhostRecords(sName.toString());
                }



                Log.d(TAG, "Job finished");
                jobFinished(params, false);
            }
        }).start();
    }

    /**
     * Removes ghost records from smart store
     */
    public void cleanGhostRecords(String syncDownName){
        SyncManager syncMgr = SyncManager.getInstance(MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser());
        try {
            syncMgr.cleanResyncGhosts(syncDownName, new SyncManager.CleanResyncGhostsCallback() {
                @Override
                public void onSuccess(int numRecords) {
                    Log.d(TAG,numRecords + " ghost records cleaned.");
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "Error occurred while attempting to cleanResyncGhosts", e);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SyncManager.MobileSyncException e) {
            Log.e(TAG, "MobileSyncException occurred while attempting to sync down", e);
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }
}
