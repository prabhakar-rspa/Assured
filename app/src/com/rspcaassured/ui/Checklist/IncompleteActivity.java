package com.rspcaassured.ui.Checklist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.rspcaassured.R;
import com.rspcaassured.Utility.SharedPref;

import java.util.ArrayList;

public class IncompleteActivity extends AppCompatActivity {
    RecyclerView statusListRv;
    TextView okTv,continueTv;
    String s = "";
    SharedPref sharedPref;

    boolean darkModeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        // setting the theme style
        if (sharedPref.loadNightModeState()) {
            darkModeEnabled = true;
            setTheme(R.style.darkTheme);
        } else {
            darkModeEnabled = false;
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomplete);
        s = getIntent().getStringExtra("sectionName");
        ArrayList<ChecklistQStatus> statusList =  (ArrayList<ChecklistQStatus>) getIntent().getSerializableExtra("test");

        okTv = findViewById(R.id.okTv);
        continueTv = findViewById(R.id.continueTv);
        statusListRv = findViewById(R.id.statusListRv);


        IncompleteListAdapter adapter = new IncompleteListAdapter(
                statusList,
                IncompleteActivity.this,
                new IncompleteQuestion() {
                    @Override
                    public void moveToSection(ChecklistQStatus checklistQStatus) {
                        String section = checklistQStatus.getSectionName().trim();
                        Log.d("onCreate", "moveToSection: "+section);
                        s = section;
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("sName",s);
                        returnIntent.putExtra("continue",false);
                        setResult(Activity.RESULT_OK,returnIntent);
                        finish();
                    }
                });


        statusListRv.setLayoutManager(new LinearLayoutManager(IncompleteActivity.this));
        statusListRv.setAdapter(adapter);


        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("sName",s);
                returnIntent.putExtra("continue",false);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        continueTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("sName",s);
                returnIntent.putExtra("continue",true);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }
}