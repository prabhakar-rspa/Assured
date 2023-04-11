package com.rspcaassured.ui;
import static com.rspcaassured.Utility.InternetUtil.isInternetAvailable;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.rspcaassured.R;
import com.rspcaassured.Utility.SharedPref;

public class LauncherActivity extends AppCompatActivity {
    private Button retryBtn;

    // Shared preferences
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        super.onCreate(savedInstanceState);
        if(sharedPref.isDashboardSeen()){
            moveToMainActivity();
        }else{
            if (isInternetAvailable(LauncherActivity.this)) {
                moveToMainActivity();
            } else {
                setContentView(R.layout.activity_launcher);
                initViews();
                setOnClicks();
            }
        }

    }

    private void setOnClicks() {
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isInternetAvailable(LauncherActivity.this)){
                    moveToMainActivity();
                }else{
                    Toast.makeText(LauncherActivity.this,
                            "Please connect to Internet",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initViews() {
        retryBtn = findViewById(R.id.retryButton);
    }

    private void moveToMainActivity() {
        Intent intent = new Intent(
                LauncherActivity.this,
                MainActivity.class
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        LauncherActivity.this.finish();
    }
}