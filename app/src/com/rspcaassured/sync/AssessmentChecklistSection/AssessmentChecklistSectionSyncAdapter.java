package com.rspcaassured.sync.AssessmentChecklistSection;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.rspcaassured.loaders.AssessmentChecklistSectionLoader;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.accounts.UserAccountManager;
import com.salesforce.androidsdk.app.SalesforceSDKManager;

public class AssessmentChecklistSectionSyncAdapter extends AbstractThreadedSyncAdapter {
    // Key for extras bundle
    public static final String SYNC_DOWN_ONLY = "syncDownOnly";

    /**
     * Parameterized constructor.
     *
     * @param context        Context.
     * @param autoInitialize True - if it should be initialized automatically, False - otherwise.
     */
    public AssessmentChecklistSectionSyncAdapter(Context context, boolean autoInitialize,
                                         boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        final boolean syncDownOnly = extras.getBoolean(SYNC_DOWN_ONLY, false);
        final SalesforceSDKManager sdkManager = SalesforceSDKManager.getInstance();
        final UserAccountManager accManager = sdkManager.getUserAccountManager();
        if (sdkManager.isLoggingOut() || accManager.getAuthenticatedUsers() == null) {
            return;
        }
        if (account != null) {
            final UserAccount user = sdkManager.getUserAccountManager().buildUserAccount(account);
            final AssessmentChecklistSectionLoader assessmentChecklistSectionLoader = new AssessmentChecklistSectionLoader(getContext(), user, null);
            if (syncDownOnly) {
                assessmentChecklistSectionLoader.syncDown();
            } else {
                assessmentChecklistSectionLoader.syncUp(); // does a sync up followed by a sync down
            }
        }
    }
}
