package com.rspcaassured.ui.Checklist;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.rspcaassured.R;

public class FullScreenImageActivity extends AppCompatActivity {
    ImageView fullScreenIV,cancelIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        fullScreenIV = findViewById(R.id.fullScreenIV);
        cancelIV = findViewById(R.id.cancelIV);
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        fullScreenIV.setImageBitmap(bmp);

        cancelIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}