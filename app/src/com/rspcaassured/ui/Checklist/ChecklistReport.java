package com.rspcaassured.ui.Checklist;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rspcaassured.R;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.LoadingDialog;
import com.rspcaassured.loaders.AssessmentListLoader;
import com.rspcaassured.objects.ApplicationObject;
import com.rspcaassured.objects.AssessmentObject;
import com.rspcaassured.sync.Assessment.AssessmentSyncAdapter;
import com.rspcaassured.ui.Assessment.AssessmentOverviewActivity;
import com.rspcaassured.ui.MainActivity;
import com.rspcaassured.ui.NoInternetDialogFragment;
import com.rspcaassured.ui.Notifications;
import com.rspcaassured.ui.Profile;
import com.rspcaassured.ui.SettingsActivity;
import com.rspcaassured.Utility.SharedPref;
import com.rspcaassured.ui.SignatureActivity;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.smartstore.store.QuerySpec;
import com.salesforce.androidsdk.smartstore.store.SmartSqlHelper;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChecklistReport extends AppCompatActivity {
    public static NoInternetDialogFragment noInternetDialogFragment;
    private static final String ASSESSMENT_SYNC_CONTENT_AUTHORITY = "com.rspcaassured.sync.Assessment.AssessmentSyncAdapter";// assessment sync content authority
    private static final int LAUNCH_SIGN_PAGE = 88;
    private UserAccount curAccount;
    private AlertDialog dialog;
    // Initialise variable
    DrawerLayout drawerLayout;
    SharedPref sharedPref;

    RecyclerView recyclerView;
    ReportRecyclerAdapter reportRecyclerAdapter;

    private String applicationId;
    private String rearingSystem;
    private String animal;
    private ArrayList<ChecklistStandard> checklistStandards;
    private String assessmentId;
    private String assessmentName;
    private SmartStore smartStore;
    private EditText memberSignEditText;
    private TextView memberDateInput,assessorDateInput;

    private TextView assessorSignName,memberSignName;

    private String inputMemberSignName="";

    private UserAccount user = null;

    // Loading dialog
    LoadingDialog loadingDialog;

    ReportRecyclerAdapter tempReportRecyclerAdapter;
    int tempReportRecyclerAdapterItemPosition = 0;

    private String presentDate = "";

    private Button memberSignButton,assessorSignButton;
    private String assessorSignStr ="";
    private String memberSignStr = "";

    private boolean darkModeEnabled = false;

    TextView tvA,tvC,tvD,tvB,assessorSign,memberSign,asessorDate,memberDate;

    CheckBox checkBoxDarkMode,checkBoxNormal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        sharedPref = new SharedPref(this);
        getPresentDate();

        // setting the theme style
        if (sharedPref.loadNightModeState() == true) {
            darkModeEnabled = true;
            setTheme(R.style.darkTheme);
        } else {
            darkModeEnabled = false;
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist_report);

        tvA = findViewById(R.id.tvA);
        tvC = findViewById(R.id.tvC);
        tvD = findViewById(R.id.tvD);
        tvB = findViewById(R.id.tvB);
        checkBoxNormal = findViewById(R.id.checkBoxNormal);
        checkBoxDarkMode = findViewById(R.id.checkBoxDarkMode);
        assessorDateInput = findViewById(R.id.assessorDateInput);
        memberDateInput = findViewById(R.id.memberDateInput);
        assessorSign = findViewById(R.id.assessorSign);
        memberSign = findViewById(R.id.memberSign);
        asessorDate = findViewById(R.id.asessorDate);
        memberDate = findViewById(R.id.memberDate);

        if(darkModeEnabled){
            tvA.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            tvC.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            tvD.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            tvB.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            assessorDateInput.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            memberDateInput.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            assessorSign.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            memberSign.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            asessorDate.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));
            memberDate.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));

          //  checkBox.setButtonTintMode(ColorStateList.valueOf(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode)));
           // checkBox.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode)));
            tvA.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));

            checkBoxDarkMode.setVisibility(View.VISIBLE);
            checkBoxNormal.setVisibility(View.GONE);
            checkBoxDarkMode.setTextColor(ContextCompat.getColor(ChecklistReport.this, R.color.textColorDarkMode));

        }else{
            checkBoxDarkMode.setVisibility(View.GONE);
            checkBoxNormal.setVisibility(View.VISIBLE);
        }

        noInternetDialogFragment = new NoInternetDialogFragment();

        // Create loading dialog
        loadingDialog = new LoadingDialog(this);
        // Start loading dialog
        loadingDialog.startAlertDialog();

        // Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);
        memberDateInput = findViewById(R.id.memberDateInput);
        assessorDateInput = findViewById(R.id.assessorDateInput);
        assessorSignName = findViewById(R.id.assessorSignName) ;
        memberSignName = findViewById(R.id.memberSignName);

        memberSignButton = findViewById(R.id.memberSignButton);
        assessorSignButton = findViewById(R.id.assessorSignButton);

        //Load drawerlayout user information
        MainActivity.loadUserInformation(drawerLayout);

        // Launching assessment with object id key passed from main activity
        final Intent launchIntent = getIntent();
        if (launchIntent != null) {
            checklistStandards = launchIntent.getParcelableArrayListExtra("nonCompliances");
            applicationId = launchIntent.getStringExtra("applicationId");
            rearingSystem = launchIntent.getStringExtra("rearingSystem");
            animal = launchIntent.getStringExtra("animal");
            assessmentId = launchIntent.getStringExtra("assessmentId");
            assessmentName = launchIntent.getStringExtra("assessmentName");
            Log.d("ChecklistReport", "checklistStandards => " + checklistStandards);
        }

        MobileSyncSDKManager sdkManager = MobileSyncSDKManager.getInstance();
        smartStore = sdkManager.getSmartStore(sdkManager.getUserAccountManager().getCurrentUser());
        loadSiteInformation();

        recyclerView = findViewById(R.id.reportRecylcer);
        if(checklistStandards==null || !checklistStandards.isEmpty()){
            findViewById(R.id.noDataTv).setVisibility(View.GONE);
        }
        reportRecyclerAdapter = new ReportRecyclerAdapter(checklistStandards, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(reportRecyclerAdapter);

        // Dismiss dialog after all information is loaded
        loadingDialog.dismissDialog();

        // initialise tem variable
        tempReportRecyclerAdapter = new ReportRecyclerAdapter(null, this);

        initUserAccountObj();
        populatePresentDate();
        setOnClicks();

        //getSignDetailsFromSalesforce();
    }

    private void setOnClicks() {
        memberSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showMemberSignNameDialog();
                startSignPage("member_sign");
            }
        });

        assessorSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //populateAssessorSign();
                startSignPage("assessor_sign");
            }
        });
    }

    private void startSignPage(String s){
        //startActivity(new Intent(ChecklistReport.this, SignatureActivity.class));
        Intent i = new Intent(this, SignatureActivity.class);
        i.putExtra("sign_type",s);
        startActivityForResult(i, LAUNCH_SIGN_PAGE);
    }

    private void initUserAccountObj(){
        user = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
    }
    private void populateAssessorSign() {
        String fullName = user.getFirstName() + " " + user.getLastName();
        assessorSignName.setText(fullName);
    }

    private void setMemberSignName(String name){
        if(!name.trim().isEmpty()){
            memberSignName.setText(name);
        }
    }

    private void populatePresentDate() {
        memberDateInput.setText(presentDate);
        assessorDateInput.setText(presentDate);
    }

    public void showMemberSignNameDialog(){
        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ChecklistReport.this);

        // Use layout inflater to get the layout from the activity
        LayoutInflater inflater = ChecklistReport.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.member_sign_name_dialog,null);

        // Get the Section Comment Edit Text field from the layout
        memberSignEditText = view.findViewById(R.id.memberSignEditText);
        // Set the default value from the open checklist section object
        memberSignEditText.setText(inputMemberSignName);
        memberSignEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If focus is lost from the edit text, then hide the keyboard
                if(!hasFocus){
                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager)ChecklistReport.this.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                        memberSignEditText.clearFocus();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear focus
                        memberSignEditText.clearFocus();
                        inputMemberSignName = memberSignEditText.getText().toString();
                        setMemberSignName(inputMemberSignName);
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    private void getPresentDate() {
        presentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    public void takePicture(ReportRecyclerAdapter tempReportRecyclerAdapter, int itemPosition) {
        // assign adaptor
        this.tempReportRecyclerAdapter = tempReportRecyclerAdapter;
        this.tempReportRecyclerAdapterItemPosition = itemPosition;
        // Open Camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 110);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_SIGN_PAGE) {
            if(resultCode == Activity.RESULT_OK){
                byte[] byteArray = data.getByteArrayExtra("sign");
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                Log.d("onCreate", "onActivityResult: sign bitmap : "+bmp);
                String type = data.getStringExtra("sign_type");
                if(type.equals("member_sign")){
                    saveMemberSign(bmp);
                }else if(type.equals("assessor_sign")){
                    saveAssessorSign(bmp);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // Write your code if there's no result
                Log.d("onCreate", "onActivityResult: cancelled");
            }
        }
        else if (requestCode == 110) {
            // get capture image
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");

            tempReportRecyclerAdapter.mStandards.get(tempReportRecyclerAdapterItemPosition).setmChecklistStandardImageUpload(captureImage);
            tempReportRecyclerAdapter.saveChecklistStandard(tempReportRecyclerAdapter.mStandards.get(tempReportRecyclerAdapterItemPosition));
            tempReportRecyclerAdapter.notifyItemChanged(tempReportRecyclerAdapterItemPosition);


        }else{
            Log.d("onCreate", "onActivityResult: cancelled");
        }
    }


    private void getSignDetailsFromSalesforce(){
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        JSONObject assessment;
        try {
            assessment = smartStore.retrieve(AssessmentListLoader.ASSESSMENT_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentListLoader.ASSESSMENT_SOUP,
                            Constants.ID, assessmentId)).getJSONObject(0);

            memberSignStr = String.valueOf(assessment.get(AssessmentObject.MEMBER_SIGNATURE_STRING));
            assessorSignStr = String.valueOf(assessment.get(AssessmentObject.ASSESSOR_SIGNATURE_STRING));

            if(assessorSignStr!=null && !assessorSignStr.isEmpty()){
                assessorSignName.setText("Signed");
            }

            if(memberSignStr!=null && !memberSignStr.isEmpty()){
                memberSignName.setText("Signed");
            }

        } catch (JSONException e) {
            Log.e("test", "JSONException occurred while parsing", e);
        }
    }


    private void saveInSalesforceDB(){
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        JSONObject assessment;
        try {
            assessment = smartStore.retrieve(AssessmentListLoader.ASSESSMENT_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentListLoader.ASSESSMENT_SOUP,
                            Constants.ID, assessmentId)).getJSONObject(0);
            assessment.put(AssessmentObject.STATUS, "Completed");
            assessment.put(SyncTarget.LOCAL, true);
            assessment.put(SyncTarget.LOCALLY_UPDATED, true);
            assessment.put(SyncTarget.LOCALLY_CREATED, false);
            assessment.put(SyncTarget.LOCALLY_DELETED, false);

            Log.d("onCreate", "saveInSalesforceDB: memberSignStr: "+memberSignStr);
            Log.d("onCreate", "saveInSalesforceDB: assessorSignStr: "+assessorSignStr);
            assessment.put(AssessmentObject.MEMBER_SIGNATURE_STRING, memberSignStr);//ist
            assessment.put(AssessmentObject.ASSESSOR_SIGNATURE_STRING, assessorSignStr);//2nd

            smartStore.upsert(AssessmentListLoader.ASSESSMENT_SOUP, assessment);
        } catch (JSONException e) {
            Log.e("test", "JSONException occurred while parsing", e);
        }
        requestSync(false);
    }
    private void saveAssessorSign(Bitmap bmp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG,0, baos);
                byte [] b=baos.toByteArray();
                assessorSignStr = Base64.encodeToString(b, Base64.DEFAULT);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveInSalesforceDB();
                        assessorSignName.setText("Signed");
                    }
                });
            }
        }).start();
    }

    private void saveMemberSign(Bitmap bmp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG,0, baos);
                byte [] b=baos.toByteArray();
                memberSignStr = Base64.encodeToString(b, Base64.DEFAULT);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveInSalesforceDB();
                        memberSignName.setText("Signed");
                    }
                });
            }
        }).start();
    }

    private void loadSiteInformation() {
        final QuerySpec querySpec = QuerySpec.buildExactQuerySpec("applications", "Id", applicationId, ApplicationObject.NAME, QuerySpec.Order.ascending, 1);
        JSONArray results;
        List<ApplicationObject> applications = new ArrayList<>();
        try {
            results = smartStore.query(querySpec, 0);
            for (int i = 0; i < results.length(); i++) {
                applications.add(new ApplicationObject(results.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.e("ChecklistReport", "JSONException occurred while parsing", e);
        } catch (SmartSqlHelper.SmartSqlException e) {
            Log.e("ChecklistReport", "SmartSqlException occurred while fetching data", e);
        }

        setText((TextView) findViewById(R.id.membershipNumber), applications.get(0).getMembershipNumber());
        setText((TextView) findViewById(R.id.siteName), applications.get(0).getSiteName());
        setText((TextView) findViewById(R.id.Species), animal);
        //setText((TextView) findViewById(R.id.productionSystem), rearingSystem);
        String appType = applications.get(0).getApplicationType();
        if(appType==null){appType = "";}
        setText((TextView) findViewById(R.id.productionSystem), appType);
    }

    private void setText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }


    /**
     * onlick action for ClickNavMenu
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
     * recreates activity
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
     * @param view
     */
    public void onClickLogout(View view) {
        if(!InternetUtil.isInternetAvailable(ChecklistReport.this)){
            noInternetDialogFragment.show(getFragmentManager(), "No Internet");
            return;
        }
        MainActivity.logoutConfirmationDialog.show(getFragmentManager(), "LogoutDialog");
    }

    /*public void onClickComplete(View view) {
        Intent intent = new Intent(this, AssessmentOverviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("object_id", applicationId);
        startActivity(intent);
    }*/

    private void requestSync(boolean syncDownOnly) {
        Account account = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentAccount();
        Bundle assessmentExtras = new Bundle();
        assessmentExtras.putBoolean(AssessmentSyncAdapter.SYNC_DOWN_ONLY, syncDownOnly);
        ContentResolver.requestSync(account, ASSESSMENT_SYNC_CONTENT_AUTHORITY, assessmentExtras);
    }

    public void onClickComplete(View view) {
        if(assessorSignStr==null || assessorSignStr.isEmpty()){
            Toast.makeText(this, "Assessor Sign is missing !", Toast.LENGTH_SHORT).show();
            return;
        }
        if(memberSignStr==null || memberSignStr.isEmpty()){
            Toast.makeText(this, "Member Sign is missing !", Toast.LENGTH_SHORT).show();
            return;
        }
        // Update Assessment object soup with the progress
        final SmartStore smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        JSONObject assessment;
        try {
            assessment = smartStore.retrieve(AssessmentListLoader.ASSESSMENT_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentListLoader.ASSESSMENT_SOUP,
                            Constants.ID, assessmentId)).getJSONObject(0);
            assessment.put(AssessmentObject.STATUS, "Completed");
            assessment.put(SyncTarget.LOCAL, true);
            assessment.put(SyncTarget.LOCALLY_UPDATED, true);
            assessment.put(SyncTarget.LOCALLY_CREATED, false);
            assessment.put(SyncTarget.LOCALLY_DELETED, false);

            //assessment.put(AssessmentObject.MEMBER_SIGNATURE_STRING, signaturevalue);//ist
            //assessment.put(AssessmentObject.ASSESSOR_SIGNATURE_STRING, signaturevalue);//2nd

            Log.d("onCreate", "saveInSalesforceDB: memberSignStr: "+memberSignStr);
            Log.d("onCreate", "saveInSalesforceDB: assessorSignStr: "+assessorSignStr);
            assessment.put(AssessmentObject.MEMBER_SIGNATURE_STRING, memberSignStr);//ist
            assessment.put(AssessmentObject.ASSESSOR_SIGNATURE_STRING, assessorSignStr);//2nd

            smartStore.upsert(AssessmentListLoader.ASSESSMENT_SOUP, assessment);
        } catch (JSONException e) {
            Log.e("test", "JSONException occurred while parsing", e);
        }
        requestSync(false);
        Intent intent = new Intent(this, AssessmentOverviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("object_id", applicationId);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }



    @Override
    public void onBackPressed() {
        /*if(!assessorSignStr.isEmpty() || !memberSignStr.isEmpty()){
            Toast.makeText(this, "Cannot go back once signature is done", Toast.LENGTH_SHORT).show();
            return;
        }*/
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);

        AlertDialog.Builder builder = null;

        if(darkModeEnabled){
            builder = new AlertDialog.Builder(ChecklistReport.this,R.style.AlertDialog);
        }else{
            builder = new AlertDialog.Builder(ChecklistReport.this);
        }


        builder.setCancelable(true);
        builder.setTitle("Return without saving");
        builder.setMessage("Are you sure you want to go back without saving?");
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    final Intent intent = new Intent(this, ChecklistActivity.class);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra("object_id", assessmentId);
                    intent.putExtra("object_name", assessmentName);
                    intent.putExtra("applicationId", applicationId);
                    intent.putExtra("rearingSystem", rearingSystem);
                    intent.putExtra("animal", animal);
                    startActivity(intent);
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


        if(darkModeEnabled){
            Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(Color.parseColor("#00A9AB"));
            Button pbutton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(Color.parseColor("#00A9AB"));
        }
    }


}