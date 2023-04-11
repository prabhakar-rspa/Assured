package com.rspcaassured.ui.Checklist;

import java.util.List;

public class ChecklistSubsection {
    public String mSubsectionName;
    public Integer mSubsectionOrder;
    public List<ChecklistStandard> mChecklistStandards;
    public Integer mSubSectionProgress;
    private boolean expanded;

    public ChecklistSubsection(String subsectionName, Integer subsectionOder,  List<ChecklistStandard> checklistStandards, Integer subSectionProgress) {
        this.mSubsectionName = subsectionName;
        this.mSubsectionOrder = subsectionOder;
        this.mChecklistStandards = checklistStandards;
        this.mSubSectionProgress = subSectionProgress;

        // When ChecklistSubsection is created, the expanded should be false so that it is displayed in a collapse view
        this.expanded = false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getSubsectionName() {
        return mSubsectionName;
    }

    public Integer getmSubsectionOrder() {
        return mSubsectionOrder;
    }

    public void setmSubsectionOrder(Integer mSubsectionOrder) {
        this.mSubsectionOrder = mSubsectionOrder;
    }

    public List<ChecklistStandard> getChecklistStandards() {
        return mChecklistStandards;
    }
    public void setChecklistStandards(List<ChecklistStandard> checklistStandardList){
        this.mChecklistStandards = checklistStandardList;
    }
    public Integer getSubSectionProgress(){
        return mSubSectionProgress;
    }

    public void setSubsectionProgress(int progress){
        this.mSubSectionProgress = progress;
    }
}
