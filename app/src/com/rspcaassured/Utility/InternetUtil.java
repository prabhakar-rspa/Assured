package com.rspcaassured.Utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetUtil {
    public static boolean isInternetAvailable(Context context){
        boolean internetAvailable = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    // connected to wifi
                    internetAvailable = true;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    internetAvailable = true;
                    break;
                default:
                    internetAvailable = false;
                    break;
            }
        } else {
            // not connected to the internet
            internetAvailable = false;
        }

        return internetAvailable;
    }
}
