package com.rspcaassured.ui.Checklist;

public class ChecklistSectionItem {
    private String id;
    private String mSectionName;

    public ChecklistSectionItem(String id,String sectionName){
        this.id = id;
        mSectionName = sectionName;
    }

    /*public ChecklistSectionItem(String sectionName){
        mSectionName = sectionName;
    }*/

    public String getSectionName() {
        return mSectionName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ChecklistSectionItem() {
    }
}
