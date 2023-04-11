package com.rspcaassured.ui.Checklist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.loaders.AssessmentQuestionListLoader;
import com.rspcaassured.objects.AssessmentQuestionObject;
import com.salesforce.androidsdk.accounts.UserAccount;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.salesforce.androidsdk.smartstore.store.SmartStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReportRecyclerAdapter extends RecyclerView.Adapter<ReportRecyclerAdapter.ViewHolder> {
    private static final String TAG = "ReportRecyclerAdapter";
    List<ChecklistStandard> mStandards;
    ChecklistReport mActivity;
    SmartStore smartStore;
    public ReportRecyclerAdapter(List<ChecklistStandard> standards, ChecklistReport activity) {
        this.mStandards = standards;
        this.mActivity = activity;
        UserAccount curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
    }

    @NonNull
    @Override
    public ReportRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.report_recycler_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ReportRecyclerAdapter.ViewHolder holder, int position) {
        ChecklistStandard checklistStandard = mStandards.get(position);
        holder.standardInput.setText(checklistStandard.getmChecklistStandardNumber());
        holder.noncomplainceInput.setText(checklistStandard.getmChecklistStandardNonCompliance());

        // setting image preview
        if(checklistStandard.getmChecklistStandardImageUpload() != null){
            holder.uploadImageView.setVisibility(View.VISIBLE);
            holder.uploadImageView.setImageBitmap(checklistStandard.getmChecklistStandardImageUpload());
        }else{
            holder.uploadImageView.setVisibility(View.GONE);
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mActivity.getApplicationContext(),
                R.array.evidence_picklist, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        holder.evidenceInput.setAdapter(adapter);

        // Set values for evidence, other evidence and correction
        if(checklistStandard.getmEvidence() != null && checklistStandard.getmEvidence() != ""){
            holder.evidenceInput.setSelection(adapter.getPosition(checklistStandard.getmEvidence()));
        }
        holder.otherEvidenceInput.setText(checklistStandard.getmOtherEvidence());
        holder.correctionInput.setText(checklistStandard.getmCorrection());
    }

    @Override
    public int getItemCount() {
        return mStandards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageButton cameraButton, noteButton;
        TextView standardInput, evidenceInputText, otherFieldLabel;
        EditText correctionInput,
                noncomplainceInput,
                otherEvidenceInput;
        ImageView uploadImageView, uploadNoteView;

        Button saveDoneButton;

        Spinner evidenceInput;


        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            cameraButton = itemView.findViewById(R.id.cameraButton);
            noteButton = itemView.findViewById(R.id.noteButton);
            standardInput = itemView.findViewById(R.id.standardInput);
            correctionInput = itemView.findViewById(R.id.correctionInput);
            noncomplainceInput = itemView.findViewById(R.id.noncomplianceInput);
            evidenceInput = itemView.findViewById(R.id.evidenceInput);
            uploadImageView = itemView.findViewById(R.id.uploadPhotoImageView);
            uploadNoteView = itemView.findViewById(R.id.uploadNoteImageView);
            otherEvidenceInput = itemView.findViewById(R.id.otherFieldInput);
            saveDoneButton = itemView.findViewById(R.id.saveDoneButton);
            evidenceInputText = itemView.findViewById(R.id.evidenceInputText);
            otherFieldLabel = itemView.findViewById(R.id.otherFieldLabel);



            evidenceInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String itemSelected = (String)parent.getItemAtPosition(position);
                    Log.d(TAG, "Spinner Item Selected: " + itemSelected);
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    sd.setmEvidence(itemSelected);

                    evidenceInputText.setText(itemSelected);

                    if(itemSelected.equals("Other")){
                        otherEvidenceInput.setVisibility(View.VISIBLE);
                        otherFieldLabel.setVisibility(View.VISIBLE);
                    }else{
                        otherEvidenceInput.setVisibility(View.GONE);
                        otherFieldLabel.setVisibility(View.GONE);
                    }

                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.takePicture(ReportRecyclerAdapter.this, getAdapterPosition());
                }
            });

            noncomplainceInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        // Display the comments action done button
                        saveDoneButton.setVisibility(View.VISIBLE);
                    }else{
                        // Hide the comments action done button
                        saveDoneButton.setVisibility(View.INVISIBLE);

                        // Reset the edit text info if focus is lost
                        ChecklistStandard sd = mStandards.get(getAdapterPosition());
                        noncomplainceInput.setText(sd.getmChecklistStandardNonCompliance());

                        // Hide keyboard
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });

            correctionInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        // Display the comments action done button
                        saveDoneButton.setVisibility(View.VISIBLE);
                    }else{
                        // Hide the comments action done button
                        saveDoneButton.setVisibility(View.INVISIBLE);

                        // Reset the edit text info if focus is lost
                        ChecklistStandard sd = mStandards.get(getAdapterPosition());
                        correctionInput.setText(sd.getmCorrection());

                        // Hide keyboard
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });


            otherEvidenceInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        // Display the comments action done button
                        saveDoneButton.setVisibility(View.VISIBLE);
                    }else{
                        // Hide the comments action done button
                        saveDoneButton.setVisibility(View.INVISIBLE);

                        // Reset the edit text info if focus is lost
                        ChecklistStandard sd = mStandards.get(getAdapterPosition());
                        otherEvidenceInput.setText(sd.getmOtherEvidence());

                        // Hide keyboard
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });

            saveDoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    sd.setmChecklistStandardNonCompliance(noncomplainceInput.getText().toString());
                    sd.setmCorrection(correctionInput.getText().toString());
                    sd.setmOtherEvidence(otherEvidenceInput.getText().toString());

                    // notify item has changed in the current adapter
                    notifyItemChanged(getAdapterPosition());

                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd);

                    saveDoneButton.setVisibility(View.GONE);
                    noncomplainceInput.clearFocus();
                    correctionInput.clearFocus();
                    otherEvidenceInput.clearFocus();

                }
            });

        }
    }

    public void saveChecklistStandard(ChecklistStandard checklistStandard){
        JSONObject assQuest;
        try {

            assQuest = smartStore.retrieve(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                            Constants.ID, checklistStandard.getmChecklistStandardId())).getJSONObject(0);

            assQuest.put(AssessmentQuestionObject.COMPLIANT, checklistStandard.getmChecklistStandardCompliance());
            assQuest.put(AssessmentQuestionObject.COMMENTS_ACTION, checklistStandard.getmChecklistStandardNonCompliance());
            assQuest.put(AssessmentQuestionObject.QUESTION_ANSWERED, checklistStandard.getChecklistStandardQuestionAnswered());
            if(checklistStandard.getmChecklistStandardImageUpload() != null){
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL, AppUtility.BitMapToString(checklistStandard.getmChecklistStandardImageUpload()));
            }
            assQuest.put(AssessmentQuestionObject.EVIDENCE_REQUIRED, checklistStandard.getmEvidence());
            assQuest.put(AssessmentQuestionObject.OTHER_EVIDENCE, checklistStandard.getmOtherEvidence());
            assQuest.put(AssessmentQuestionObject.CORRECTION, checklistStandard.getmCorrection());
            assQuest.put(SyncTarget.LOCAL, true);
            assQuest.put(SyncTarget.LOCALLY_UPDATED, true);
            assQuest.put(SyncTarget.LOCALLY_CREATED, false);
            assQuest.put(SyncTarget.LOCALLY_DELETED, false);

            smartStore.upsert(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP, assQuest);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException occurred while parsing", e);
        }
    }
}
