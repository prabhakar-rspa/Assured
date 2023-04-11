package com.rspcaassured.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.rspcaassured.objects.ApplicationObject;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartSqlHelper;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * A simple AsyncTaskLoader to load object detail for a Application object.
 *
 * @author shahedmiah
 */
public class ApplicationDetailLoader extends AsyncTaskLoader<ApplicationObject> {

    private static final String TAG = "ApplicationDetailLoader";

    private String objectId;
    private SmartStore smartStore;

    /**
     * Parameterized constructor.
     *
     * @param context Context.
     * @param account User account.
     * @param objId Object ID.
     */
    public ApplicationDetailLoader(Context context, UserAccount account,
                               String objId) {
        super(context);
        objectId = objId;
        smartStore = MobileSyncSDKManager.getInstance().getSmartStore(account);
    }

    @Override
    public ApplicationObject loadInBackground() {
        if (TextUtils.isEmpty(objectId)) {
            return null;
        }
        ApplicationObject sObject = null;
        if (!smartStore.hasSoup(ApplicationListLoader.APPLICATION_SOUP)) {
            return null;
        }
        final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(
                ApplicationListLoader.APPLICATION_SOUP, Constants.ID, objectId, null, null, 1);
        JSONArray results = null;
        try {
            results = smartStore.query(querySpec, 0);
            if (results != null) {
                sObject = new ApplicationObject(results.getJSONObject(0));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }
        return sObject;
    }
}
