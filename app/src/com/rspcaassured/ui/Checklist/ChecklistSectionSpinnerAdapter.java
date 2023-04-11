package com.rspcaassured.ui.Checklist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rspcaassured.R;


import java.util.ArrayList;

public class ChecklistSectionSpinnerAdapter extends ArrayAdapter<ChecklistSectionItem> {

    public ChecklistSectionSpinnerAdapter(Context context, ArrayList<ChecklistSectionItem> checklistSectionItemsItems) {
        super(context, 0, checklistSectionItemsItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.checklist_spinner_row, parent, false
            );
        }
        RelativeLayout spinnerRelativeLayout = (RelativeLayout)convertView.findViewById(R.id.checklistSpinnerRelativeLayout);
        ImageView bar_image = (ImageView)convertView.findViewById(R.id.bar_image);
        TextView sectionName = (TextView)convertView.findViewById(R.id.sectionName);
        if(spinnerRelativeLayout != null){
            spinnerRelativeLayout.setBackground(null);
        }
        if(sectionName != null){
            sectionName.setText(null);
        }
        if(bar_image != null){
            bar_image.setBackground(null);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.checklist_spinner_row, parent, false
            );
        }

        TextView sectionName = (TextView)convertView.findViewById(R.id.sectionName);

        ChecklistSectionItem currentChecklistSectionItem = getItem(position);

        if(currentChecklistSectionItem != null) {
            sectionName.setText(currentChecklistSectionItem.getSectionName());
        }

        return convertView;
    }
}
