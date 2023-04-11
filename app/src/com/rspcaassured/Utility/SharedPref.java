package com.rspcaassured.Utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences mySharedPref;
    public SharedPref(Context context){
        mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }

    // this method will save the nightMode State : True or False
    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("NightMode",state);
        editor.commit();
    }
    // This method will load the Night Mode State
    public Boolean loadNightModeState () {
        Boolean state = mySharedPref.getBoolean("NightMode", false);
        return state;
    }

    // this method will save the Background Services State : True or False
    public void setBackgroundServicesState(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("BackgroundServices",state);
        editor.commit();
    }
    // This method will load the Background Services State
    public Boolean loadBackgroundServicesState () {
        Boolean state = mySharedPref.getBoolean("BackgroundServices", true);
        return state;
    }




    public void setDashboardSeen(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("dashboard_seen",state);
        editor.apply();
    }

    public Boolean isDashboardSeen() {
        return mySharedPref.getBoolean("dashboard_seen", false);
    }
}
