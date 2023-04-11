package com.rspcaassured.ui.Checklist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.loaders.AssessmentQuestionListLoader;
import com.rspcaassured.objects.AssessmentQuestionObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONException;
import org.json.JSONObject;

public class AddNoteDialog{
    private Activity activity;
    private AlertDialog dialog;
    private EditText addNoteEditText;
    private  ChecklistStandard sd = null;
    SmartStore smartStore = null;


    public AddNoteDialog(
            Activity myActivity,
            SmartStore smartStore
    ){
        this.activity = myActivity;
        this.smartStore = smartStore;
    }

    public void startAlertDialog(ChecklistStandard sd){
        this.sd = sd;
        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Use layout inflater to get the layout from the activity
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_note_dialog,null);
        addNoteEditText = view.findViewById(R.id.addNoteEditText);
        String noteAddedEarlier = sd.getAddNote();
        if(noteAddedEarlier!=null && !noteAddedEarlier.isEmpty()){
            addNoteEditText.setText(noteAddedEarlier);
        }
        addNoteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If focus is lost from the edit text, then hide the keyboard
                if(!hasFocus){
                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        // Set the view on the builder for the Alert Dialog
        builder.setView(view)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear focus
                        addNoteEditText.clearFocus();
                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear focus
                        addNoteEditText.clearFocus();
                        saveAddNoteText();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    /**
     * on Click action for saving checklist section record
     */
    public void saveAddNoteText(){
        String inputText = addNoteEditText.getText().toString();
        Log.d("onCreate", "saveAddNoteText inputText: "+inputText);

        if(inputText!=null){
            //set data
            sd.setAddNote(inputText);
            // Save the Assessment Question to smart store
            saveChecklistStandard(sd);
            dialog.dismiss();
        }
    }

    public void saveChecklistStandard(ChecklistStandard checklistStandard){
        JSONObject assQuest;
        try {

            assQuest = smartStore.retrieve(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                            Constants.ID, checklistStandard.getmChecklistStandardId())).getJSONObject(0);

            assQuest.put(AssessmentQuestionObject.NOTES_UPLOAD_LOCAL, checklistStandard.getAddNote());
            assQuest.put(AssessmentQuestionObject.COMPLIANT, checklistStandard.getmChecklistStandardCompliance());
            assQuest.put(AssessmentQuestionObject.COMMENTS_ACTION, checklistStandard.getmChecklistStandardNonCompliance());
            assQuest.put(AssessmentQuestionObject.QUESTION_ANSWERED, checklistStandard.getChecklistStandardQuestionAnswered());
            if(checklistStandard.getmChecklistStandardImageUpload() != null){
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL, AppUtility.BitMapToString(checklistStandard.getmChecklistStandardImageUpload()));
            }
            assQuest.put(SyncTarget.LOCAL, true);
            assQuest.put(SyncTarget.LOCALLY_UPDATED, true);
            assQuest.put(SyncTarget.LOCALLY_CREATED, false);
            assQuest.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, assQuest);
        } catch (JSONException e) {
            Log.e("onCreate", "JSONException occurred while parsing", e);
        }
    }

}
