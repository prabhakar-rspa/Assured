package com.rspcaassured.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.rspcaassured.R;
import com.rspcaassured.Utility.InternetUtil;
import com.rspcaassured.Utility.SharedPref;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;

public class Profile extends AppCompatActivity {
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

        setContentView(R.layout.activity_profile);

        noInternetDialogFragment = new NoInternetDialogFragment();

        //Assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        //Load drawerlayout user information
        MainActivity.loadUserInformation(drawerLayout);

        // Load profile page user information
        loadProfileData();
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
     * recreates profile activity
     */
    public void ClickProfile(View view){
        // Recreate activity
        recreate();
    }

    /**
     * redirects activity to notifications
     */
    public void ClickNotifications(View view){
        // Redirect activity to notifications
        MainActivity.redirectActivity(this,Notifications.class);
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
        if(!InternetUtil.isInternetAvailable(Profile.this)){
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

    /**
     * Load user information
     */
    private void loadProfileData(){
        UserAccount user = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        TextView userFullNameProfile = (TextView) drawerLayout.findViewById(R.id.userFullNameProfile);
        ImageView userProfileImage = (ImageView) drawerLayout.findViewById(R.id.userProfileImage);
        TextView firstName = (TextView) drawerLayout.findViewById(R.id.firstName);
        TextView lastName = (TextView) drawerLayout.findViewById(R.id.lastName);
        TextView salesforceUsername = (TextView) drawerLayout.findViewById(R.id.salesforceUsername);
        TextView userEmail = (TextView) drawerLayout.findViewById(R.id.userEmail);
        if(userFullNameProfile != null){
            userFullNameProfile.setText(user.getFirstName() + " " + user.getLastName());
        }
        if(userProfileImage != null){
            if(user.getProfilePhoto() != null){
                userProfileImage.setImageBitmap(user.getProfilePhoto());
            }
        }
        if(firstName != null){
            firstName.setText(user.getFirstName());
        }
        if(lastName != null){
            lastName.setText(user.getLastName());
        }
        if(salesforceUsername != null){
            salesforceUsername.setText(user.getUsername());
        }
        if(userEmail != null){
            userEmail.setText(user.getEmail());
        }

    }

}