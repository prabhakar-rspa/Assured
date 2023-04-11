package com.rspcaassured.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.rspcaassured.Utility.SharedPref;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.rspcaassured.R;

/**
 * A simple dialog fragment to provide options at logout.
 */
public class LogoutDialogFragment extends DialogFragment {
    // Shared preferences
    SharedPref sharedPref = null;

    /**
     * Default constructor.
     *
     */

    public LogoutDialogFragment() {

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean isDarkTheme = SalesforceSDKManager.getInstance().isDarkTheme();
        return new AlertDialog.Builder(getActivity(), isDarkTheme ?
                R.style.SalesforceSDK_Dialog_Dark : R.style.SalesforceSDK_Dialog)
                .setTitle(R.string.logout_title)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                sharedPref = new SharedPref(getActivity());
                                sharedPref.setDashboardSeen(false);
                                SalesforceSDKManager.getInstance().logout(getActivity());
                            }
                        })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }
}