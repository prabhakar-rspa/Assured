package com.rspcaassured.ui.Checklist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
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
import com.tooltip.Tooltip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ChecklistStandardRecyclerAdapter extends RecyclerView.Adapter<ChecklistStandardRecyclerAdapter.ViewHolder> {
    private static final String TAG = "ChecklistStandardRecyclerAdapter";
    List<ChecklistStandard> mStandards;
    ChecklistActivity mActivity;
    ChecklistSubsectionRecyclerAdapter subsectionRecyclerAdapter;
    int subsectionPosition;
    ProgressBar subsectionProgressBar;
    TextView subSectionProgressPercentage;
    SmartStore smartStore;
    AddNoteDialog addNoteDialog;
    private String originalSdText = null;
    List<ChecklistStandardBitmap> bitmapList;
    int requestFocusPos = -1;
    private OpenKeyboard openKeyboard;

    public ChecklistStandardRecyclerAdapter(
            List<ChecklistStandardBitmap> bitmapList,
            List<ChecklistStandard> standards,
            ChecklistActivity activity,
            ChecklistSubsectionRecyclerAdapter subsectionRecyclerAdapter,
            int subsectionPosition,
            ProgressBar subSectionProgressBar,
            TextView subSectionProgressPercentage,
            OpenKeyboard openKeyboard
    ) {
        this.requestFocusPos = -1;
        this.bitmapList = bitmapList;
        this.mStandards = standards;
        this.mActivity = activity;
        this.subsectionRecyclerAdapter = subsectionRecyclerAdapter;
        this.subsectionPosition = subsectionPosition;
        this.subsectionProgressBar = subSectionProgressBar;
        this.subSectionProgressPercentage = subSectionProgressPercentage;


        UserAccount curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
        this.addNoteDialog = new AddNoteDialog(activity,smartStore);
        this.openKeyboard = openKeyboard;
    }

    @NonNull
    @Override
    public ChecklistStandardRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.checklist_standard_recycler, parent, false);
        return new ChecklistStandardRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistStandardRecyclerAdapter.ViewHolder holder, int position) {
        if(mActivity.darkModeEnabled){
            holder.guidanceNotesButton.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.info_dark_mode));
            //set et background
            holder.checklistStandardNonCompliance.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.custom_input_dark_mode));
            //set et text color
            holder.checklistStandardNonCompliance.setTextColor(mActivity.getResources().getColor(R.color.textColorDarkMode));
            // and hint color
            holder.checklistStandardNonCompliance.setHintTextColor(mActivity.getResources().getColor(R.color.textColorDarkModeHint));

            holder.complianceYes.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.radio_checked_dark_mode));;
            holder.complianceNo.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.radio_checked_dark_mode));;
            holder.complianceNA.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.radio_checked_dark_mode));;

        }
        ChecklistStandard checklistStandard = mStandards.get(position);
        ChecklistStandardBitmap checklistStandardBitmap = bitmapList.get(position);

        if(requestFocusPos == position){
            holder.checklistStandardNonCompliance.requestFocus();
            openKeyBoardForcefully();
            requestFocusPos = -1;
        }else{
            holder.checklistStandardNonCompliance.clearFocus();
            requestFocusPos = -1;
        }

        //holder.checklistStandardNonCompliance.clearFocus();

        holder.checklistStandardNumber.setText(checklistStandard.getmChecklistStandardNumber());
        holder.checklistStandardSummary.setText(checklistStandard.getmChecklistStandardSummary());

        String complianceValue = checklistStandard.getmChecklistStandardCompliance();

        String isAnswered = checklistStandard.mChecklistStandardQuestionAnswered;
        boolean isAns = checklistStandard.getChecklistStandardQuestionAnswered();
        Log.d("onCreate", "isAns : "+isAns);
        Log.d("onCreate", "onBindViewHolder checklistStandard.mChecklistStandardQuestionAnswered: "+isAnswered);

        if(isAnswered.equals("true")){
            switch(complianceValue) {
                case "Yes":
                    holder.complianceYes.setChecked(true);
                    break;
                case "No":
                    holder.complianceNo.setChecked(true);
                    break;
                case "N/A":
                    holder.complianceNA.setChecked(true);
                    break;
                default:
            }
        }

        holder.checklistStandardNonCompliance.setText(checklistStandard.getmChecklistStandardNonCompliance());
        holder.guidanceNotesButton.setContentDescription(checklistStandard.getChecklistStandardGuidance());
        /*if(checklistStandard.getmChecklistStandardImageUpload() != null){
            holder.imgContainer.setVisibility(View.VISIBLE);
            holder.uploadImageView.setImageBitmap(checklistStandard.getmChecklistStandardImageUpload());
        }else{
            holder.imgContainer.setVisibility(View.INVISIBLE);
        }*/

        if(checklistStandardBitmap.getBitmap()!=null){
            Log.d("onCreate", "image obtained : "+checklistStandardBitmap.getBitmap());
            holder.imgContainer.setVisibility(View.VISIBLE);
            holder.uploadImageView.setImageBitmap(checklistStandardBitmap.getBitmap());
        }else{
            holder.imgContainer.setVisibility(View.INVISIBLE);
        }

        if(mActivity.readOnly){
            Log.d("onCreate", "onBindViewHolder: clickable false");
            holder.checklistRow.setClickable(false);
            holder.complianceNA.setClickable(false);
            holder.complianceNo.setClickable(false);
            holder.complianceYes.setClickable(false);
            holder.deleteImgButton.setClickable(false);
            holder.uploadImageButton.setClickable(false);
            holder.checklistStandardNonCompliance.setFocusable(false);
            //holder.checklistStandardNonCompliance.setEnabled(false);
        }

    }

    private void openKeyBoardForcefully() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public int getItemCount() {
        return mStandards.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView checklistStandardNumber;
        TextView checklistStandardSummary;
        EditText checklistStandardNonCompliance;
        RadioButton complianceYes;
        RadioButton complianceNo;
        RadioButton complianceNA;
        Button guidanceNotesButton;
        Button commentsActionDoneButton;
        ImageButton uploadImageButton,addNote;
        ImageView uploadImageView,deleteImgButton;
        ConstraintLayout imgContainer;
        ConstraintLayout checklistRow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            checklistStandardNumber = itemView.findViewById(R.id.standardNumber);
            checklistStandardSummary = itemView.findViewById(R.id.standardSummary);
            checklistStandardNonCompliance = itemView.findViewById(R.id.standardNonComplianceText);
            complianceYes = itemView.findViewById(R.id.complianceYes);
            complianceNo  = itemView.findViewById(R.id.complianceNo);
            complianceNA = itemView.findViewById(R.id.complianceNA);
            guidanceNotesButton = itemView.findViewById(R.id.guidanceNotesButton);
            commentsActionDoneButton = itemView.findViewById(R.id.commentsActionDoneButton);
            uploadImageButton = itemView.findViewById(R.id.addphoto);
            uploadImageView = itemView.findViewById(R.id.uploadPhotoImageView);
            addNote = itemView.findViewById(R.id.addNote);
            deleteImgButton = itemView.findViewById(R.id.deleteImgButton);
            imgContainer = itemView.findViewById(R.id.imgContainer);
            checklistRow = itemView.findViewById(R.id.checklistRow);

            checklistStandardNonCompliance.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    String input = s.toString().trim();
                    Log.d("onCreate", "afterTextChanged: "+input);
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    //saves data on ui only
                    sd.setmChecklistStandardNonCompliance(input);
                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd,getAdapterPosition());
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
            });


            uploadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Convert to byte array
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    Bitmap bmp = mStandards.get(getAdapterPosition()).getmChecklistStandardImageUpload();
                    bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byte[] byteArray = stream.toByteArray();
                    Intent in1 = new Intent(mActivity, FullScreenImageActivity.class);
                    in1.putExtra("image",byteArray);
                    mActivity.startActivity(in1);
                }
            });

            addNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNoteDialog.startAlertDialog(mStandards.get(getAdapterPosition()));
                }
            });

            deleteImgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("onCreate", "delete clicked: ");
                    mActivity.removePicture(ChecklistStandardRecyclerAdapter.this, getAdapterPosition());
                }
            });

            // Compliance "No" button on click listener updates the compliance field on the checklist standard object
            complianceNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    sd.setmChecklistStandardCompliance("No");

                    // Mark checklist question as answered
                    sd.setChecklistStandardQuestionAnswered(true);
                    requestFocusPos = getAdapterPosition();
                    // notify item has changed in the current adapter
                    notifyItemChanged(getAdapterPosition());

                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd,getAdapterPosition());

                    // refresh subsection data
                    subsectionRecyclerAdapter.refreshSubsection(subsectionPosition, mStandards, subsectionProgressBar, subSectionProgressPercentage);
                }
            });

            // Compliance "Yes" button on click listener updates the compliance field on the checklist standard object
            complianceYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    sd.setmChecklistStandardCompliance("Yes");

                    // Mark checklist question as answered
                    sd.setChecklistStandardQuestionAnswered(true);
                    requestFocusPos = getAdapterPosition();
                    // notify item has changed in the current adapter
                    notifyItemChanged(getAdapterPosition());

                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd,getAdapterPosition());

                    // refresh subsection data
                    subsectionRecyclerAdapter.refreshSubsection(subsectionPosition, mStandards, subsectionProgressBar, subSectionProgressPercentage);
                }
            });

            // Compliance "N/A" button on click listener updates the compliance field on the checklist standard object
            complianceNA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistStandard sd = mStandards.get(getAdapterPosition());
                    sd.setmChecklistStandardCompliance("N/A");

                    // Mark checklist question as answered
                    sd.setChecklistStandardQuestionAnswered(true);
                    requestFocusPos = getAdapterPosition();
                    // notify item has changed in the current adapter
                    notifyItemChanged(getAdapterPosition());

                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd,getAdapterPosition());

                    // refresh subsection data
                    subsectionRecyclerAdapter.refreshSubsection(subsectionPosition, mStandards, subsectionProgressBar, subSectionProgressPercentage);
                }
            });


           /* checklistStandardNonCompliance.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });*/


            // Enables comments action done button when non compliance edit text is in focus.
            checklistStandardNonCompliance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        originalSdText = mStandards.get(getAdapterPosition()).getmChecklistStandardNonCompliance();
                        // Display the comments action done button
                        commentsActionDoneButton.setVisibility(View.VISIBLE);
                        //commentsActionDoneButton.setVisibility(View.INVISIBLE);
                    }else{
                        Log.d("onCreate", "onFocusChange: focus lost");
                        // Hide the comments action done button
                        commentsActionDoneButton.setVisibility(View.INVISIBLE);

                        // Reset the edit text info if focus is lost
                        ChecklistStandard sd = mStandards.get(getAdapterPosition());
                        checklistStandardNonCompliance.setText(sd.getmChecklistStandardNonCompliance());

                        // Hide keyboard
                       /* InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);*/
                    }
                }
            });


            // Update the ChecklistStandard Non Compliance Text and hide the keyboard and hide the 'Done' button
            commentsActionDoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ChecklistStandard sd = mStandards.get(getAdapterPosition());

                    Log.d("onCreate", "originalText: "+originalSdText);
                    checklistStandardNonCompliance.setText(originalSdText);

                 /*   sd.setmChecklistStandardNonCompliance(checklistStandardNonCompliance.getText().toString());
                    // Mark checklist question as answered
                    sd.setChecklistStandardQuestionAnswered(true);
                    // notify item has changed in the current adapter
                    notifyItemChanged(getAdapterPosition());
                    // Save the Assessment Question to smart store
                    saveChecklistStandard(sd);
                    // refresh subsection data
                    subsectionRecyclerAdapter.refreshSubsection(subsectionPosition, mStandards, subsectionProgressBar, subSectionProgressPercentage);
                    commentsActionDoneButton.setVisibility(View.INVISIBLE);
                    checklistStandardNonCompliance.clearFocus();*/
                }
            });

            // Guidance Notes tooltip button
            guidanceNotesButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Button btn = (Button)v;
                    String helpText = (String) btn.getContentDescription();
                    if(helpText == null || helpText == ""){
                        helpText = "No guidance note available.";
                    }
                    Tooltip tooltip = new Tooltip.Builder(btn)
                            .setText(helpText)
                            .setTextColor(Color.WHITE)
                            .setGravity(Gravity.TOP)
                            .setCornerRadius(8f)
                            .setDismissOnClick(true)
                            .setBackgroundColor(R.attr.headerTextColor)
                            .setCancelable(true)
                            .show();
                }
            });



            // upload image button action
            uploadImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mActivity.takePicture(ChecklistStandardRecyclerAdapter.this, getAdapterPosition());

                }
            });

        }

    }



    public void saveChecklistStandard(ChecklistStandard checklistStandard,int pos){
        JSONObject assQuest;
        try {

            assQuest = smartStore.retrieve(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                    smartStore.lookupSoupEntryId(AssessmentQuestionListLoader.ASSESSMENT_QUESTION_SOUP,
                            Constants.ID, checklistStandard.getmChecklistStandardId())).getJSONObject(0);

            assQuest.put(AssessmentQuestionObject.COMPLIANT, checklistStandard.getmChecklistStandardCompliance());
            assQuest.put(AssessmentQuestionObject.COMMENTS_ACTION, checklistStandard.getmChecklistStandardNonCompliance());
            assQuest.put(AssessmentQuestionObject.QUESTION_ANSWERED, checklistStandard.getChecklistStandardQuestionAnswered());
            /*if(checklistStandard.getmChecklistStandardImageUpload() != null){
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL, AppUtility.BitMapToString(checklistStandard.getmChecklistStandardImageUpload()));
            }else{
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL,"");
            }*/
            if(checklistStandard.getmChecklistStandardImageUploadString() != null &&
                    !checklistStandard.getmChecklistStandardImageUploadString().isEmpty()){
                Log.d("onCreate", "image saved: "+checklistStandard.getmChecklistStandardImageUploadString());
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL,checklistStandard.getmChecklistStandardImageUploadString());
            }else{
                Log.d("onCreate", "image deleted on pos: "+pos);
                assQuest.put(AssessmentQuestionObject.IMAGE_UPLOAD_LOCAL,"");
            }
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
