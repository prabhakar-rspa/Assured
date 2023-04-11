package com.rspcaassured.ui.Assessment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.objects.AssessmentObject;
import com.rspcaassured.ui.Checklist.ChecklistActivity;

import java.util.ArrayList;
import java.util.List;

public class AssessmentSectionRecyclerAdapter extends RecyclerView.Adapter<AssessmentSectionRecyclerAdapter.ViewHolder> implements AssessmentItemsRecyclerAdapter.OnAssessmentItemListener {
    List<AssessmentSection> assessmentSectionList;
    Context context;
    public AssessmentSectionRecyclerAdapter(Context context, List<AssessmentSection> assessmentSectionList) {
        this.context = context;
        this.assessmentSectionList = assessmentSectionList;
    }

    @NonNull
    @Override
    public AssessmentSectionRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.assessment_section_recycler, parent, false);
        return new AssessmentSectionRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssessmentSectionRecyclerAdapter.ViewHolder holder, int position) {
        AssessmentSection assessmentSection = assessmentSectionList.get(position);
        String sectionName = assessmentSection.getSectionName();
        List<AssessmentObject> assessmentObjects = assessmentSection.getAssessmentItems();

        holder.sectionNameTextView.setText(sectionName);

        AssessmentItemsRecyclerAdapter assessmentItemsRecyclerAdapter = new AssessmentItemsRecyclerAdapter(assessmentObjects, this);
        holder.assessmentItemsRecyclerView.setAdapter(assessmentItemsRecyclerAdapter);
    }

    @Override
    public int getItemCount() {
        return assessmentSectionList.size();
    }

    @Override
    public void onAssessmentItemClick(int position) {
        List<AssessmentObject> assessmentObjects = new ArrayList<>();
        for(AssessmentSection as : assessmentSectionList){
            assessmentObjects.addAll(as.getAssessmentItems());
        }
        AssessmentObject object = assessmentObjects.get(position);
        int percent = 0;
        if(!object.getAssessmentCompletion().isEmpty()){
             percent = Integer.valueOf(object.getAssessmentCompletion());
        }

        String assessmentStatus = object.getStatus();

        Log.d("assessmentStatus", "assessmentStatus : "+assessmentStatus);

        Intent intent = new Intent(context, ChecklistActivity.class);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(AssessmentOverviewActivity.OBJECT_ID_KEY, object.getObjectId());
        intent.putExtra(AssessmentOverviewActivity.OBJECT_NAME_KEY, object.getName());
        intent.putExtra("applicationId", object.getApplicationId());
        intent.putExtra("rearingSystem", object.getRearingSystem());
        intent.putExtra("animal", object.getAnimals());
        /*if(percent==100){
            intent.putExtra("readonly",true);
        }else{
            intent.putExtra("readonly",false);
        }*/
        if(assessmentStatus!=null && assessmentStatus.equals("Completed")){
            intent.putExtra("readonly",true);
        }else{
            intent.putExtra("readonly",false);
        }
        context.startActivity(intent);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionNameTextView;
        RecyclerView assessmentItemsRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            assessmentItemsRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
