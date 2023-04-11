package com.rspcaassured.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.rspcaassured.R;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.SharedPref;

public class Notifications extends AppCompatActivity {
    public static NoInternetDialogFragment noInternetDialogFragment;
    // Initialise variable
    DrawerLayout drawerLayout;

    SharedPref sharedPref;

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

        setContentView(R.layout.activity_notifications);

        noInternetDialogFragment = new NoInternetDialogFragment();


        // Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        //Load drawerlayout user information
        MainActivity.loadUserInformation(drawerLayout);

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
        MainActivity.redirectActivity(this,Profile.class);
    }

    /**
     * recreates activity
     */
    public void ClickNotifications(View view){
        // Recreate activity
        recreate();
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
     * @param view
     */
    public void onClickLogout(View view){
        if(!InternetUtil.isInternetAvailable(Notifications.this)){
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
}