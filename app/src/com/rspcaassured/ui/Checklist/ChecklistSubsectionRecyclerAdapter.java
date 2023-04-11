package com.rspcaassured.ui.Checklist;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
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

public class ChecklistSubsectionRecyclerAdapter extends RecyclerView.Adapter<ChecklistSubsectionRecyclerAdapter.ViewHolder> {
    List<ChecklistSubsection> mChecklistSubsections;
    ChecklistActivity mActivity;
    RefreshSpinner refreshSpinner;
    MarkNAPopup markNAPopup;
    List<AllBitmap> allBitmapList;
    boolean listAlreadyLaunched = false;
    private OpenKeyboard openKeyboard;
    SmartStore smartStore;

    public ChecklistSubsectionRecyclerAdapter(
            List<AllBitmap> allBitmapList,
            ChecklistActivity activity,
            List<ChecklistSubsection> checklistSubsections,
            RefreshSpinner refreshSpinner,
            OpenKeyboard openKeyboard,
            MarkNAPopup markNAPopup
    ) {
        this.listAlreadyLaunched = false;
        this.allBitmapList = allBitmapList;
        this.mChecklistSubsections = checklistSubsections;
        this.mActivity = activity;
        this.refreshSpinner = refreshSpinner;
        this.openKeyboard = openKeyboard;
        this.markNAPopup = markNAPopup;


        UserAccount curAccount = MobileSyncSDKManager.getInstance().getUserAccountManager().getCurrentUser();
        smartStore = MobileSyncSDKManager.getInstance().getSmartStore(curAccount);
    }

