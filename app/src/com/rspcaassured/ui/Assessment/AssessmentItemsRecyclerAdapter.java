package com.rspcaassured.ui.Assessment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.objects.AssessmentObject;

import java.util.List;

public class AssessmentItemsRecyclerAdapter extends RecyclerView.Adapter<AssessmentItemsRecyclerAdapter.ViewHolder>{

    private OnAssessmentItemListener mOnAssessmentItemListener;

    List<AssessmentObject> items;

    public AssessmentItemsRecyclerAdapter(List<AssessmentObject> items, OnAssessmentItemListener onAssessmentItemListener) {
        this.items = items;
        this.mOnAssessmentItemListener = onAssessmentItemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.assessment_list_recycler, parent, false);
        return new ViewHolder(view, mOnAssessmentItemListener);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssessmentObject object = items.get(position);
        holder.checklistAnimal.setText(object.getAnimals() + " Checklist");
        holder.checklistNumberValue.setText(object.getName());
        holder.checklistStatus.setText(object.getStatus());
        // Progress Percentage
        String applicationProgress = object.getAssessmentCompletion();
        if(applicationProgress == ""){
            applicationProgress = "0";
        }
        holder.checklistProgressPercentage.setText(applicationProgress + "%");
        String assessmentStatus = object.getStatus();
        /*if(Integer.valueOf(applicationProgress) == 100){
            //show tick
            holder.syncStatusView.setVisibility(View.VISIBLE);
        }else{
            //hide tick
            holder.syncStatusView.setVisibility(View.GONE);
        }*/
        if(assessmentStatus!=null && assessmentStatus.equals("Completed")){
            //show tick
            holder.syncStatusView.setVisibility(View.VISIBLE);
        }else{
            //hide tick
            holder.syncStatusView.setVisibility(View.GONE);
        }
        holder.checklistProgressBar.setProgress(Integer.valueOf(applicationProgress));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView checklistAnimal;
        TextView checklistNumberValue;
        TextView checklistStatus;
        TextView checklistProgressPercentage;
        ProgressBar checklistProgressBar;
        RelativeLayout syncStatusView;

        OnAssessmentItemListener onAssessmentItemListener;

        public ViewHolder(@NonNull View itemView, OnAssessmentItemListener onAssessmentItemListener) {
            super(itemView);

            checklistAnimal = itemView.findViewById(R.id.checklistAnimal);
            checklistNumberValue = itemView.findViewById(R.id.checklistNumberValue);
            checklistStatus = itemView.findViewById(R.id.checklistStatus);
            checklistProgressPercentage = itemView.findViewById(R.id.checklistProgressPercentage);
            checklistProgressBar = (ProgressBar)itemView.findViewById(R.id.checklistProgressBar);
            syncStatusView = (RelativeLayout) itemView.findViewById(R.id.syncStatusView);

            this.onAssessmentItemListener = onAssessmentItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onAssessmentItemListener.onAssessmentItemClick(getAdapterPosition());
        }
    }

    public interface OnAssessmentItemListener{
        void onAssessmentItemClick(int position);
    }
}
