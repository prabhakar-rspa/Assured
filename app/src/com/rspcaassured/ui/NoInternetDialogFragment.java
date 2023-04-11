package com.rspcaassured.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import com.rspcaassured.R;
import com.salesforce.androidsdk.app.SalesforceSDKManager;



public class NoInternetDialogFragment extends DialogFragment {
    // Shared preferences
    //SharedPref sharedPref = null;

    /**
     * Default constructor.
     *
     */

    public NoInternetDialogFragment() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isDarkTheme = SalesforceSDKManager.getInstance().isDarkTheme();
        return new AlertDialog.Builder(getActivity(), isDarkTheme ?
                R.style.SalesforceSDK_Dialog_Dark : R.style.SalesforceSDK_Dialog)
                .setTitle(R.string.no_internet_title)
                /*.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                sharedPref = new SharedPref(getActivity());
                                sharedPref.setDashboardSeen(false);
                                SalesforceSDKManager.getInstance().logout(getActivity());
                            }
                        })*/
                .setNegativeButton(R.string.ok, null)
                .create();
    }
}