    @NonNull
    @Override
    public ChecklistSubsectionRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.checklist_subsection_recycler, parent, false);
        return new ChecklistSubsectionRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistSubsectionRecyclerAdapter.ViewHolder holder, int position) {
        int listSize = mChecklistSubsections.size();
        ChecklistSubsection checklistSubsection = mChecklistSubsections.get(position);
        String subSectionName = checklistSubsection.getSubsectionName();
        Integer subsectionProgress = checklistSubsection.getSubSectionProgress();
        List<ChecklistStandard> checklistStandards = checklistSubsection.getChecklistStandards();

        Log.d("CLSRecycler", "checklistStandards => " + checklistStandards);
        Log.d("CLSRecycler", "checklistStandards size => " + checklistStandards.size());

        holder.subSectionNameTextView.setText(subSectionName);
        holder.subsectionProgress.setProgress(subsectionProgress);
        holder.subsectionProgressPercentage.setText(String.valueOf(subsectionProgress) + '%');

        if(listSize == 1 && !listAlreadyLaunched){
            checklistSubsection.setExpanded(true);
            listAlreadyLaunched = true;
        }
        Boolean isExpanded = checklistSubsection.isExpanded();
        Log.d("onCreate", "onBindViewHolder: isExpanded : "+isExpanded);
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.expandIcon.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        holder.collapseIcon.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.markAllNA.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if(mActivity.darkModeEnabled){
            holder.expandIcon.setColorFilter(mActivity.getResources().getColor(R.color.textColorDarkMode));
            holder.collapseIcon.setColorFilter(mActivity.getResources().getColor(R.color.textColorDarkMode));
        }

        if(mActivity.readOnly){
            holder.markAllNA.setClickable(false);
        }


        //---new bug solution starts----
        if(position > allBitmapList.size()-1){
            return;
        }
        //---new bug solution ends----
        AllBitmap allBitmap = allBitmapList.get(position);
        List<ChecklistStandardBitmap> checklistStandardBitmapList = allBitmap.getBitmapList();
        ChecklistStandardRecyclerAdapter checklistStandardRecyclerAdapter = new ChecklistStandardRecyclerAdapter(
                checklistStandardBitmapList,
                checklistStandards,
                mActivity,
                this,
                position,
                holder.subsectionProgress,
                holder.subsectionProgressPercentage,
                openKeyboard
        );
        holder.checklistStandardsRecyclerView.setAdapter(checklistStandardRecyclerAdapter);

    }

    @Override
    public int getItemCount() {
        return mChecklistSubsections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView subSectionNameTextView;
        RecyclerView checklistStandardsRecyclerView;
        ProgressBar subsectionProgress;
        TextView subsectionProgressPercentage;
        ConstraintLayout expandableLayout,checklistSubsectionNameRecycler;

        ImageView expandIcon;
        ImageView collapseIcon;

        CardView expandCardLayout;
        TextView markAllNA;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            subSectionNameTextView = itemView.findViewById(R.id.checklistSubsectionNameText);
            checklistStandardsRecyclerView = itemView.findViewById(R.id.checklistStandardRecyclerView);
            subsectionProgress = itemView.findViewById(R.id.subSectionProgressBar);
            subsectionProgressPercentage = itemView.findViewById(R.id.subSectionProgressPercentage);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            expandIcon = itemView.findViewById(R.id.expandSubsection);
            collapseIcon = itemView.findViewById(R.id.collapseSubsection);
            expandCardLayout = itemView.findViewById(R.id.expandLayout);
            checklistSubsectionNameRecycler = itemView.findViewById(R.id.checklistSubsectionNameRecycler);
            markAllNA = itemView.findViewById(R.id.markAllNA);

            /*expandCardLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistSubsection subsection = mChecklistSubsections.get(getAdapterPosition());
                    Log.d("onCreate", "expand: "+subsection.isExpanded());
                    if(!subsection.isExpanded()){
                        //expand
                        subsection.setExpanded(true);
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });*/

            markAllNA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistSubsection checklistSubsection = mChecklistSubsections.get(getAdapterPosition());
                    List<ChecklistStandard> checklistStandards = checklistSubsection.getChecklistStandards();

                    markNAPopup.showMarkNAPopup(checklistStandards,getAdapterPosition());

                 /*   checklistStandards.forEach((sd)->{
                        sd.setmChecklistStandardCompliance("N/A");

                        // Mark checklist question as answered
                        sd.setChecklistStandardQuestionAnswered(true);

                        // Save the Assessment Question to smart store
                        saveChecklistStandard(sd,getAdapterPosition());
                    });

                    notifyItemChanged(getAdapterPosition());*/
                }
            });

            checklistSubsectionNameRecycler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistSubsection subsection = mChecklistSubsections.get(getAdapterPosition());
                    Log.d("onCreate", "expand: "+subsection.isExpanded());
                    if(!subsection.isExpanded()){
                        //expand
                        subsection.setExpanded(true);
                        notifyItemChanged(getAdapterPosition());
                    }else{
                        //collapse
                        subsection.setExpanded(false);
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });

            /*collapseIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChecklistSubsection subsection = mChecklistSubsections.get(getAdapterPosition());
                    subsection.setExpanded(false);
                    notifyItemChanged(getAdapterPosition());
                }
            });*/


        }
    }

    public void refreshSubsection(int position, List<ChecklistStandard> checklistStandard, ProgressBar subsectionProgressBar, TextView subsectionProgressPercentage){
        refreshSpinner.refreshDropdown();
        if(mChecklistSubsections != null){
            Boolean dataChanged = false;
            // get Checklist Subsection using position
            ChecklistSubsection checklistSubsection = mChecklistSubsections.get(position);
            if(checklistSubsection != null){
                // Update the Checklist Standard list for this subsection
                checklistSubsection.setChecklistStandards(checklistStandard);

                // Calculate new progress based on checklist standards answered
                int completed = 0;
                assert checklistStandard != null;
                for(ChecklistStandard sd : checklistStandard){
                    if(sd.getChecklistStandardQuestionAnswered()){
                        completed = completed +1;
                    }
                }
                int percent = (int)Math.round(((double) completed / checklistStandard.size()) * 100);

                // Update the percentage on Checklist subsection
                checklistSubsection.setSubsectionProgress(percent);

                // Update the progress bar for the subsection
                subsectionProgressBar.setProgress(percent);
                subsectionProgressPercentage.setText(String.valueOf(percent) + '%');


            }
        }
    }



    void doMarkAllNA(List<ChecklistStandard> checklistStandards,int pos){
        checklistStandards.forEach((sd)->{
            sd.setmChecklistStandardCompliance("N/A");

            // Mark checklist question as answered
            sd.setChecklistStandardQuestionAnswered(true);

            // Save the Assessment Question to smart store
            saveChecklistStandard(sd,pos);
        });

        notifyItemChanged(pos);
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
            Log.e("Error", "JSONException occurred while parsing", e);
        }
    }
}
