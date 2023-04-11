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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rspcaassured.R;
import com.rspcaassured.Utility.AppUtility;
import com.rspcaassured.loaders.AssessmentQuestionListLoader;
import com.rspcaassured.objects.AssessmentQuestionObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;
import com.tooltip.Tooltip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;



public class IncompleteListAdapter extends
        RecyclerView.Adapter<IncompleteListAdapter.ViewHolder> {
    private List<ChecklistQStatus> statusList;
    private Context ctx;
    private IncompleteQuestion incompleteQuestion;

    public IncompleteListAdapter(
            List<ChecklistQStatus> statusList,
            Context ctx,
            IncompleteQuestion incompleteQuestion
    ) {
        this.statusList = statusList;
        this.ctx = ctx;
        this.incompleteQuestion = incompleteQuestion;
    }

    @NonNull
    @Override
    public IncompleteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.incomplete_row, parent, false);
        return new IncompleteListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncompleteListAdapter.ViewHolder holder, int position) {
        ChecklistQStatus checklistQStatus = statusList.get(position);

        if(checklistQStatus.getNotAns()>0 || checklistQStatus.getNoComments()>0){
            holder.sectionName.setVisibility(View.VISIBLE);
            holder.sectionName.setText(checklistQStatus.getSectionName());
        }else{
            holder.sectionName.setVisibility(View.GONE);
        }
        //set data
        if(checklistQStatus.getNotAns()>0){
            holder.unAnsTv.setVisibility(View.VISIBLE);
            holder.unAnsTv.setText("Unanswered questions = "+checklistQStatus.getNotAns());
        }else{
            holder.unAnsTv.setVisibility(View.GONE);
        }

        if(checklistQStatus.getNoComments()>0){
            holder.noCommentTv.setVisibility(View.VISIBLE);
            holder.noCommentTv.setText("Questions without comments = "+checklistQStatus.getNoComments());
        }else{
            holder.noCommentTv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sectionName,unAnsTv,noCommentTv;
        ConstraintLayout rowIncomplete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.sectionName);
            unAnsTv = itemView.findViewById(R.id.unAnsTv);
            noCommentTv = itemView.findViewById(R.id.noCommentTv);
            rowIncomplete = itemView.findViewById(R.id.rowIncomplete);
            rowIncomplete = itemView.findViewById(R.id.rowIncomplete);



            //set onclicks
            rowIncomplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incompleteQuestion.moveToSection(statusList.get(getAdapterPosition()));
                }
            });
        }



    }




}

