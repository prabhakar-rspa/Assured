package com.rspcaassured.ui.Assessment;

import static com.rspcaassured.Utility.CustomDateFormatter.formattedDate;

import android.accounts.Account;
import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.Utility.BackgroundJobService;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.LoadingDialog;
import com.rspcaassured.Utility.SharedPref;
import com.rspcaassured.loaders.ApplicationDetailLoader;
import com.rspcaassured.loaders.ApplicationListLoader;
import com.rspcaassured.loaders.AssessmentListLoader;
import com.rspcaassured.loaders.AssessmentQuestionListLoader;
import com.rspcaassured.objects.ApplicationObject;
import com.rspcaassured.objects.AssessmentObject;
import com.rspcaassured.sync.Assessment.AssessmentSyncAdapter;
import com.rspcaassured.ui.Checklist.ChecklistActivity;
import com.rspcaassured.ui.MainActivity;
import com.rspcaassured.ui.NoInternetDialogFragment;
import com.rspcaassured.ui.Notifications;
import com.rspcaassured.ui.Profile;
import com.rspcaassured.ui.SettingsActivity;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AssessmentOverviewActivity extends SalesforceActivity{
    public static NoInternetDialogFragment noInternetDialogFragment;
    private AlertDialog alert = null;
    // Initialise variable
    DrawerLayout drawerLayout;

    // Shared preferences
    SharedPref sharedPref;

    public static final String OBJECT_ID_KEY = "object_id";
    public static final String OBJECT_NAME_KEY = "object_name";

    private static final String SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.Assessment.AssessmentSyncAdapter";// assessment sync content authority
    private static final long SYNC_FREQUENCY_ONE_HOUR = 60 * 60;
    private static final int APPLICATION_DETAIL_LOADER_ID = 2;
    private static final int ASSESSMENT_LIST_LOADER_ID = 3;
    private static final String TAG = "AssessmentOverview";
    private static final int JOB_ID = 2;
    private static final String ASSESSMENT_SYNC_DOWN_NAME = "syncDownAssessments";

    private UserAccount curAccount;
    private String objectId;
    private ApplicationObject sObject;
    private List<AssessmentObject> assessmentObjectList;
    private LoadCompleteReceiver loadCompleteReceiver;
    private AtomicBoolean isRegistered;

    private RestClient client;

    AssessmentSectionRecyclerAdapter assessmentSectionRecyclerAdapter;
    RecyclerView assessmentSectionRecycler;
    List<AssessmentSection> assessmentSectionsList;



    // editable texts for record
    //private EditText siteNotesEditText;

    private TextView siteNotesEditText;

    // action buttons
    private Button saveButton;
    private Button cancelButton;

    // Loading dialog
    LoadingDialog loadingDialog;

    // Loader callbacks
    LoaderManager.LoaderCallbacks<ApplicationObject> applicationRecordCallback;
    LoaderManager.LoaderCallbacks<List<AssessmentObject>> assessmentListCallback;

    private ImageView editWhat3Words,openWhat3WordsWebsite;

    private String what3WordsFromServer = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        // setting the theme style
        if(sharedPref.loadNightModeState()){
            setTheme(R.style.darkTheme);
        }else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_overview);

        noInternetDialogFragment = new NoInternetDialogFragment();

        // Create loading dialog
        loadingDialog = new LoadingDialog(this);
        // Start loading dialog
        loadingDialog.startAlertDialog();

        // Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        editWhat3Words = findViewById(R.id.editWhat3Words);
        openWhat3WordsWebsite = findViewById(R.id.openWhat3WordsWebsite);

        // Load drawer layout user information
        MainActivity.loadUserInformation(drawerLayout);

        // Launching assessment with object id key passed from main activity
        final Intent launchIntent = getIntent();
        if (launchIntent != null) {
            objectId = launchIntent.getStringExtra(MainActivity.OBJECT_ID_KEY);
        }

        curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();

        assessmentSectionsList = new ArrayList<>();
        assessmentSectionRecycler = findViewById(R.id.assessmentChecklistSectionRecycler);


        // edit text listener
        siteNotesEditText = findViewById(R.id.siteNotesEditText);
        //siteNotesEditText.addTextChangedListener(displayRecordActionsTextWatcher);

        //action buttons
        saveButton = findViewById(R.id.saveAssessmentOverview);
        cancelButton = findViewById(R.id.cancelSaveAssessment);

        setActionButtonOnClicks();

        // Initialising broadcast receiver class
        loadCompleteReceiver = new LoadCompleteReceiver();
        isRegistered = new AtomicBoolean(false);

        // initialise loader callbacks
        applicationRecordCallback = new LoaderManager.LoaderCallbacks<ApplicationObject>() {
            @Override
            public Loader<ApplicationObject> onCreateLoader(int id, Bundle args) {
                return new ApplicationDetailLoader(getApplicationContext(), curAccount, objectId);
            }

            @Override
            public void onLoadFinished(Loader<ApplicationObject> loader, ApplicationObject data) {
                sObject = data;
                if(sObject!=null){
                    what3WordsFromServer = sObject.getWhat3words();
                }
                refreshScreen();
            }

            @Override
            public void onLoaderReset(Loader<ApplicationObject> loader) {
                sObject = null;
                refreshScreen();
            }
        };
        assessmentListCallback = new LoaderManager.LoaderCallbacks<List<AssessmentObject>>() {
            @Override
            public Loader<List<AssessmentObject>> onCreateLoader(int id, Bundle args) {
                return new AssessmentListLoader(getApplicationContext(),curAccount,objectId);
            }

            @Override
            public void onLoadFinished(Loader<List<AssessmentObject>> loader, List<AssessmentObject> data) {
                assessmentObjectList = data;
                refreshScreen();
            }

            @Override
            public void onLoaderReset(Loader<List<AssessmentObject>> loader) {
                assessmentObjectList = null;
                refreshScreen();
            }
        };

        setOnClicks();
    }

    private void setOnClicks() {
        editWhat3Words.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWhat3WordsEditDialog();
            }
        });

        openWhat3WordsWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(
                        AssessmentOverviewActivity.this,
                        "Redirecting to What3Words website",
                        Toast.LENGTH_SHORT
                ).show();

                String fullWebsite = "https://what3words.com/"+what3WordsFromServer ;
                Log.d("onCreate", "what 3 words website: "+fullWebsite);
                openWebsiteOfWhat3Words(fullWebsite);
            }
        });
    }

    private void openWebsiteOfWhat3Words(String fullWebsite) {
        Intent httpIntent = new Intent(Intent.ACTION_VIEW);
        httpIntent.setData(Uri.parse(fullWebsite));
        startActivity(httpIntent);
    }

    private void setActionButtonOnClicks(){
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlert();
                showCancelAlertDialog(AssessmentOverviewActivity.this);
            }
        });
    }

    private void showCancelAlertDialog(Context context){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage("Are you sure you want to cancel ?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        AssessmentOverviewActivity.this.finish();
                    }
                });

        dialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alert = dialogBuilder.create();
        alert.show();
    }

    private void dismissAlert(){
        if(alert!=null){
            alert.dismiss();
        }
    }



    private TextWatcher displayRecordActionsTextWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            saveButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
        }
    };

    /**
     * on Click action for cancelling save record
     */
    public void cancelSave(View view){
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        refreshScreen();
    }

    /**
     * on Click action for cancelling save record
     */
    public void saveApplication(View view){
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        final String siteNotesEdited = siteNotesEditText.getText().toString();
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        JSONObject application;
        try {
            boolean isCreate = TextUtils.isEmpty(objectId);
            if (!isCreate) {
                application = smartStore.retrieve(ApplicationListLoader.APPLICATION_SOUP,
                        smartStore.lookupSoupEntryId(ApplicationListLoader.APPLICATION_SOUP,
                                Constants.ID, objectId)).getJSONObject(0);
            } else {
                application = new JSONObject();
                application.put(Constants.ID, "local_" + System.currentTimeMillis()
                        + Constants.EMPTY_STRING);
                final JSONObject attributes = new JSONObject();
                attributes.put(Constants.TYPE.toLowerCase(), "Application__c");
                application.put(Constants.ATTRIBUTES, attributes);
            }
            //application.put(ApplicationObject.WHAT3WORDS, "test");
            application.put(ApplicationObject.SITE_NOTE_EDIT, siteNotesEdited);
            application.put(SyncTarget.LOCAL, true);
            application.put(SyncTarget.LOCALLY_UPDATED, !isCreate);
            application.put(SyncTarget.LOCALLY_CREATED, isCreate);
            application.put(SyncTarget.LOCALLY_DELETED, false);
            if (isCreate) {
                smartStore.create(ApplicationListLoader.APPLICATION_SOUP, application);
            } else {
                Log.d(TAG,"saveApplications test 2=> " + application);
                smartStore.upsert(ApplicationListLoader.APPLICATION_SOUP, application);
            }
            Toast.makeText(this, "Save successful!", Toast.LENGTH_LONG).show();
            //finish();
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }
    }


    public void saveWhat3WordsText(String newText){
        saveButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        final String siteNotesEdited = siteNotesEditText.getText().toString();
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        JSONObject application;
        try {
            boolean isCreate = TextUtils.isEmpty(objectId);
            if (!isCreate) {
                application = smartStore.retrieve(ApplicationListLoader.APPLICATION_SOUP,
                        smartStore.lookupSoupEntryId(ApplicationListLoader.APPLICATION_SOUP,
                                Constants.ID, objectId)).getJSONObject(0);
            } else {
                application = new JSONObject();
                application.put(Constants.ID, "local_" + System.currentTimeMillis()
                        + Constants.EMPTY_STRING);
                final JSONObject attributes = new JSONObject();
                attributes.put(Constants.TYPE.toLowerCase(), "Application__c");
                application.put(Constants.ATTRIBUTES, attributes);
            }
            application.put(ApplicationObject.WHAT3WORDS, newText);
            application.put(ApplicationObject.SITE_NOTE_EDIT, siteNotesEdited);
            application.put(SyncTarget.LOCAL, true);
            application.put(SyncTarget.LOCALLY_UPDATED, !isCreate);
            application.put(SyncTarget.LOCALLY_CREATED, isCreate);
            application.put(SyncTarget.LOCALLY_DELETED, false);
            if (isCreate) {
                smartStore.create(ApplicationListLoader.APPLICATION_SOUP, application);
            } else {
                Log.d(TAG,"saveApplications test 2=> " + application);
                smartStore.upsert(ApplicationListLoader.APPLICATION_SOUP, application);
            }
            Toast.makeText(this, "Save successful!", Toast.LENGTH_LONG).show();
            //finish();
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }
    }


    private void showWhat3WordsEditDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AssessmentOverviewActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.edit_what3words_layout, null);
        alertDialog.setView(customLayout);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                EditText what3WordsEt = customLayout.findViewById(R.id.what3WordsEditText);
                String str = what3WordsEt.getText().toString().trim();
                Log.d("onCreate", "showWhat3WordsEditDialog text: "+str);
                saveWhat3WordsText(str);
                what3WordsFromServer = str;
                saveWhat3WordsTextOnUI(what3WordsFromServer);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        EditText et = customLayout.findViewById(R.id.what3WordsEditText);
        et.setText(what3WordsFromServer);
        alert.show();
    }

    private void saveWhat3WordsTextOnUI(String str) {
        setText((TextView) findViewById(R.id.what3WordsInput), str);
    }


    /**
     * on Click action for ClickNavMenu
     */
    public void ClickNavMenu(View view){
        // Open drawer
        MainActivity.openDrawer(drawerLayout);
    }

    /**
     * on click action for ClickCloseNavigation
     */
    public void ClickCloseNavigation(View view){
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    /**
     * Onclick action for ClickHome
     */
    public void ClickHome(View view){
        // Redirect activity to home
        MainActivity.redirectActivity(this,MainActivity.class);
    }

    /**
     * redirects activity to profile
     */
    public void ClickProfile(View view){
        // Redirects activity to profile
        MainActivity.redirectActivity(this, Profile.class);
    }

    /**
     * Redirects activity to Notifications
     */
    public void ClickNotifications(View view){
        // Redirects activity to Notifications
        MainActivity.redirectActivity(this, Notifications.class);
    }

    /**
     * redirects activity to settings
     */
    public void ClickSettings(View view){
        // Redirect activity to settings
        MainActivity.redirectActivity(this, SettingsActivity.class);
    }

    /**
     * Displays logout confirmation dialog
     * @param view view element
     */
    public void onClickLogout(View view){
        if(!InternetUtil.isInternetAvailable(AssessmentOverviewActivity.this)){
            noInternetDialogFragment.show(getFragmentManager(), "No Internet");
            return;
        }
        MainActivity.logoutConfirmationDialog.show(getFragmentManager(), "LogoutDialog");
    }

    @Override
    public void onPause(){
        // Unregister broadcast reciever
        if (isRegistered.get()) {
            unregisterReceiver(loadCompleteReceiver);
        }
        isRegistered.set(false);
        // destroy loader
        getLoaderManager().destroyLoader(APPLICATION_DETAIL_LOADER_ID);
        sObject = null;
        getLoaderManager().destroyLoader(ASSESSMENT_LIST_LOADER_ID);
        assessmentObjectList = null;

        super.onPause();
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    @Override
    public void onDestroy() {
        dismissAlert();
        loadCompleteReceiver = null;
        super.onDestroy();
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        getLoaderManager().initLoader(APPLICATION_DETAIL_LOADER_ID, null, applicationRecordCallback).forceLoad();
        getLoaderManager().initLoader(ASSESSMENT_LIST_LOADER_ID,null, assessmentListCallback).forceLoad();

        // Registering broadcast receiver
        if (!isRegistered.get()) {
            registerReceiver(loadCompleteReceiver,
                    new IntentFilter(AssessmentListLoader.LOAD_COMPLETE_INTENT_ACTION));
        }
        isRegistered.set(true);

        // Setup periodic sync
        setupPeriodicSync();

        // Sync now
        requestSync(true /* sync down only */);

        // Periodic clean ghost job starting
        setupPeriodicCleanGhosts();

    }

    /**
     * Schedule job to periodically clean ghost records from smart store
     */
    public void setupPeriodicCleanGhosts(){
        Boolean backgroundServicesState = sharedPref.loadBackgroundServicesState();
        Boolean jobAlreadyRunning =  isJobServiceOn(this);
        Log.d(TAG, "Job Already Running => " + jobAlreadyRunning);
        if(backgroundServicesState && !jobAlreadyRunning){
            ComponentName componentName = new ComponentName(this, BackgroundJobService.class);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("SyncName", ASSESSMENT_SYNC_DOWN_NAME);
            JobInfo info = new JobInfo.Builder(JOB_ID, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(60 * 60 * 1000)
                    .setExtras(bundle)
                    .build();

            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if(resultCode == JobScheduler.RESULT_SUCCESS){
                Log.d(TAG, "Job Scheduled.");
            }else{
                Log.d(TAG, "Job Scheduling Failed.");
            }
        }
    }

    /**
     * Checks if Job Service is already pending
     * @param context
     * @return
     */
    public static boolean isJobServiceOn( Context context ) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == JOB_ID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }

    /**
     * Setup periodic sync
     */
    private void setupPeriodicSync() {
        Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();
        /*
         * Enables sync automatically for this provider. To enable almost
         * instantaneous sync when records are modified locally, a call needs
         * to be made by the content provider to notify the sync provider
         * that the underlying data set has changed. Since we don't use cursors
         * in this sample application, we simply enable periodic sync every hour.
         */
        ContentResolver.setSyncAutomatically(account, SYNC_CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, SYNC_CONTENT_AUTHORITY,
                Bundle.EMPTY, SYNC_FREQUENCY_ONE_HOUR);
    }

    /**
     * Request a sync
     * @param syncDownOnly if true, only a sync down is done, if false a sync up followed by a sync down is done
     */
    private void requestSync(boolean syncDownOnly) {
        Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();
        Bundle extras = new Bundle();
        extras.putBoolean(AssessmentSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
        ContentResolver.requestSync(account, SYNC_CONTENT_AUTHORITY, extras);
    }

    private void refreshList() {
        getLoaderManager().getLoader(APPLICATION_DETAIL_LOADER_ID).forceLoad();
        getLoaderManager().getLoader(ASSESSMENT_LIST_LOADER_ID).forceLoad();
    }

    /*private String formattedDate(String date){
        if(date!=null && !date.trim().isEmpty()){
            String[] sep = date.split("-");
            if(sep.length==3){
                return sep[2] +" / "+ sep[1] + " / "+sep[0];
            }else{
                return "";
            }
        }else{
            return "";
        }
    }*/

    private void refreshScreen() {
        if (sObject != null) {
            // dismiss loading dialog
            loadingDialog.dismissDialog();

            Log.d("onCreate", "sObject.getSiteNotes() : "+sObject.getSiteNotes());
            Log.d("onCreate", "sObject.getSiteName() : "+sObject.getSiteName());
            Log.d("onCreate", "sObject.getSiteNotesEdit() : "+sObject.getSiteNotesEdit());



            // change the values of each field
            setText((TextView) findViewById(R.id.farmAssessmentNameAndDate),sObject.getFarmAssessmentName());
            setText((TextView) findViewById(R.id.memberNameInput), sObject.getSiteName());
            //setText((TextView) findViewById(R.id.lastAssessmentDateInput), sObject.getPrevAssessmentDate());
            setText((TextView) findViewById(R.id.lastAssessmentDateInput), formattedDate(sObject.getPrevAssessmentDate()));
            setText((TextView) findViewById(R.id.assessmentDateInput), sObject.getRenewalDate());
            setText((TextView) findViewById(R.id.assessmentDeadlineInput), formattedDate(sObject.getAssessmentDeadline()));
            setText((TextView) findViewById(R.id.contactNumberInput), sObject.getSitePhone());
            setText((TextView) findViewById(R.id.locationNotesInput), sObject.getLocationNotes());
            setText((TextView) findViewById(R.id.what3WordsInput), sObject.getWhat3words());
            /*setText((TextView) findViewById(R.id.siteNotesEditText), sObject.getSiteNotes());*/
            setText((TextView) findViewById(R.id.siteNotesEditText), sObject.getSiteNotesEdit());
            String address = sObject.getAddress();
            address = address.replace("<br>",", ");
            address = address.replace("&amp;","&");
            setText((TextView) findViewById(R.id.addressInput), address);


            // Map of assessment section to assessment list
            if(assessmentObjectList != null){
                Map<String, List<AssessmentObject>> assessmentSectionMap = new HashMap<>();
                for(AssessmentObject assessmentObject : assessmentObjectList){
                    if(assessmentSectionMap.containsKey(assessmentObject.getChecklistType())){
                        List<AssessmentObject> aList = assessmentSectionMap.get(assessmentObject.getChecklistType());
                        assert aList != null;
                        aList.add(assessmentObject);
                        assessmentSectionMap.put(assessmentObject.getChecklistType(), aList);
                    }else{
                        List<AssessmentObject> aList = new ArrayList<>();
                        aList.add(assessmentObject);
                        assessmentSectionMap.put(assessmentObject.getChecklistType(), aList);
                    }
                }
                assessmentSectionsList.clear();
                for(String section : assessmentSectionMap.keySet()){
                    assessmentSectionsList.add(new AssessmentSection(section, assessmentSectionMap.get(section)));
                }


                // Set the assessment section recycler adapter
                assessmentSectionRecyclerAdapter = new AssessmentSectionRecyclerAdapter(this, assessmentSectionsList);
                assessmentSectionRecycler.setAdapter(assessmentSectionRecyclerAdapter);
            }

        }
    }



    private void setEditText(EditText textField, String text) {
        if (textField != null) {
            textField.setText(text);
        }
    }
    private void setText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    /**
     * A simple receiver for load complete events.
     *
     * @author shahedmiah
     */
    private class LoadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                if (AssessmentListLoader.LOAD_COMPLETE_INTENT_ACTION.equals(action)) {
                    refreshList();
                }
            }
        }
    }
}