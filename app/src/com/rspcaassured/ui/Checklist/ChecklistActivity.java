package com.rspcaassured.ui.Checklist;

import android.accounts.Account;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.Utility.BackgroundJobService;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.LoadingDialog;
import com.rspcaassured.Utility.SharedPref;
import com.rspcaassured.loaders.ApplicationListLoader;
import com.rspcaassured.loaders.AssessmentChecklistSectionLoader;
import com.rspcaassured.loaders.AssessmentListLoader;
import com.rspcaassured.loaders.AssessmentQuestionListLoader;
import com.rspcaassured.objects.ApplicationObject;
import com.rspcaassured.objects.AssessmentChecklistSectionObject;
import com.rspcaassured.objects.AssessmentObject;
import com.rspcaassured.objects.AssessmentQuestionObject;
import com.rspcaassured.sync.Assessment.AssessmentSyncAdapter;
import com.rspcaassured.sync.AssessmentChecklistSection.AssessmentChecklistSectionSyncAdapter;
import com.rspcaassured.sync.AssessmentQuestion.AssessmentQuestionSyncAdapter;
import com.rspcaassured.ui.Assessment.AssessmentOverviewActivity;
import com.rspcaassured.ui.MainActivity;
import com.rspcaassured.ui.NoInternetDialogFragment;
import com.rspcaassured.ui.Notifications;
import com.rspcaassured.ui.Profile;
import com.rspcaassured.ui.SettingsActivity;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartSqlHelper;
import com.salesforce.androidsdk.smartstore.store.SmartStore;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ChecklistActivity extends SalesforceActivity {
    public static NoInternetDialogFragment noInternetDialogFragment;
    private static final int REQUEST_WRITE_PERMISSION = 99;
    private static final int LAUNCH_INC_PAGE = 55;
    private AlertDialog choiceDialog = null;
    public static final int GET_FROM_GALLERY = 3;
    private String commentBoxData = "";
    // Initialise variable
    DrawerLayout drawerLayout;

    SharedPref sharedPref;

    private static final String CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.AssessmentQuestion.AssessmentQuestionSyncAdapter";
    private static final String CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.AssessmentChecklistSection.AssessmentChecklistSectionSyncAdapter";
    private static final String ASSESSMENT_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.Assessment.AssessmentSyncAdapter";// assessment sync content authority
    private static final long SYNC_FREQUENCY_ONE_HOUR = 60 * 60;
    private static final int ASSESSMENT_QUESTION_LOADER_ID = 4;
    private static final int ASSESSMENT_CHECKLIST_SECTION_LOADER_ID = 5;

    private RestClient client;

    private static final String TAG = "ChecklistActivity";
    private static final int CHECKLIST_QUESTION_JOB_ID = 3;
    private static final String ASSESSMENT_QUESTION_SYNC_DOWN_NAME = "syncDownAssessmentQuestions";
    private static final int CHECKLIST_SECTION_JOB_ID = 4;
    private static final String ASSESSMENT_CHECKLIST_SECTION_SYNC_DOWN_NAME = "syncDownAssessmentChecklistSection";

    private String objectId;
    private String objectName;
    private String applicationId;
    private String rearingSystem;
    private String animal;
    private UserAccount curAccount;
    private ChecklistQuestionLoadCompleteReceiver checklistQuestionLoadCompleteReceiver;
    private ChecklistSectionLoadCompleteReceiver checklistSectionLoadCompleteReceiver;
    private AtomicBoolean checklistQuestionBroadcastReceiverIsRegistered;
    private AtomicBoolean checklistSectionReceiverRegistered;
    private List<AssessmentQuestionObject> assessmentQuestionObjectList;
    private List<AssessmentChecklistSectionObject> assessmentChecklistSectionObjectList;
    private AssessmentChecklistSectionObject selectedAssessmentChecklistSectionObject;

    Spinner spinnerSectionItemList;
    private ArrayList<ChecklistSectionItem> mChecklistSectionItems;
    //private ArrayList<ChecklistSectionItem> mChecklistSectionItems2;
    private ChecklistSectionSpinnerAdapter mChecklistSectionSpinnerAdapter;
    private String selectedSection;

    ChecklistSubsectionRecyclerAdapter checklistSubsectionRecyclerAdapter;
    RecyclerView checklistSubsectionRecycler;
    List<ChecklistSubsection> checklistSubsections;
    List<ChecklistStandard> checklistStandards;

    private ArrayList<ChecklistStandard> nonCompliances;

    // Loading dialog
    LoadingDialog loadingDialog;

    // Section Comments button
    Button openSectionCommentsButton;
    Button completeBtn;

    private boolean onLoadFinishedAlreadyExecuted = false;

    private Uri imageUri = null;


    // Loader callbacks
    LoaderManager.LoaderCallbacks<List<AssessmentQuestionObject>> assessmentQuestionObjectLoaderCallbacks;
    LoaderManager.LoaderCallbacks<List<AssessmentChecklistSectionObject>> assessmentChecklistSectionObjectLoaderCallbacks;

    ChecklistStandardRecyclerAdapter tempCheckStandRecyclerAdapter;
    int tempCheckStandRecyclerAdapterItemPosition = 0;

    private AtomicInteger totalNotAns = new AtomicInteger(0);
    private AtomicInteger totalNoComments = new AtomicInteger(0);

    private Button reloadBtn;

    private Runnable reloadRunnable = null;
    private Handler reloadHandler = null;

    private Runnable loadRunnable = null;
    private Handler loadHandler = null;

    boolean darkModeEnabled = false;

    boolean readOnly = false;

    private void reloadQuestionListFromDB(){
        loadingDialog.startAlertDialog();
        reloadRunnable = new Runnable() {
            @Override
            public void run() {
                startAllLoaderProcesses();
            }
        };

        reloadHandler = new Handler();
        reloadHandler.postDelayed(reloadRunnable, 3000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onLoadFinishedAlreadyExecuted = false;
        sharedPref = new SharedPref(this);
        // setting the theme style
        if (sharedPref.loadNightModeState()) {
            darkModeEnabled = true;
            setTheme(R.style.darkTheme);
        } else {
            darkModeEnabled = false;
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        readOnly = getIntent().getBooleanExtra("readonly",false);
        Log.d("onCreate", "readOnly: "+readOnly);

        noInternetDialogFragment = new NoInternetDialogFragment();

        reloadBtn = findViewById(R.id.reloadBtn);
        completeBtn = findViewById(R.id.complete_button);
        /*if(readOnly){
            completeBtn.setVisibility(View.GONE);
        }*/

        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadFinishedAlreadyExecuted = false;
                reloadBtn.setVisibility(View.GONE);
                reloadQuestionListFromDB();


               /* destroyAllLoaderProcesses();
                checklistQuestionLoadCompleteReceiver = null;
                checklistSectionLoadCompleteReceiver = null;
                if (choiceDialog != null) {
                    choiceDialog.dismiss();
                }
                onLoadFinishedAlreadyExecuted = false;
                reloadBtn.setVisibility(View.GONE);
                finish();
                startActivity(getIntent());
                overridePendingTransition(0, 0);*/
            }
        });

        commentBoxData = "";

        // Create loading dialog
        loadingDialog = new LoadingDialog(this);
        // Start loading spinner dialog
        loadingDialog.startAlertDialog();

        // Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        //Load drawer layout user information
        MainActivity.loadUserInformation(drawerLayout);

        // Launching checklist with object id key passed from assessment overview
        final Intent launchIntent = getIntent();
        if (launchIntent != null) {
            objectId = launchIntent.getStringExtra(AssessmentOverviewActivity.OBJECT_ID_KEY);
            objectName = launchIntent.getStringExtra(AssessmentOverviewActivity.OBJECT_NAME_KEY);
            applicationId = launchIntent.getStringExtra("applicationId");
            rearingSystem = launchIntent.getStringExtra("rearingSystem");
            animal = launchIntent.getStringExtra("animal");

        }

        // Creating Assessment Question List loader
        curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        Log.d("onCreate", "user id: "+curAccount.getUserId());

        // set the checklist section spinner
        mChecklistSectionItems = new ArrayList<>();
        //mChecklistSectionItems2 = new ArrayList<>();
        spinnerSectionItemList = findViewById(R.id.checklistSectionsMenu); // Get the spinner from the activity_checklist layout
        selectedSection = "";


        // Initialise the AssessmentQuestionObject List
        assessmentQuestionObjectList = new ArrayList<>();

        // Initialise the AssessmentChecklistSectionObject list
        assessmentChecklistSectionObjectList = new ArrayList<>();

        // initialise Checklist Subsection And Standard Recyclers
        checklistSubsections = new ArrayList<>();
        checklistSubsectionRecycler = findViewById(R.id.checklistSubsectionWithStandards);


        // Initialise non compliance list
        nonCompliances = new ArrayList<>();

        // Initialising broadcast receiver class
        checklistQuestionLoadCompleteReceiver = new ChecklistQuestionLoadCompleteReceiver();
        checklistQuestionBroadcastReceiverIsRegistered = new AtomicBoolean(false);
        checklistSectionLoadCompleteReceiver = new ChecklistSectionLoadCompleteReceiver();
        checklistSectionReceiverRegistered = new AtomicBoolean(false);


        // initialise loader callbacks
        assessmentQuestionObjectLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<AssessmentQuestionObject>>() {
            @Override
            public Loader<List<AssessmentQuestionObject>> onCreateLoader(int id, Bundle args) {
                return new AssessmentQuestionListLoader(getApplicationContext(), curAccount, objectId);
            }

            @Override
            public void onLoadFinished(Loader<List<AssessmentQuestionObject>> loader, List<AssessmentQuestionObject> data) {
                if (!onLoadFinishedAlreadyExecuted) {
                    assessmentQuestionObjectList = data;

                    if(assessmentQuestionObjectList!=null &&
                            assessmentQuestionObjectList.size()>0){
                        if(loadHandler!=null && loadRunnable!=null){
                            loadHandler.removeCallbacks(loadRunnable);
                        }
                        if(reloadRunnable!=null && reloadHandler!=null){
                            reloadHandler.removeCallbacks(reloadRunnable);
                        }
                        refreshScreen();
                        Log.d("onCreate", "onLoadFinished execute 1 ");
                        onLoadFinishedAlreadyExecuted = true;
                        reloadBtn.setVisibility(View.GONE);
                    }else{
                        refreshScreen();
                        Log.d("onCreate", "onLoadFinished execute 1 ");
                        onLoadFinishedAlreadyExecuted = true;
                        //delay
                        loadHandler = new Handler();
                        loadRunnable = new Runnable() {
                            @Override
                            public void run() {
                                reloadBtn.setVisibility(View.VISIBLE);
                            }
                        };
                        loadHandler.postDelayed(loadRunnable, 5000);//70000
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<List<AssessmentQuestionObject>> loader) {
                assessmentQuestionObjectList = null;
                refreshScreen();
                Log.d("onCreate", "onLoaderReset execute 2 ");
            }
        };
        assessmentChecklistSectionObjectLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<AssessmentChecklistSectionObject>>() {
            @Override
            public Loader<List<AssessmentChecklistSectionObject>> onCreateLoader(int id, Bundle args) {
                return new AssessmentChecklistSectionLoader(getApplicationContext(), curAccount, objectId);
            }

            @Override
            public void onLoadFinished(Loader<List<AssessmentChecklistSectionObject>> loader, List<AssessmentChecklistSectionObject> data) {
                assessmentChecklistSectionObjectList = data;
            }

            @Override
            public void onLoaderReset(Loader<List<AssessmentChecklistSectionObject>> loader) {
                assessmentChecklistSectionObjectList = null;
                refreshScreen();
                Log.d("onCreate", "onLoaderReset execute 3 ");
            }
        };

        // Initialise openSectionCommentsButton
        openSectionCommentsButton = (Button) findViewById(R.id.openSectionComment);

        if(darkModeEnabled){
            openSectionCommentsButton.setBackground(ContextCompat.getDrawable(
                    ChecklistActivity.this,
                    R.drawable.comment_dark_mode
            ));
        }

        openSectionCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSectionCommentsDialog();
            }
        });

        // initialise tem variable
        tempCheckStandRecyclerAdapter = new ChecklistStandardRecyclerAdapter(null,null, this, null, 0, null, null,null);



       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startAllLoaderProcesses();
            }
        }, 10000);*/
        startAllLoaderProcesses();
    }

    public void takePicture(ChecklistStandardRecyclerAdapter tempCheckStandRecyclerAdapter, int itemPosition) {
        // assign adaptor
        this.tempCheckStandRecyclerAdapter = tempCheckStandRecyclerAdapter;
        this.tempCheckStandRecyclerAdapterItemPosition = itemPosition;

        openOptionsDialog();
    }

    private boolean checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            return true;
        }
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_WRITE_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Open Camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 110);
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }

    public void removePicture(
            ChecklistStandardRecyclerAdapter tempCheckStandRecyclerAdapter,
            int itemPosition
    ) {
        // assign adaptor
        this.tempCheckStandRecyclerAdapter = tempCheckStandRecyclerAdapter;
        this.tempCheckStandRecyclerAdapterItemPosition = itemPosition;

        loadingDialog.startAlertDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition).setmChecklistStandardImageUpload(null);
                tempCheckStandRecyclerAdapter.bitmapList.get(tempCheckStandRecyclerAdapterItemPosition).setBitmap(null);
                tempCheckStandRecyclerAdapter.saveChecklistStandard(tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition),tempCheckStandRecyclerAdapterItemPosition);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tempCheckStandRecyclerAdapter.notifyItemChanged(tempCheckStandRecyclerAdapterItemPosition);
                        loadingDialog.dismissDialog();
                    }
                });
            }
        }).start();
    }


    private void openOptionsDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChecklistActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(ChecklistActivity.this).inflate(R.layout.choice_dialog, viewGroup, false);
        TextView galleryTv = dialogView.findViewById(R.id.galleryTv);
        TextView cameraTv = dialogView.findViewById(R.id.cameraTv);
        builder.setView(dialogView);
        choiceDialog = builder.create();
        galleryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceDialog.dismiss();
                choiceDialog = null;
                //Open gallery
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
            }
        });
        cameraTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choiceDialog.dismiss();
                choiceDialog = null;

                if (checkAndRequestPermissions()) {
                    // Open Camera
                    /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 110);*/

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, 110);
                }

            }
        });
        choiceDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 110 && resultCode == Activity.RESULT_OK) {
            // get capture image
            //Bitmap captureImage = (Bitmap) data.getExtras().get("data");

            Bitmap captureImageTemp = null;
            try {
                captureImageTemp = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                captureImageTemp = null;
            }

            final Bitmap captureImage = getResizedBitmap(captureImageTemp,650);

            Log.d("onCreate", "captureImage bitmap: " + captureImage);

            loadingDialog.startAlertDialog();
            //bitmap to string
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String imgStr = "";
                   // MediaStore.Images.Media.insertImage(getContentResolver(), captureImage, "test", "test");
                    if(captureImage==null){
                        imgStr = "";
                    }else{
                        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                        captureImage.compress(Bitmap.CompressFormat.PNG,100, baos);
                        byte [] b=baos.toByteArray();
                        imgStr = Base64.encodeToString(b, Base64.DEFAULT);
                    }

                    tempCheckStandRecyclerAdapter.bitmapList.get(tempCheckStandRecyclerAdapterItemPosition).setBitmap(captureImage);
                    tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition).setmChecklistStandardImageUploadString(imgStr);
                    tempCheckStandRecyclerAdapter.saveChecklistStandard(tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition),tempCheckStandRecyclerAdapterItemPosition);

                    //---
                   /* tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition).setmChecklistStandardImageUpload(captureImage);
                    tempCheckStandRecyclerAdapter.saveChecklistStandard(tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition));*/

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tempCheckStandRecyclerAdapter.notifyItemChanged(tempCheckStandRecyclerAdapterItemPosition);
                            loadingDialog.dismissDialog();
                        }
                    });
                }
            }).start();




        } else if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            loadingDialog.startAlertDialog();
            //bitmap to string
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap1 = null;
                    try {
                        Bitmap b = MediaStore.Images.Media.getBitmap(ChecklistActivity.this.getContentResolver(), selectedImage);
                        bitmap1 = getResizedBitmap(b, 500);//100
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String imgStr = "";
                    if(bitmap1==null){
                        imgStr = "";
                    }else{
                        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                        bitmap1.compress(Bitmap.CompressFormat.PNG,0, baos);
                        byte [] b=baos.toByteArray();
                        imgStr = Base64.encodeToString(b, Base64.DEFAULT);
                    }
                    //set bitmap1 on list item
                    tempCheckStandRecyclerAdapter.bitmapList.get(tempCheckStandRecyclerAdapterItemPosition).setBitmap(bitmap1);
                    tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition).setmChecklistStandardImageUploadString(imgStr);
                    //tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition).setmChecklistStandardImageUpload(bitmap);
                    tempCheckStandRecyclerAdapter.saveChecklistStandard(tempCheckStandRecyclerAdapter.mStandards.get(tempCheckStandRecyclerAdapterItemPosition),tempCheckStandRecyclerAdapterItemPosition);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //todo: switch back to ui thread
                            tempCheckStandRecyclerAdapter.notifyItemChanged(tempCheckStandRecyclerAdapterItemPosition);
                            loadingDialog.dismissDialog();
                        }
                    });
                }
            }).start();
        }else if(requestCode == LAUNCH_INC_PAGE){
            if(resultCode == Activity.RESULT_OK){
                boolean cont = data.getBooleanExtra("continue",false);
                if(cont){
                    continueNormalProcess();
                }else{
                    String result=data.getStringExtra("sName");
                    selectedSection = result;
                    moveToSelectedSection(selectedSection);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // Write your code if there's no result
                Log.d("onCreate", "onActivityResult: RESULT_CANCELED");
                moveToSelectedSection(selectedSection);
            }
        }
        else {
            Log.d("onCreate", "onActivityResult: error");
        }
    }

    public void openSectionCommentsDialog() {
        ChecklistSectionCommentDialog checklistSectionCommentDialog = new ChecklistSectionCommentDialog(this, selectedAssessmentChecklistSectionObject);
        checklistSectionCommentDialog.startAlertDialog();
    }


    private void reloadUi(String sectionName){
        selectedSection = sectionName;
        // load checklist assessment questions
        loadChecklistSubsections();

        // Active section object
        AssessmentChecklistSectionObject activeSectionObject = null;

        // Active section mapping
        Map<String, Boolean> activeSectionMapping = new HashMap<>();

        // get AssessmentChecklistSection object using section name and update selectedAssessmentChecklistSectionObject variable with the object
        if (assessmentChecklistSectionObjectList != null) {
            for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                if (sectionObject.getSectionName().equals(sectionName)) {
                    selectedAssessmentChecklistSectionObject = sectionObject;
                    Log.d("onCreate", "Selected Assessment Checklist Section Object: " + selectedAssessmentChecklistSectionObject);
                    activeSectionObject = sectionObject;
                    Log.d("onCreate", "activeSectionObject: "+activeSectionObject);
                }
            }

            // Set the 1 section to be active and the rest to be inactive based on the section selected
            for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                if (sectionObject.getObjectId() == activeSectionObject.getObjectId()) {
                    activeSectionMapping.put(sectionObject.getObjectId(), true);
                } else {
                    // Set all the other Section Object Active value to false
                    activeSectionMapping.put(sectionObject.getObjectId(), false);
                }

            }

            if (!activeSectionMapping.isEmpty()) {
                final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, AssessmentChecklistSectionObject.ASSESSMENT_LOOKUP, objectId, AssessmentChecklistSectionObject.SECTION_ORDER, QuerySpec.Order.ascending, 1000);
                JSONArray results;
                List<AssessmentChecklistSectionObject> assessmentChecklistSectionObjects = new ArrayList<>();
                try {
                    results = smartStore.query(querySpec, 0);
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject assessSection = (JSONObject) results.get(i);
                        String assessSectionId = assessSection.optString("Id");
                        if (activeSectionMapping.containsKey(assessSectionId)) {
                            assessSection.put(AssessmentChecklistSectionObject.ACTIVE_SECTION, activeSectionMapping.get(assessSectionId));
                            assessSection.put(SyncTarget.LOCAL, true);
                            assessSection.put(SyncTarget.LOCALLY_UPDATED, true);
                            assessSection.put(SyncTarget.LOCALLY_CREATED, false);
                            assessSection.put(SyncTarget.LOCALLY_DELETED, false);

                            smartStore.upsert(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, assessSection);
                        }

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException occurred while parsing", e);
                }

            }
        }
    }
    public void onSectionSelection(final Spinner spinner) {
        Log.d("onCreate", "onSectionSelection called: ");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onCreate", "onItemSelected called:");
                //((TextView)view).setText(null); // Sets the text to null

                ChecklistSectionItem clickedChecklistSectionItem = (ChecklistSectionItem) parent.getItemAtPosition(position);
                String str = clickedChecklistSectionItem.getSectionName();
                String sectionName = str.substring(0,str.lastIndexOf("  -  "));
                TextView checklistSectionName = (TextView) findViewById(R.id.checklistSectionName);
                final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                if (checklistSectionName != null) {

                    // Setting the section name on the section bar
                    checklistSectionName.setText(sectionName);
                    selectedSection = sectionName;

                    // load checklist assessment questions
                    loadChecklistSubsections();

                    // Active section object
                    AssessmentChecklistSectionObject activeSectionObject = null;

                    // Active section mapping
                    Map<String, Boolean> activeSectionMapping = new HashMap<>();

                    // get AssessmentChecklistSection object using section name and update selectedAssessmentChecklistSectionObject variable with the object
                    if (assessmentChecklistSectionObjectList != null) {
                        for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                            if (sectionObject.getSectionName().equals(sectionName)) {
                                selectedAssessmentChecklistSectionObject = sectionObject;
                                Log.d("onCreate", "Selected Assessment Checklist Section Object: " + selectedAssessmentChecklistSectionObject);
                                activeSectionObject = sectionObject;
                                Log.d("onCreate", "activeSectionObject: "+activeSectionObject);
                            }
                        }

                        // Set the 1 section to be active and the rest to be inactive based on the section selected
                        for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                            if (sectionObject.getObjectId() == activeSectionObject.getObjectId()) {
                                activeSectionMapping.put(sectionObject.getObjectId(), true);
                            } else {
                                // Set all the other Section Object Active value to false
                                activeSectionMapping.put(sectionObject.getObjectId(), false);
                            }

                        }

                        if (!activeSectionMapping.isEmpty()) {
                            //final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                            final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, AssessmentChecklistSectionObject.ASSESSMENT_LOOKUP, objectId, AssessmentChecklistSectionObject.SECTION_ORDER, QuerySpec.Order.ascending, 1000);
                            JSONArray results;
                            List<AssessmentChecklistSectionObject> assessmentChecklistSectionObjects = new ArrayList<>();
                            try {
                                results = smartStore.query(querySpec, 0);
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject assessSection = (JSONObject) results.get(i);
                                    String assessSectionId = assessSection.optString("Id");
                                    if (activeSectionMapping.containsKey(assessSectionId)) {
                                        assessSection.put(AssessmentChecklistSectionObject.ACTIVE_SECTION, activeSectionMapping.get(assessSectionId));
                                        assessSection.put(SyncTarget.LOCAL, true);
                                        assessSection.put(SyncTarget.LOCALLY_UPDATED, true);
                                        assessSection.put(SyncTarget.LOCALLY_CREATED, false);
                                        assessSection.put(SyncTarget.LOCALLY_DELETED, false);

                                        smartStore.upsert(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, assessSection);
                                    }

                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "JSONException occurred while parsing", e);
                            }
                        }
                    }


                }

                reloadDataFromDB(smartStore,sectionName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void reloadDataFromDB(SmartStore smartStore,String sectionName) {
        //load from db
        final QuerySpec assQuestionQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
        JSONArray assQuesResults;
        List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
        try {
            assQuesResults = smartStore.query(assQuestionQuerySpec, 0);
            for (int i = 0; i < assQuesResults.length(); i++) {
                assessmentQuestions.add(new AssessmentQuestionObject(assQuesResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        assessmentQuestionObjectList = assessmentQuestions;
        reloadUi(sectionName);
    }

    private void reloadOnlyDataDataFromDB() {
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        //load from db
        final QuerySpec assQuestionQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
        JSONArray assQuesResults;
        List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
        try {
            assQuesResults = smartStore.query(assQuestionQuerySpec, 0);
            for (int i = 0; i < assQuesResults.length(); i++) {
                assessmentQuestions.add(new AssessmentQuestionObject(assQuesResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        assessmentQuestionObjectList = assessmentQuestions;
        //reloadUi(sectionName);
    }


    /**
     * on Click action for ClickNavMenu
     */
    public void ClickNavMenu(View view) {
        // Open drawer
        MainActivity.openDrawer(drawerLayout);
    }

    /**
     * on click action for ClickCloseNavigation
     */
    public void ClickCloseNavigation(View view) {
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    /**
     * Onclick action for ClickHome
     */
    public void ClickHome(View view) {
        // Redirect activity to home
        MainActivity.redirectActivity(this, MainActivity.class);
    }

    /**
     * redirects activity to profile
     */
    public void ClickProfile(View view) {
        // Redirects activity to profile
        MainActivity.redirectActivity(this, Profile.class);
    }

    /**
     * Redirects activity to Notifications
     */
    public void ClickNotifications(View view) {
        // Redirects activity to Notifications
        MainActivity.redirectActivity(this, Notifications.class);
    }

    /**
     * redirects activity to settings
     */
    public void ClickSettings(View view) {
        // Redirect activity to settings
        MainActivity.redirectActivity(this, SettingsActivity.class);
    }

    /**
     * Displays logout confirmation dialog
     *
     * @param view view element
     */
    public void onClickLogout(View view) {
        if(!InternetUtil.isInternetAvailable(ChecklistActivity.this)){
            noInternetDialogFragment.show(getFragmentManager(), "No Internet");
            return;
        }
        MainActivity.logoutConfirmationDialog.show(getFragmentManager(), "LogoutDialog");
    }

    public void onClickSave(View view) {
        // Get Assessment Questions from Smart Store
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        final QuerySpec assQuestionQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
        JSONArray assQuesResults;
        List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
        try {
            assQuesResults = smartStore.query(assQuestionQuerySpec, 0);
            for (int i = 0; i < assQuesResults.length(); i++) {
                assessmentQuestions.add(new AssessmentQuestionObject(assQuesResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        // Calculate the number of Checklist Standard completed by iterating through the list
        int completed = 0;
        assert assessmentQuestions != null;
        for (AssessmentQuestionObject aq : assessmentQuestions) {
            if (aq.getQuestionAnswered()) {
                completed = completed + 1;
            }
        }

        // Calculate the percentage completion
        int percent = (int) Math.round(((double) completed / assessmentQuestions.size()) * 100);

        // Update Assessment object soup with the progress
        JSONObject assessment;
        assert objectId != null;
        try {
            assessment = smartStore.retrieve(AssessmentListLoader.ASSESSMENT_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentListLoader.ASSESSMENT_SOUP,
                            Constants.ID, objectId)).getJSONObject(0);
            assessment.put(AssessmentObject.ASSESSMENT_COMPLETION, String.valueOf(percent));
            assessment.put(SyncTarget.LOCAL, true);
            assessment.put(SyncTarget.LOCALLY_UPDATED, true);
            assessment.put(SyncTarget.LOCALLY_CREATED, false);
            assessment.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(AssessmentListLoader.ASSESSMENT_SOUP, assessment);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }

        // Calculate the Overall Assessment completion for the application
        final QuerySpec assessmentQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentListLoader.ASSESSMENT_SOUP, AssessmentObject.APPLICATION_ID, applicationId, AssessmentObject.NAME, QuerySpec.Order.ascending, 10000);
        JSONArray assessResults;
        List<AssessmentObject> assessments = new ArrayList<>();
        try {
            assessResults = smartStore.query(assessmentQuerySpec, 0);
            for (int i = 0; i < assessResults.length(); i++) {
                assessments.add(new AssessmentObject(assessResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        int totalAssessmentCompletion = 0;
        for (AssessmentObject assessmentObject : assessments) {
            int completion = 0;
            String completionString = assessmentObject.getAssessmentCompletion();
            if (!completionString.equals("")) {
                completion = Integer.parseInt(completionString);
            }
            totalAssessmentCompletion = totalAssessmentCompletion + completion;
        }

        // Calculate the percentage completion
        int totalAssessmentPercent = (int) Math.round(((double) totalAssessmentCompletion / assessments.size()));

        // Update Application object soup with the progress
        JSONObject application;
        assert applicationId != null;
        try {
            application = smartStore.retrieve(ApplicationListLoader.APPLICATION_SOUP,
                    smartStore.lookupSoupEntryId(ApplicationListLoader.APPLICATION_SOUP,
                            Constants.ID, applicationId)).getJSONObject(0);
            application.put(ApplicationObject.CHECKLIST_COMPLETION, String.valueOf(totalAssessmentPercent));
            application.put(SyncTarget.LOCAL, true);
            application.put(SyncTarget.LOCALLY_UPDATED, true);
            application.put(SyncTarget.LOCALLY_CREATED, false);
            application.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(ApplicationListLoader.APPLICATION_SOUP, application);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }


        Toast.makeText(this, "Save successful!", Toast.LENGTH_LONG).show();
        requestSync(false);
        finish();


    }

    public void saveAllProgress() {
        // Get Assessment Questions from Smart Store
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        final QuerySpec assQuestionQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
        JSONArray assQuesResults;
        List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
        try {
            assQuesResults = smartStore.query(assQuestionQuerySpec, 0);
            for (int i = 0; i < assQuesResults.length(); i++) {
                assessmentQuestions.add(new AssessmentQuestionObject(assQuesResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        // Calculate the number of Checklist Standard completed by iterating through the list
        int completed = 0;
        assert assessmentQuestions != null;
        for (AssessmentQuestionObject aq : assessmentQuestions) {
            if (aq.getQuestionAnswered()) {
                completed = completed + 1;
            }
        }

        // Calculate the percentage completion
        int percent = (int) Math.round(((double) completed / assessmentQuestions.size()) * 100);

        // Update Assessment object soup with the progress
        JSONObject assessment;
        assert objectId != null;
        try {
            assessment = smartStore.retrieve(AssessmentListLoader.ASSESSMENT_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentListLoader.ASSESSMENT_SOUP,
                            Constants.ID, objectId)).getJSONObject(0);
            assessment.put(AssessmentObject.ASSESSMENT_COMPLETION, String.valueOf(percent));
            assessment.put(SyncTarget.LOCAL, true);
            assessment.put(SyncTarget.LOCALLY_UPDATED, true);
            assessment.put(SyncTarget.LOCALLY_CREATED, false);
            assessment.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(AssessmentListLoader.ASSESSMENT_SOUP, assessment);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }

        // Calculate the Overall Assessment completion for the application
        final QuerySpec assessmentQuerySpec = QuerySpec.buildExactQuerySpec(AssessmentListLoader.ASSESSMENT_SOUP, AssessmentObject.APPLICATION_ID, applicationId, AssessmentObject.NAME, QuerySpec.Order.ascending, 10000);
        JSONArray assessResults;
        List<AssessmentObject> assessments = new ArrayList<>();
        try {
            assessResults = smartStore.query(assessmentQuerySpec, 0);
            for (int i = 0; i < assessResults.length(); i++) {
                assessments.add(new AssessmentObject(assessResults.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        int totalAssessmentCompletion = 0;
        for (AssessmentObject assessmentObject : assessments) {
            int completion = 0;
            String completionString = assessmentObject.getAssessmentCompletion();
            if (!completionString.equals("")) {
                completion = Integer.parseInt(completionString);
            }
            totalAssessmentCompletion = totalAssessmentCompletion + completion;
        }

        // Calculate the percentage completion
        int totalAssessmentPercent = (int) Math.round(((double) totalAssessmentCompletion / assessments.size()));

        // Update Application object soup with the progress
        JSONObject application;
        assert applicationId != null;
        try {
            application = smartStore.retrieve(ApplicationListLoader.APPLICATION_SOUP,
                    smartStore.lookupSoupEntryId(ApplicationListLoader.APPLICATION_SOUP,
                            Constants.ID, applicationId)).getJSONObject(0);
            application.put(ApplicationObject.CHECKLIST_COMPLETION, String.valueOf(totalAssessmentPercent));
            application.put(SyncTarget.LOCAL, true);
            application.put(SyncTarget.LOCALLY_UPDATED, true);
            application.put(SyncTarget.LOCALLY_CREATED, false);
            application.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(ApplicationListLoader.APPLICATION_SOUP, application);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }


        //Toast.makeText(this, "Save successful!", Toast.LENGTH_LONG).show();
        requestSync(false);
        //finish();


    }


    /*private void showIncompleteDialog(String sName,int totalNoAns,int totalNoComments){
        final AlertDialog.Builder builder = new AlertDialog.Builder(ChecklistActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(ChecklistActivity.this).inflate(R.layout.incomplete_dialog, viewGroup, false);
        TextView okTv=dialogView.findViewById(R.id.okTv);
        TextView continueTv=dialogView.findViewById(R.id.continueTv);
        TextView sectionName=dialogView.findViewById(R.id.sectionName);
        TextView unAnsTv=dialogView.findViewById(R.id.unAnsTv);
        TextView noCommentTv=dialogView.findViewById(R.id.noCommentTv);

        sectionName.setText(sName);
        if(totalNoAns>0){
            unAnsTv.setText("Unanswered questions = "+totalNoAns);
        }else{
            unAnsTv.setText("");
        }
        if(totalNoComments>0){
            noCommentTv.setText("Questions without comments = "+totalNoComments);
        }else{
            noCommentTv.setText("");
        }


        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        continueTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // Get all assessment questions for assessment
                final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
                JSONArray results;
                List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
                try {
                    results = smartStore.query(querySpec, 0);
                    for (int i = 0; i < results.length(); i++) {
                        assessmentQuestions.add(new AssessmentQuestionObject(results.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException occurred while parsing", e);
                } catch (SmartSqlHelper.SmartSqlException e) {
                    Log.e(TAG, "SmartSqlException occurred while fetching data", e);
                }

                for (AssessmentQuestionObject aqo : assessmentQuestions) {
                    if (aqo.getCOMPLIANT().equals("No")) {
                        nonCompliances.add(new ChecklistStandard(
                                aqo.getObjectId(),
                                aqo.getSECTION(),
                                aqo.getSUBSECTION(),
                                aqo.getSTANDARD(),
                                aqo.getSUMMARY(),
                                aqo.getCOMPLIANT(),
                                aqo.getCommentsAction(),
                                aqo.getSectionOrder(),
                                aqo.getSubsectionOrder(),
                                aqo.getStandardOrder(),
                                aqo.getGuidanceNotes(),
                                aqo.getQuestionAnswered().toString(),
                                aqo.getImageUploadLocal(),
                                aqo.getEvidenceRequired(),
                                aqo.getOtherEvidence(),
                                aqo.getCorrection())
                        );

                        // sort the non compliance list
                        Comparator<ChecklistStandard> compareByStandardOrder = new Comparator<ChecklistStandard>() {
                            @Override
                            public int compare(ChecklistStandard o1, ChecklistStandard o2) {
                                return o1.getmChecklistStandardOrder().compareTo(o2.getmChecklistStandardOrder());
                            }
                        };
                        Collections.sort(nonCompliances, compareByStandardOrder);
                    }
                }
                launchNonComplianceReport();
            }
        });
        alertDialog.show();
    }*/

    private void continueNormalProcess(){
        // Get all assessment questions for assessment
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
        JSONArray results;
        List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
        try {
            results = smartStore.query(querySpec, 0);
            for (int i = 0; i < results.length(); i++) {
                assessmentQuestions.add(new AssessmentQuestionObject(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e(TAG, "SmartSqlException occurred while fetching data", e);
        }

        for (AssessmentQuestionObject aqo : assessmentQuestions) {
            if (aqo.getCOMPLIANT().equals("No")) {
                nonCompliances.add(new ChecklistStandard(
                        aqo.getObjectId(),
                        aqo.getSECTION(),
                        aqo.getSUBSECTION(),
                        aqo.getSTANDARD(),
                        aqo.getSUMMARY(),
                        aqo.getCOMPLIANT(),
                        aqo.getCommentsAction(),
                        aqo.getSectionOrder(),
                        aqo.getSubsectionOrder(),
                        aqo.getStandardOrder(),
                        aqo.getGuidanceNotes(),
                        aqo.getQuestionAnswered().toString(),
                        aqo.getImageUploadLocal(),
                        aqo.getEvidenceRequired(),
                        aqo.getOtherEvidence(),
                        aqo.getCorrection())
                );

                // sort the non compliance list
                Comparator<ChecklistStandard> compareByStandardOrder = new Comparator<ChecklistStandard>() {
                    @Override
                    public int compare(ChecklistStandard o1, ChecklistStandard o2) {
                        return o1.getmChecklistStandardOrder().compareTo(o2.getmChecklistStandardOrder());
                    }
                };
                Collections.sort(nonCompliances, compareByStandardOrder);
            }
        }
        launchNonComplianceReport();
    }
    private void showIncompleteDialog(List<ChecklistQStatus> statusList){
        Log.d("onCreate", "selectedSection: "+selectedSection);
        ArrayList<ChecklistQStatus> qList = new ArrayList<ChecklistQStatus>(statusList);
        Intent intent = new Intent(ChecklistActivity.this, IncompleteActivity.class);
        intent.putExtra("test",qList);
        intent.putExtra("sectionName",selectedSection);
        startActivityForResult(intent, LAUNCH_INC_PAGE);

        //continueNormalProcess();
        //moveToSelectedSection("");




    /*    final AlertDialog.Builder builder = new AlertDialog.Builder(ChecklistActivity.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(ChecklistActivity.this).inflate(R.layout.incomplete_dialog, viewGroup, false);
        RecyclerView statusListRv=dialogView.findViewById(R.id.statusListRv);
        TextView okTv = dialogView.findViewById(R.id.okTv);
        TextView continueTv = dialogView.findViewById(R.id.continueTv);



        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        IncompleteListAdapter adapter = new IncompleteListAdapter(
                statusList,
                ChecklistActivity.this,
                new IncompleteQuestion() {
                    @Override
                    public void moveToSection(ChecklistQStatus checklistQStatus) {
                        String section = checklistQStatus.getSectionName().trim();
                        Log.d("onCreate", "moveToSection: "+section);
                        alertDialog.dismiss();
                        moveToSelectedSection(section);
                    }
                });


        statusListRv.setLayoutManager(new LinearLayoutManager(ChecklistActivity.this));
        statusListRv.setAdapter(adapter);

        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        continueTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                // Get all assessment questions for assessment
                final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
                JSONArray results;
                List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
                try {
                    results = smartStore.query(querySpec, 0);
                    for (int i = 0; i < results.length(); i++) {
                        assessmentQuestions.add(new AssessmentQuestionObject(results.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException occurred while parsing", e);
                } catch (SmartSqlHelper.SmartSqlException e) {
                    Log.e(TAG, "SmartSqlException occurred while fetching data", e);
                }

                for (AssessmentQuestionObject aqo : assessmentQuestions) {
                    if (aqo.getCOMPLIANT().equals("No")) {
                        nonCompliances.add(new ChecklistStandard(
                                aqo.getObjectId(),
                                aqo.getSECTION(),
                                aqo.getSUBSECTION(),
                                aqo.getSTANDARD(),
                                aqo.getSUMMARY(),
                                aqo.getCOMPLIANT(),
                                aqo.getCommentsAction(),
                                aqo.getSectionOrder(),
                                aqo.getSubsectionOrder(),
                                aqo.getStandardOrder(),
                                aqo.getGuidanceNotes(),
                                aqo.getQuestionAnswered().toString(),
                                aqo.getImageUploadLocal(),
                                aqo.getEvidenceRequired(),
                                aqo.getOtherEvidence(),
                                aqo.getCorrection())
                        );

                        // sort the non compliance list
                        Comparator<ChecklistStandard> compareByStandardOrder = new Comparator<ChecklistStandard>() {
                            @Override
                            public int compare(ChecklistStandard o1, ChecklistStandard o2) {
                                return o1.getmChecklistStandardOrder().compareTo(o2.getmChecklistStandardOrder());
                            }
                        };
                        Collections.sort(nonCompliances, compareByStandardOrder);
                    }
                }
                launchNonComplianceReport();
            }
        });

        alertDialog.show();*/
    }

    private void moveToSelectedSection(String sectionName) {
        TextView checklistSectionName = (TextView) findViewById(R.id.checklistSectionName);
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        if (checklistSectionName != null) {

            // Setting the section name on the section bar
            checklistSectionName.setText(sectionName);
            selectedSection = sectionName;

            // load checklist assessment questions
            loadChecklistSubsections();

            // Active section object
            AssessmentChecklistSectionObject activeSectionObject = null;

            // Active section mapping
            Map<String, Boolean> activeSectionMapping = new HashMap<>();

            // get AssessmentChecklistSection object using section name and update selectedAssessmentChecklistSectionObject variable with the object
            if (assessmentChecklistSectionObjectList != null) {
                for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                    if (sectionObject.getSectionName().equals(sectionName)) {
                        selectedAssessmentChecklistSectionObject = sectionObject;
                        Log.d("onCreate", "Selected Assessment Checklist Section Object: " + selectedAssessmentChecklistSectionObject);
                        activeSectionObject = sectionObject;
                        Log.d("onCreate", "activeSectionObject: "+activeSectionObject);
                    }
                }

                // Set the 1 section to be active and the rest to be inactive based on the section selected
                for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                    if (sectionObject.getObjectId() == activeSectionObject.getObjectId()) {
                        activeSectionMapping.put(sectionObject.getObjectId(), true);
                    } else {
                        // Set all the other Section Object Active value to false
                        activeSectionMapping.put(sectionObject.getObjectId(), false);
                    }

                }

                if (!activeSectionMapping.isEmpty()) {
                    //final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
                    final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, AssessmentChecklistSectionObject.ASSESSMENT_LOOKUP, objectId, AssessmentChecklistSectionObject.SECTION_ORDER, QuerySpec.Order.ascending, 1000);
                    JSONArray results;
                    List<AssessmentChecklistSectionObject> assessmentChecklistSectionObjects = new ArrayList<>();
                    try {
                        results = smartStore.query(querySpec, 0);
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject assessSection = (JSONObject) results.get(i);
                            String assessSectionId = assessSection.optString("Id");
                            if (activeSectionMapping.containsKey(assessSectionId)) {
                                assessSection.put(AssessmentChecklistSectionObject.ACTIVE_SECTION, activeSectionMapping.get(assessSectionId));
                                assessSection.put(SyncTarget.LOCAL, true);
                                assessSection.put(SyncTarget.LOCALLY_UPDATED, true);
                                assessSection.put(SyncTarget.LOCALLY_CREATED, false);
                                assessSection.put(SyncTarget.LOCALLY_DELETED, false);

                                smartStore.upsert(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, assessSection);
                            }

                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException occurred while parsing", e);
                    }
                }
            }


        }

        reloadDataFromDB(smartStore,sectionName);
    }


    public void onClickComplete(View view){
        if(readOnly){
            Toast.makeText(this, "This section is already completed", Toast.LENGTH_SHORT).show();
            return;
        }
        List<ChecklistStandard> backupChecklistStandards = new ArrayList<>();
        backupChecklistStandards.addAll(checklistStandards);

        List<ChecklistSubsection> backupChecklistSubsections = new ArrayList<>();
        backupChecklistSubsections.addAll(checklistSubsections);

        List<ChecklistQStatus> incompleteList = checkAllQuestionsCompletionStatus();

        checklistStandards = backupChecklistStandards;
        checklistSubsections = backupChecklistSubsections;

        boolean incomplete = false;

        for(int i=0;i< incompleteList.size();i++){
            ChecklistQStatus checklistQStatus = incompleteList.get(i);
            if(checklistQStatus.getNotAns()>0 || checklistQStatus.getNoComments()>0){
                incomplete = true;
                break;
            }
        }



        if(incomplete){
            List<ChecklistQStatus> filteredList =  incompleteList.stream().filter((o)->{
                return (o.getNotAns()>0 || o.getNoComments()>0);
            }).collect(Collectors.toList());
            //show incomplete dialog
            showIncompleteDialog(filteredList);
        }else{
            // Get all assessment questions for assessment
            final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
            final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
            JSONArray results;
            List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
            try {
                results = smartStore.query(querySpec, 0);
                for (int i = 0; i < results.length(); i++) {
                    assessmentQuestions.add(new AssessmentQuestionObject(results.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException occurred while parsing", e);
            } catch (SmartSqlHelper.SmartSqlException e) {
                Log.e(TAG, "SmartSqlException occurred while fetching data", e);
            }

            for (AssessmentQuestionObject aqo : assessmentQuestions) {
                if (aqo.getCOMPLIANT().equals("No")) {
                    nonCompliances.add(new ChecklistStandard(
                            aqo.getObjectId(),
                            aqo.getSECTION(),
                            aqo.getSUBSECTION(),
                            aqo.getSTANDARD(),
                            aqo.getSUMMARY(),
                            aqo.getCOMPLIANT(),
                            aqo.getCommentsAction(),
                            aqo.getSectionOrder(),
                            aqo.getSubsectionOrder(),
                            aqo.getStandardOrder(),
                            aqo.getGuidanceNotes(),
                            aqo.getQuestionAnswered().toString(),
                            aqo.getImageUploadLocal(),
                            aqo.getEvidenceRequired(),
                            aqo.getOtherEvidence(),
                            aqo.getCorrection())
                    );

                    // sort the non compliance list
                    Comparator<ChecklistStandard> compareByStandardOrder = new Comparator<ChecklistStandard>() {
                        @Override
                        public int compare(ChecklistStandard o1, ChecklistStandard o2) {
                            return o1.getmChecklistStandardOrder().compareTo(o2.getmChecklistStandardOrder());
                        }
                    };
                    Collections.sort(nonCompliances, compareByStandardOrder);
                }
            }
            launchNonComplianceReport();
        }
    }
    /*public void onClickComplete(View view) {
        List<ChecklistStandard> backupChecklistStandards = new ArrayList<>();
        backupChecklistStandards.addAll(checklistStandards);

        List<ChecklistSubsection> backupChecklistSubsections = new ArrayList<>();
        backupChecklistSubsections.addAll(checklistSubsections);

        checkAllQuestionsCompletionStatus();

        checklistStandards = backupChecklistStandards;
        checklistSubsections = backupChecklistSubsections;

        Log.d("onCreate", "main section name: "+selectedSection);
        totalNotAns = new AtomicInteger(0);
        totalNoComments = new AtomicInteger(0);
        checklistSubsections.forEach((checklistSubsection)->{
            Log.d("onCreate", "section name: "+checklistSubsection.getSubsectionName());
            checklistSubsection.getChecklistStandards().forEach((checkListStandard)->{
                boolean isAnswered = checkListStandard.getChecklistStandardQuestionAnswered();
                String comment = checkListStandard.getmChecklistStandardNonCompliance();
                Log.d("onCreate", "is answered : "+isAnswered);
                Log.d("onCreate", "comment : "+comment);
                if(!isAnswered) totalNotAns.getAndIncrement();
                if(comment.trim().isEmpty()) totalNoComments.getAndIncrement();
            });
        });
        Log.d("onCreate", "totalNotAns: "+totalNotAns.get());
        Log.d("onCreate", "totalNoComments: "+totalNoComments.get());


        Log.d("onCreate", "main section name: "+selectedSection);
        Log.d("onCreate", "totalNotAns: "+totalNotAns.get());
        Log.d("onCreate", "totalNoComments: "+totalNoComments.get());

        if(totalNotAns.get()>0 || totalNoComments.get()>0){
            showIncompleteDialog(selectedSection,totalNotAns.get(),totalNoComments.get());
        }else{
            // Get all assessment questions for assessment
            final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
            final QuerySpec querySpec = QuerySpec.buildExactQuerySpec(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, AssessmentQuestionObject.ASSESSMENT_CHECKLIST, objectId, AssessmentQuestionObject.STANDARD, QuerySpec.Order.ascending, 10000);
            JSONArray results;
            List<AssessmentQuestionObject> assessmentQuestions = new ArrayList<>();
            try {
                results = smartStore.query(querySpec, 0);
                for (int i = 0; i < results.length(); i++) {
                    assessmentQuestions.add(new AssessmentQuestionObject(results.getJSONObject(i)));
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException occurred while parsing", e);
            } catch (SmartSqlHelper.SmartSqlException e) {
                Log.e(TAG, "SmartSqlException occurred while fetching data", e);
            }

            for (AssessmentQuestionObject aqo : assessmentQuestions) {
                if (aqo.getCOMPLIANT().equals("No")) {
                    nonCompliances.add(new ChecklistStandard(
                            aqo.getObjectId(),
                            aqo.getSECTION(),
                            aqo.getSUBSECTION(),
                            aqo.getSTANDARD(),
                            aqo.getSUMMARY(),
                            aqo.getCOMPLIANT(),
                            aqo.getCommentsAction(),
                            aqo.getSectionOrder(),
                            aqo.getSubsectionOrder(),
                            aqo.getStandardOrder(),
                            aqo.getGuidanceNotes(),
                            aqo.getQuestionAnswered().toString(),
                            aqo.getImageUploadLocal(),
                            aqo.getEvidenceRequired(),
                            aqo.getOtherEvidence(),
                            aqo.getCorrection())
                    );

                    // sort the non compliance list
                    Comparator<ChecklistStandard> compareByStandardOrder = new Comparator<ChecklistStandard>() {
                        @Override
                        public int compare(ChecklistStandard o1, ChecklistStandard o2) {
                            return o1.getmChecklistStandardOrder().compareTo(o2.getmChecklistStandardOrder());
                        }
                    };
                    Collections.sort(nonCompliances, compareByStandardOrder);
                }
            }
            launchNonComplianceReport();
        }

    }*/


    private void launchNonComplianceReport() {
        final Intent detailIntent = new Intent(this, ChecklistReport.class);
        detailIntent.putExtra("applicationId", applicationId);
        detailIntent.putExtra("rearingSystem", rearingSystem);
        detailIntent.putExtra("animal", animal);
        detailIntent.putParcelableArrayListExtra("nonCompliances", nonCompliances);
        detailIntent.putExtra("assessmentId", objectId);
        detailIntent.putExtra("assessmentName", objectName);

        startActivity(detailIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
     /*   AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Return without saving");
        builder.setMessage("Are you sure you want to go back without saving?");
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    final Intent detailIntent = new Intent(this, AssessmentOverviewActivity.class);
                    detailIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    detailIntent.putExtra("object_id", applicationId);
                    startActivity(detailIntent);
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();*/
    }


    @Override
    public void onPause() {
        Log.d("onCreate", "onPause CheckListActivity: called");
        super.onPause();
        saveAllProgress();//added later to auto save all progress
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    private void destroyAllLoaderProcesses() {
        // Unregister broadcast receiver
        if (checklistQuestionBroadcastReceiverIsRegistered.get()) {
            unregisterReceiver(checklistQuestionLoadCompleteReceiver);
        }
        checklistQuestionBroadcastReceiverIsRegistered.set(false);
        // destroy loader
        getLoaderManager().destroyLoader(ASSESSMENT_QUESTION_LOADER_ID);
        assessmentQuestionObjectList = null;

        // Unregister broadcast receiver
        if (checklistSectionReceiverRegistered.get()) {
            unregisterReceiver(checklistSectionLoadCompleteReceiver);
        }
        checklistSectionReceiverRegistered.set(false);
        // destroy loader
        getLoaderManager().destroyLoader(ASSESSMENT_CHECKLIST_SECTION_LOADER_ID);
        assessmentChecklistSectionObjectList = null;
    }

    @Override
    public void onDestroy() {
        if(loadHandler!=null && loadRunnable!=null){
            loadHandler.removeCallbacks(loadRunnable);
        }
        if(reloadRunnable!=null && reloadHandler!=null){
            reloadHandler.removeCallbacks(reloadRunnable);
        }
        Log.d("onCreate", "onDestroy CheckListActivity: called");
        destroyAllLoaderProcesses();
        checklistQuestionLoadCompleteReceiver = null;
        checklistSectionLoadCompleteReceiver = null;
        if (choiceDialog != null) {
            choiceDialog.dismiss();
        }
        onLoadFinishedAlreadyExecuted = false;
        super.onDestroy();
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
    }

    private void startAllLoaderProcesses() {

        getLoaderManager().initLoader(ASSESSMENT_QUESTION_LOADER_ID, null, assessmentQuestionObjectLoaderCallbacks).forceLoad();
        getLoaderManager().initLoader(ASSESSMENT_CHECKLIST_SECTION_LOADER_ID, null, assessmentChecklistSectionObjectLoaderCallbacks).forceLoad();

        // Registering broadcast receiver
        if (!checklistQuestionBroadcastReceiverIsRegistered.get()) {
            registerReceiver(checklistQuestionLoadCompleteReceiver,
                    new IntentFilter(AssessmentQuestionListLoader.LOAD_COMPLETE_INTENT_ACTION));
        }
        checklistQuestionBroadcastReceiverIsRegistered.set(true);

        if (!checklistSectionReceiverRegistered.get()) {
            registerReceiver(checklistSectionLoadCompleteReceiver,
                    new IntentFilter(AssessmentChecklistSectionLoader.LOAD_COMPLETE_INTENT_ACTION));
        }
        checklistSectionReceiverRegistered.set(true);

        // Setup periodic sync
        setupPeriodicSync();

        // Sync now
        requestSync(true /* sync down only */);

        // Periodic clean ghost job starting
        setupPeriodicCleanGhosts(ASSESSMENT_QUESTION_SYNC_DOWN_NAME, CHECKLIST_QUESTION_JOB_ID);
        setupPeriodicCleanGhosts(ASSESSMENT_CHECKLIST_SECTION_SYNC_DOWN_NAME, CHECKLIST_SECTION_JOB_ID);
    }

    /**
     * Schedule job to periodically clean ghost records from smart store
     */
    public void setupPeriodicCleanGhosts(String syncName, int jobId) {
        Boolean backgroundServicesState = sharedPref.loadBackgroundServicesState();
        Boolean jobAlreadyRunning = isJobServiceOn(this, jobId);
        Log.d(TAG, "Job Already Running => " + jobAlreadyRunning);
        if (backgroundServicesState && !jobAlreadyRunning) {
            ComponentName componentName = new ComponentName(this, BackgroundJobService.class);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putString("SyncName", syncName);
            JobInfo info = new JobInfo.Builder(jobId, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(60 * 60 * 1000)
                    .setExtras(bundle)
                    .build();

            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(info);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "Job Scheduled.");
            } else {
                Log.d(TAG, "Job Scheduling Failed.");
            }
        }
    }

    /**
     * Checks if Job Service is already pending
     *
     * @param context
     * @return
     */
    public static boolean isJobServiceOn(Context context, int jobId) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == jobId) {
                hasBeenScheduled = true;
                break;
            }
        }

        return hasBeenScheduled;
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
        ContentResolver.setSyncAutomatically(account, CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY,
                Bundle.EMPTY, SYNC_FREQUENCY_ONE_HOUR);

        ContentResolver.setSyncAutomatically(account, CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY, true);
        ContentResolver.addPeriodicSync(account, CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY,
                Bundle.EMPTY, SYNC_FREQUENCY_ONE_HOUR);
    }

    /**
     * Request a sync
     *
     * @param syncDownOnly if true, only a sync down is done, if false a sync up followed by a sync down is done
     */
    private void requestSync(boolean syncDownOnly) {
        Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();

        Bundle checklistQuestionExtras = new Bundle();
        checklistQuestionExtras.putBoolean(AssessmentQuestionSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
        ContentResolver.requestSync(account, CHECKLIST_QUESTION_SYNC_CONTENT_AUTHORITY, checklistQuestionExtras);

        Bundle checklistSectionExtras = new Bundle();
        checklistSectionExtras.putBoolean(AssessmentChecklistSectionSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
        ContentResolver.requestSync(account, CHECKLIST_SECTION_SYNC_CONTENT_AUTHORITY, checklistSectionExtras);

        Bundle assessmentExtras = new Bundle();
        assessmentExtras.putBoolean(AssessmentSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
        ContentResolver.requestSync(account, ASSESSMENT_SYNC_CONTENT_AUTHORITY, assessmentExtras);
    }


    private void refreshList() {
        getLoaderManager().getLoader(ASSESSMENT_QUESTION_LOADER_ID).forceLoad();
        getLoaderManager().getLoader(ASSESSMENT_CHECKLIST_SECTION_LOADER_ID).forceLoad();
    }

    private void refreshScreen() {
        Log.d("onCreate", "refreshScreen: called");
        if (assessmentQuestionObjectList != null && assessmentQuestionObjectList.size() > 0) {
            setText((TextView) findViewById(R.id.checklistAssessmentName), objectName);

            // Sort the Assessment Question Object list in order of Section, Subsection and Standard Order
            assessmentQuestionObjectList = new ArrayList<>(sortAssessmentChecklist(assessmentQuestionObjectList)); // Update the global variable with the sorted list

            // load the checklist sections for the dropdown
            loadChecklistSectionOptions();
            // set the adapter
            if (mChecklistSectionItems.size() > 0) {
                //mChecklistSectionSpinnerAdapter = new ChecklistSectionSpinnerAdapter(this, mChecklistSectionItems); // set the mChecklistSectionItems to the adapter
                mChecklistSectionSpinnerAdapter = new ChecklistSectionSpinnerAdapter(this, mChecklistSectionItems); // set the mChecklistSectionItems to the adapter
                spinnerSectionItemList.setAdapter(mChecklistSectionSpinnerAdapter); // set the adapter for the spinner


                onSectionSelection(spinnerSectionItemList); // Handles on item selection

                // Select the active section
                if (assessmentChecklistSectionObjectList != null) {
                    String activeSectionName = "";
                    int activeChecklistSectionItemPosition = 0;
                    for (AssessmentChecklistSectionObject sectionObject : assessmentChecklistSectionObjectList) {
                        if (sectionObject.getActiveSection()) {
                            activeSectionName = sectionObject.getSectionName();
                        }
                    }
                    if (activeSectionName != "") {
                        for (int i = 0; i < mChecklistSectionItems.size(); i++) {
                            ChecklistSectionItem csi = mChecklistSectionItems.get(i);
                            if (csi.getSectionName().equals(activeSectionName)) {
                                activeChecklistSectionItemPosition = i;
                            }
                        }

                        spinnerSectionItemList.setSelection(mChecklistSectionSpinnerAdapter.getPosition(mChecklistSectionItems.get(activeChecklistSectionItemPosition)));
                    }


                }
            }
        }

        // dismiss loading spinner
        loadingDialog.dismissDialog();
    }


    private void loadChecklistSectionOptions() {

        ArrayList<ChecklistSectionItem> mChecklistSectionItemsBackup = new ArrayList<>(mChecklistSectionItems);

        // Using a linked set to keep the order of the checklist sections
        LinkedHashSet<String> checklistSectionsSet = new LinkedHashSet<>();
        List<Dropdown> dropdownList = new ArrayList<>();

        // Iterate through the ordered assessment question object and add to the linked hash set

        assessmentQuestionObjectList.forEach((o)->{
            Log.d("onCreate", "o.getSECTION():-> "+o.getSECTION());
        });

        for (AssessmentQuestionObject aqo : assessmentQuestionObjectList) {
            //Log.d("onCreate", "section name : "+aqo.getSECTION());
            //Log.d("onCreate", "section isAnswered : "+aqo.getQuestionAnswered());
            checklistSectionsSet.add(aqo.getSECTION());
            dropdownList.add(new Dropdown(aqo.getSECTION(),aqo.getQuestionAnswered()));
        }

        //-------starts-------
        Map<String,DropdownValue> dropdownMap = new LinkedHashMap<>();

        checklistSectionsSet.forEach((value)->{
            AtomicInteger total = new AtomicInteger(0);
            AtomicInteger ans = new AtomicInteger(0);
            dropdownList.forEach((dValue)->{

                if(value.equals(dValue.getName())){
                    total.getAndIncrement();
                    if(dValue.isAnswered()){
                        ans.getAndIncrement();
                    }
                    dropdownMap.put(value,new DropdownValue(total.get(), ans.get(),0));
                }
            });
        });


        Map<String,DropdownValue> finalDropdownMap = new LinkedHashMap<>();
        dropdownMap.forEach((k,v)->{
            //System.out.println(k+" -> "+v);
            if(v.getTotalCount()>0){
                int percent = (int) Math.round(((double) v.getTotalAns() / v.getTotalCount()) * 100);
                DropdownValue d = new DropdownValue(v.getTotalCount(),v.getTotalAns(),percent);
                finalDropdownMap.put(k,d);
            }else{
                int percent = 0;
                DropdownValue d = new DropdownValue(v.getTotalCount(),v.getTotalAns(),percent);
                finalDropdownMap.put(k,d);
            }
        });

        LinkedHashSet<DropdownVal> finalSet = new LinkedHashSet<>();
        finalDropdownMap.forEach((k,v)->{
            finalSet.add(new DropdownVal(k,k+"  -  "+v.getPercent()+" %"));
            // finalSet.add(k+"  -  "+v.getPercent()+" %");
        });


        finalSet.forEach((v)->{
            //Log.d("onCreate", "dropdown value : "+v);
        });
        //--------ends--------

        // If there are any checklist sections then iterate through those and add them to the checklist section items for the recycler
        /*if (checklistSectionsSet.size() > 0) {
            mChecklistSectionItems.clear();
            for (String s : checklistSectionsSet) {
                mChecklistSectionItems.add(new ChecklistSectionItem(s));
            }
        }*/
        if (finalSet.size() > 0) {
            mChecklistSectionItems.clear();
            for (DropdownVal o : finalSet) {
                mChecklistSectionItems.add(new ChecklistSectionItem(o.getId(),o.getName()));
            }
        }




        //---modified
        if(mChecklistSectionItemsBackup.size()>0){
            Log.d("onCreate", "mChecklistSectionItemsBackup: "+mChecklistSectionItemsBackup);
            List<ChecklistSectionItem> tmpList =  new ArrayList<>();
            for(int i=0;i<mChecklistSectionItemsBackup.size();i++){
                tmpList.add(null);
            }

            Log.d("onCreate", "tmpList: "+tmpList);

            for(int i =0 ; i<mChecklistSectionItemsBackup.size();i++){
                ChecklistSectionItem original = mChecklistSectionItemsBackup.get(i);
                for (int j=0;j<mChecklistSectionItems.size();j++){
                    if(original.getId().equals(mChecklistSectionItems.get(j).getId())){
                        tmpList.set(i,mChecklistSectionItems.get(j));
                        break;
                    }
                }
            }


            Log.d("onCreate", "tmpList: "+tmpList);
            mChecklistSectionItems.clear();
            mChecklistSectionItems.addAll(tmpList);

            mChecklistSectionItems.forEach((v)->{
                Log.d("onCreate", "dropdown value : "+v.getSectionName());
            });
        }else{
            mChecklistSectionItems.forEach((v)->{
                Log.d("onCreate", "dropdown value : "+v.getSectionName());
            });
        }

        //---modified

        mChecklistSectionItems.forEach((v)->{
            Log.d("onCreate", "dropdown option name : "+v.getId());
        });


    }


    private List<ChecklistQStatus> checkAllQuestionsCompletionStatus(){
        reloadOnlyDataDataFromDB();
        List<ChecklistQStatus> statusList = new ArrayList<>();
        mChecklistSectionItems.forEach((v)->{
            Log.d("value", "dropdown option name : "+v.getId());
            String selectedDropdownOption = v.getId();
            if (assessmentQuestionObjectList != null && assessmentQuestionObjectList.size() > 0){
                checklistStandards = new ArrayList<>();
                for (AssessmentQuestionObject aqo : assessmentQuestionObjectList) {
                    if (aqo.getSECTION().trim().equals(selectedDropdownOption.trim())) {
                        Log.d(TAG, "Subsection Order:  " + aqo.getSubsectionOrder() + " => " + aqo.getSubsectionOrder());
                        checklistStandards.add(new ChecklistStandard(
                                aqo.getObjectId(),
                                aqo.getSECTION(),
                                aqo.getSUBSECTION(),
                                aqo.getSTANDARD(),
                                aqo.getSUMMARY(),
                                aqo.getCOMPLIANT(),
                                aqo.getCommentsAction(),
                                aqo.getSectionOrder(),
                                aqo.getSubsectionOrder(),
                                aqo.getStandardOrder(),
                                aqo.getGuidanceNotes(),
                                aqo.getQuestionAnswered().toString(),
                                aqo.getImageUploadLocal(),
                                aqo.getEvidenceRequired(),
                                aqo.getOtherEvidence(),
                                aqo.getCorrection())
                        );
                    }
                }
                Log.d(TAG, "checklistStandards size => " + checklistStandards.size());


                Map<String, List<ChecklistStandard>> subsectionStandardMap = new LinkedHashMap<>();
                for (ChecklistStandard checklistStandard : checklistStandards) {
                    List<ChecklistStandard> aList;
                    if (subsectionStandardMap.containsKey(checklistStandard.getmChecklistStandardSubSection())) {
                        aList = subsectionStandardMap.get(checklistStandard.getmChecklistStandardSubSection());
                    } else {
                        aList = new ArrayList<>();
                    }
                    Objects.requireNonNull(aList).add(checklistStandard);
                    subsectionStandardMap.put(checklistStandard.getmChecklistStandardSubSection(), aList);
                }

                checklistSubsections.clear();
                for (String subSection : subsectionStandardMap.keySet()) {
                    int completed = 0;
                    List<ChecklistStandard> sdList = subsectionStandardMap.get(subSection);
                    assert sdList != null;
                    for (ChecklistStandard sd : sdList) {
                        if (sd.getChecklistStandardQuestionAnswered()) {
                            completed = completed + 1;
                        }
                    }
                    int percent = (int) Math.round(((double) completed / sdList.size()) * 100);
                    checklistSubsections.add(new ChecklistSubsection(subSection, sdList.get(0).getmChecklistStandardSubSectionOrder(), subsectionStandardMap.get(subSection), percent));
                }

                int totalNotAnswered = 0;
                int totalNoComments = 0;
                for(int i=0;i<checklistSubsections.size();i++){
                    ChecklistSubsection checklistSubsection = checklistSubsections.get(i);
                    Log.d("onCreate", "section name: "+checklistSubsection.getSubsectionName());
                    for(int j=0;j<checklistSubsection.getChecklistStandards().size();j++){
                        ChecklistStandard checkListStandard = checklistSubsection.getChecklistStandards().get(j);
                        boolean isAnswered = checkListStandard.getChecklistStandardQuestionAnswered();
                        String comment = checkListStandard.getmChecklistStandardNonCompliance();
                        String ansValue = checkListStandard.getmChecklistStandardCompliance();
                        Log.d("onCreate", "is answered : "+isAnswered);
                        Log.d("onCreate", "comment : "+comment);
                        Log.d("onCreate", "ansValue: "+ansValue);
                        if(!isAnswered) totalNotAnswered++;
                        if(comment.trim().isEmpty() && !ansValue.equals("N/A")) totalNoComments++;
                    }
                }
                Log.d("onCreate", "totalNotAns: "+totalNotAnswered);
                Log.d("onCreate", "totalNoComments: "+totalNoComments);

                Log.d("value", "value="+selectedDropdownOption+" -> "+totalNotAnswered+" -> "+totalNoComments);
                statusList.add(
                        new ChecklistQStatus(selectedDropdownOption,totalNotAnswered,totalNoComments)
                );
            }
        });
        return statusList;
    }



    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        float imgSizeInKB = getImageSize(image);
        Log.d("onCreate", "imgSizeInKB: "+imgSizeInKB);
        if(imgSizeInKB<195){
            return image;
        }


        if(image == null){return  null;}
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private float getImageSize(Bitmap bitmapPhoto) {
        try {
            if (bitmapPhoto!=null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmapPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] imageInByte = stream.toByteArray();


                // Get length of file in bytes
                float imageSizeInBytes = imageInByte.length;
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                float imageSizeInKB = imageSizeInBytes / 1024; //KB
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                //float imageSizeInMB = imageSizeInKB / 1024;
                return imageSizeInKB;
            }else {
                return 0.0f;
            }
        }catch (Exception e){
            e.printStackTrace();
            return 0.0f;

        }

    }



    public void loadChecklistSubsections() {
        Log.d("onCreate", "loadChecklistSubsections: ");
        if (assessmentQuestionObjectList != null && assessmentQuestionObjectList.size() > 0) {
            if (selectedSection != null && !selectedSection.equals("")) {
                checklistStandards = new ArrayList<>();
                for (AssessmentQuestionObject aqo : assessmentQuestionObjectList) {
                    if (aqo.getSECTION().equals(selectedSection)) {
                        Log.d(TAG, "Subsection Order:  " + aqo.getSubsectionOrder() + " => " + aqo.getSubsectionOrder());
                        checklistStandards.add(new ChecklistStandard(
                                aqo.getObjectId(),
                                aqo.getSECTION(),
                                aqo.getSUBSECTION(),
                                aqo.getSTANDARD(),
                                aqo.getSUMMARY(),
                                aqo.getCOMPLIANT(),
                                aqo.getCommentsAction(),
                                aqo.getSectionOrder(),
                                aqo.getSubsectionOrder(),
                                aqo.getStandardOrder(),
                                aqo.getGuidanceNotes(),
                                aqo.getQuestionAnswered().toString(),
                                aqo.getImageUploadLocal(),
                                aqo.getEvidenceRequired(),
                                aqo.getOtherEvidence(),
                                aqo.getCorrection())
                        );
                    }
                }
                Log.d(TAG, "checklistStandards size => " + checklistStandards.size());


                Map<String, List<ChecklistStandard>> subsectionStandardMap = new HashMap<>();
                for (ChecklistStandard checklistStandard : checklistStandards) {
                    List<ChecklistStandard> aList;
                    if (subsectionStandardMap.containsKey(checklistStandard.getmChecklistStandardSubSection())) {
                        aList = subsectionStandardMap.get(checklistStandard.getmChecklistStandardSubSection());
                    } else {
                        aList = new ArrayList<>();
                    }
                    Objects.requireNonNull(aList).add(checklistStandard);
                    subsectionStandardMap.put(checklistStandard.getmChecklistStandardSubSection(), aList);
                }

                checklistSubsections.clear();
                for (String subSection : subsectionStandardMap.keySet()) {
                    int completed = 0;
                    List<ChecklistStandard> sdList = subsectionStandardMap.get(subSection);
                    assert sdList != null;
                    for (ChecklistStandard sd : sdList) {
                        if (sd.getChecklistStandardQuestionAnswered()) {
                            completed = completed + 1;
                        }
                    }
                    int percent = (int) Math.round(((double) completed / sdList.size()) * 100);
                    checklistSubsections.add(new ChecklistSubsection(subSection, sdList.get(0).getmChecklistStandardSubSectionOrder(), subsectionStandardMap.get(subSection), percent));
                }

                // Sort the subsections
                Comparator<ChecklistSubsection> compareBySubsectionOrder = new Comparator<ChecklistSubsection>() {
                    @Override
                    public int compare(ChecklistSubsection ss1, ChecklistSubsection ss2) {
                        return ss1.getmSubsectionOrder().compareTo(ss2.getmSubsectionOrder());

                    }
                };
                Collections.sort(checklistSubsections, compareBySubsectionOrder);


                Log.d("onCreate", "main section name: "+selectedSection);
                totalNotAns = new AtomicInteger(0);
                totalNoComments = new AtomicInteger(0);
                checklistSubsections.forEach((checklistSubsection)->{
                    Log.d("onCreate", "section name: "+checklistSubsection.getSubsectionName());
                    checklistSubsection.getChecklistStandards().forEach((checkListStandard)->{
                        boolean isAnswered = checkListStandard.getChecklistStandardQuestionAnswered();
                        String comment = checkListStandard.getmChecklistStandardNonCompliance();
                        Log.d("onCreate", "is answered : "+isAnswered);
                        Log.d("onCreate", "comment : "+comment);
                        if(!isAnswered) totalNotAns.getAndIncrement();
                        if(comment.trim().isEmpty()) totalNoComments.getAndIncrement();
                    });
                });
                Log.d("onCreate", "totalNotAns: "+totalNotAns.get());
                Log.d("onCreate", "totalNoComments: "+totalNoComments.get());

                // set the adapter
                OpenKeyboard openKeyboard = new OpenKeyboard() {
                    @Override
                    public void openKeyBoard(EditText editText) {
                        /* Not Used */
                    }
                };
                MarkNAPopup markNAPopup = new MarkNAPopup() {
                    @Override
                    public void showMarkNAPopup(List<ChecklistStandard> checklistStandards,int pos) {
                        showConfirmPopup(checklistStandards,pos);
                    }
                };
                RefreshSpinner refreshSpinner = new RefreshSpinner() {
                    @Override
                    public void refreshDropdown() {
                        Log.d("onCreate", "refreshDropdown: called");
                        // initialise loader callbacks

                        LoaderManager.LoaderCallbacks<List<AssessmentQuestionObject>> tmp = new LoaderManager.LoaderCallbacks<List<AssessmentQuestionObject>>() {
                            @Override
                            public Loader<List<AssessmentQuestionObject>> onCreateLoader(int id, Bundle args) {
                                return new AssessmentQuestionListLoader(getApplicationContext(), curAccount, objectId);
                            }

                            @Override
                            public void onLoadFinished(Loader<List<AssessmentQuestionObject>> loader, List<AssessmentQuestionObject> data) {
                                assessmentQuestionObjectList = data;
                                loadChecklistSectionOptions();
                                mChecklistSectionSpinnerAdapter.notifyDataSetChanged();
                                /*if (mChecklistSectionItems.size() > 0) {
                                        mChecklistSectionSpinnerAdapter = new ChecklistSectionSpinnerAdapter(ChecklistActivity.this, mChecklistSectionItems); // set the mChecklistSectionItems to the adapter
                                        spinnerSectionItemList.setAdapter(mChecklistSectionSpinnerAdapter); // set the adapter for the spinner
                                        onSectionSelection(spinnerSectionItemList); // Handles on item selection
                                    }*/
                            }

                            @Override
                            public void onLoaderReset(Loader<List<AssessmentQuestionObject>> loader) {
                            }
                        };

                        getLoaderManager().initLoader(ASSESSMENT_QUESTION_LOADER_ID, null, tmp).forceLoad();

                    }
                };

                List<AllBitmap> allBitmapList = new ArrayList<>();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<checklistSubsections.size();i++){
                            List<ChecklistStandard> standardList =  checklistSubsections.get(i).getChecklistStandards();
                            List<ChecklistStandardBitmap> tmpBitmapList  = new ArrayList<>();
                            for(int j=0;j<standardList.size();j++){
                                String imgString = standardList.get(j).getmChecklistStandardImageUploadString();
                                Log.d("onCreate", "imgString: "+imgString);
                                //string to bitmap
                                Bitmap tmpBitmap = null;
                                try {
                                    byte [] encodeByte= Base64.decode(imgString,Base64.DEFAULT);
                                    tmpBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                } catch(Exception e) {
                                    e.printStackTrace();
                                    tmpBitmap = null;
                                }

                                Log.d("onCreate", "tmpBitmap: "+tmpBitmap);
                                Bitmap compressedImage = getResizedBitmap(tmpBitmap, 500);//100
                                //Bitmap compressedImage = tmpBitmap;
                                //add tmp bitmap
                                tmpBitmapList.add(new ChecklistStandardBitmap(compressedImage));
                            }
                            allBitmapList.add(new AllBitmap(tmpBitmapList));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //ui
                                checklistSubsectionRecyclerAdapter = new ChecklistSubsectionRecyclerAdapter(
                                        allBitmapList,
                                        ChecklistActivity.this,
                                        checklistSubsections,
                                        refreshSpinner,
                                        openKeyboard,
                                        markNAPopup
                                );
                                checklistSubsectionRecycler.setAdapter(checklistSubsectionRecyclerAdapter);
                                Log.d("onCreate", "loadChecklistSubsections adapter: ");
                            }
                        });
                    }
                }).start();


            }

        }
    }

    private void showConfirmPopup(List<ChecklistStandard> checklistStandards,int pos) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        checklistSubsectionRecyclerAdapter.doMarkAllNA(
                                checklistStandards,
                                pos
                        );
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChecklistActivity.this);
        builder.setMessage("This will mark all questions in this sub-section as N/A and cannot be undone, are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public List<AssessmentQuestionObject> sortAssessmentChecklist(List<AssessmentQuestionObject> unsortedAssessmentQuestions) {
        // Sorting the Assessment Question Object List in Section Order Ascending
        ArrayList<AssessmentQuestionObject> sortedList = new ArrayList<>(unsortedAssessmentQuestions);
        Comparator<AssessmentQuestionObject> compareBySectionOrder = new Comparator<AssessmentQuestionObject>() {
            @Override
            public int compare(AssessmentQuestionObject aq1, AssessmentQuestionObject aq2) {
                //return aq1.getSectionOrder().compareTo(aq2.getSectionOrder());
                int value1 = aq1.getSectionOrder().compareTo(aq2.getSectionOrder());
                if (value1 == 0) {
                    int value2 = aq1.getSubsectionOrder().compareTo(aq2.getSubsectionOrder());
                    if (value2 == 0) {
                        return aq1.getStandardOrder().compareTo(aq2.getStandardOrder());
                    } else {
                        return value2;
                    }
                }
                return value1;
            }
        };
        Collections.sort(sortedList, compareBySectionOrder);

        return sortedList;
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
     * A simple receiver for checklist question load complete events.
     *
     * @author shahedmiah
     */
    private class ChecklistQuestionLoadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                if (AssessmentQuestionListLoader.LOAD_COMPLETE_INTENT_ACTION.equals(action)) {
                    refreshList();
                }
            }
        }
    }

    /**
     * A simple receiver for checklist section load complete events.
     *
     * @author shahedmiah
     */
    private class ChecklistSectionLoadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                if (AssessmentChecklistSectionLoader.LOAD_COMPLETE_INTENT_ACTION.equals(action)) {
                    refreshList();
                }
            }
        }
    }

    private class ChecklistSectionCommentDialog {
        private Activity activity;
        private AlertDialog dialog;
        private EditText checklistSectionCommentEditText;
        private AssessmentChecklistSectionObject openedChecklistSectionCommentObject;

        public ChecklistSectionCommentDialog(Activity myActivity, AssessmentChecklistSectionObject assessmentChecklistSectionObject) {
            this.activity = myActivity;
            this.openedChecklistSectionCommentObject = assessmentChecklistSectionObject;
        }

        public void startAlertDialog() {
            // Create AlertDialog
            AlertDialog.Builder builder = null;



            if(darkModeEnabled){
                builder = new AlertDialog.Builder(activity,R.style.AlertDialog);
            }else{
                builder = new AlertDialog.Builder(activity);
            }

            // Use layout inflater to get the layout from the activity
            LayoutInflater inflater = activity.getLayoutInflater();

            View view = null;

            if(darkModeEnabled){
                view = inflater.inflate(R.layout.section_comment_dialog_dark_mode, null);
            }else{
                view = inflater.inflate(R.layout.section_comment_dialog, null);
            }

            // Get the Section Comment Edit Text field from the layout
            checklistSectionCommentEditText = view.findViewById(R.id.sectionCommentEditText);
            // Set the default value from the open checklist section object

            if (commentBoxData.isEmpty()) {
                checklistSectionCommentEditText.setText(openedChecklistSectionCommentObject.getSectionComments());
            } else {
                checklistSectionCommentEditText.setText(commentBoxData);
            }
            checklistSectionCommentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // If focus is lost from the edit text, then hide the keyboard
                    if (!hasFocus) {
                        // Hide keyboard
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });

            // Set the view on the builder for the Alert Dialog
            builder.setView(view)
                    .setCancelable(false)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Clear focus
                            checklistSectionCommentEditText.clearFocus();
                        }
                    })
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            commentBoxData = checklistSectionCommentEditText.getText().toString().trim();
                            // Clear focus
                            checklistSectionCommentEditText.clearFocus();

                            // Save the checklist section comment
                            saveChecklistSection();
                        }
                    });

            dialog = builder.create();
            dialog.show();

            if(darkModeEnabled){
                Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(Color.parseColor("#00A9AB"));
                Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(Color.parseColor("#00A9AB"));
            }
        }

        /**
         * on Click action for saving checklist section record
         */
        public void saveChecklistSection() {

            final String checklistSectionComment = checklistSectionCommentEditText.getText().toString();
            final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
            JSONObject assessmentChecklistSection;
            try {

                assessmentChecklistSection = smartStore.retrieve(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP,
                        smartStore.lookupSoupEntryId(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP,
                                Constants.ID, openedChecklistSectionCommentObject.getObjectId())).getJSONObject(0);

                assessmentChecklistSection.put(AssessmentChecklistSectionObject.SECTION_COMMENTS, checklistSectionCommentEditText.getText());
                assessmentChecklistSection.put(SyncTarget.LOCAL, true);
                assessmentChecklistSection.put(SyncTarget.LOCALLY_UPDATED, true);
                assessmentChecklistSection.put(SyncTarget.LOCALLY_CREATED, false);
                assessmentChecklistSection.put(SyncTarget.LOCALLY_DELETED, false);

                Log.d(TAG, "Save Section Comments => " + assessmentChecklistSection);
                smartStore.upsert(AssessmentChecklistSectionLoader.ASSESSMENT_CHECKLIST_SECTION_SOUP, assessmentChecklistSection);

                dialog.dismiss();
                //refreshList();
            } catch (JSONException e) {
                Log.e(TAG, "JSONException occurred while parsing", e);
            }
        }

    }
}