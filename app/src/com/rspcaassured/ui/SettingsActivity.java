package com.rspcaassured.ui;

import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.rspcaassured.R;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.SharedPref;

public class SettingsActivity extends AppCompatActivity {
    public static NoInternetDialogFragment noInternetDialogFragment;
    // Initialise variable
    DrawerLayout drawerLayout;
    public static Switch darkSwitch;
    SharedPref sharedPref;

    private static final String TAG = "SettingsActivity";
    public static Switch backgroundServicesSwitch;



    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        // setting the theme style
        if(sharedPref.loadNightModeState() == true){
            setTheme(R.style.darkTheme);
        }else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        noInternetDialogFragment = new NoInternetDialogFragment();

        drawerLayout = findViewById(R.id.drawer_layout);

        //Load drawerlayout user information
        MainActivity.loadUserInformation(drawerLayout);

        // setting the dark switch button
        darkSwitch = (Switch) findViewById(R.id.darkSwitch);
        if(sharedPref.loadNightModeState() == true){
            darkSwitch.setChecked(true);
        }
        darkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sharedPref.setNightModeState(true);
                    restartApp();
                }
                else {
                    sharedPref.setNightModeState(false);
                    restartApp();
                }
            }
        });

        // background services switch
        backgroundServicesSwitch = (Switch) findViewById(R.id.backgroundServicesSwitch);
        if(sharedPref.loadBackgroundServicesState() == true){
            backgroundServicesSwitch.setChecked(true);
        }
        backgroundServicesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sharedPref.setBackgroundServicesState(true);
                }else{
                    sharedPref.setBackgroundServicesState(false);
                    Log.d(TAG, "Turning off background services");
                    cancelAllScheduledJobs();
                }
            }
        });


    }

    /**
     * Cancels all scheduled jobs
     */
    public void cancelAllScheduledJobs(){
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancelAll();
        Log.d(TAG, "All Jobs Cancelled");
    }

    public void restartApp (){
        Log.d("onCreate", "restartApp: called");
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * onlick action for ClickNavMenu
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
     * redirects activity to notifications
     */
    public void ClickNotifications(View view){
        // Redirect activity to settings
        MainActivity.redirectActivity(this, Notifications.class);
    }

    /**
     * Recreate activity
     */
    public void ClickSettings(View view){
        // Recreate activity
        recreate();
    }

    /**
     * Displays logout confirmation dialog
     * @param view
     */
    public void onClickLogout(View view){
        if(!InternetUtil.isInternetAvailable(SettingsActivity.this)){
            noInternetDialogFragment.show(getFragmentManager(), "No Internet");
            return;
        }
        MainActivity.logoutConfirmationDialog.show(getFragmentManager(), "LogoutDialog");
    }

    @Override
    protected void onPause(){
        super.onPause();
        // Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }

    @Override
    public void onBackPressed() {
        Intent homeScreen = new Intent(SettingsActivity.this, MainActivity.class);
        homeScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeScreen);
        ActivityCompat.finishAffinity(SettingsActivity.this);
    }
}