package com.rspcaassured.ui.Assessment;

import com.rspcaassured.objects.AssessmentObject;

import java.util.List;

public class AssessmentSection {
    private String sectionName;
    private List<AssessmentObject> assessmentItems;

    public AssessmentSection(String sectionName, List<AssessmentObject> assessmentItems){
        this.sectionName = sectionName;
        this.assessmentItems = assessmentItems;
    }

    public String getSectionName() {
        return sectionName;
    }

    public List<AssessmentObject> getAssessmentItems() {
        return assessmentItems;
    }
}