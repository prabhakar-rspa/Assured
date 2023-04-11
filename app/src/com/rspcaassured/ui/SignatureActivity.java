package com.rspcaassured.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rspcaassured.R;

import java.io.ByteArrayOutputStream;

public class SignatureActivity extends AppCompatActivity {
    SignaturePad signaturePad;
    Button saveBtn,clearBtn;
    boolean signDone = false;
    String signType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        signType = getIntent().getStringExtra("sign_type");

        signaturePad = (SignaturePad) findViewById(R.id.signaturePad);
        saveBtn = findViewById(R.id.saveBtn);
        clearBtn = findViewById(R.id.clearBtn);

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {

            @Override
            public void onStartSigning() {
                //Event triggered when the pad is touched
            }

            @Override
            public void onSigned() {
                //Event triggered when the pad is signed
                Log.d("onCreate", "onSigned: called");
                signDone = true;
            }

            @Override
            public void onClear() {
                //Event triggered when the pad is cleared
                signDone = false;
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent returnIntent = new Intent();
                returnIntent.putExtra("SignBitmap",b);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();*/

                Bitmap bmp = signaturePad.getSignatureBitmap();
                if(bmp!=null && signDone){
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byte[] byteArray = stream.toByteArray();
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("sign",byteArray);
                    returnIntent.putExtra("sign_type",signType);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }else{
                    Toast.makeText(SignatureActivity.this, "Missing Signature !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
            }
        });
    }
}