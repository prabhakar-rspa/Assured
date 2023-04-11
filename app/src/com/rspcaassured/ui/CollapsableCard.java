package com.rspcaassured.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.Utility.SharedPref;
import com.rspcaassured.ui.Checklist.ChecklistActivity;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class CollapsableCard extends SalesforceActivity {

    SharedPref sharedPref;
    private RestClient client;
    private String TAG = "CollapseCard";

    ImageView imageView;


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
        setContentView(R.layout.collapsable_card);


        imageView = findViewById(R.id.imageTestCollapse);


    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        if(AppUtility.isConnected(this)){
            Log.d("Network","Online");
            // Query Assessment Records from salesforce. Need to add check here to see if there is access to internet first
            try {
                sendRequest(); // to get single content version
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else {
            Log.d("Network","Offline");
        }
    }

    private void sendRequest() throws UnsupportedEncodingException {
        RestRequest restRequest = new RestRequest(RestRequest.RestMethod.GET, "/services/data/v52.0/sobjects/ContentVersion/0680D000000qfG0QAI/VersionData");
        Log.d("CollapseCard", "restRequest => " + restRequest);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                   public void run() {
                        try {
                            byte[] bytes = result.asBytes();
                            Log.d(TAG, "bytes data => " + bytes);

                            setImageCollapse(bytes);


                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CollapsableCard.this,
                                CollapsableCard.this.getString(R.string.sf__generic_error, exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void setImageCollapse(byte[] bytes){
        if(bytes != null){
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bmp);

            // create scaled image
            //imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, imageView.getWidth(), imageView.getHeight(), false));
        }

    }
}
