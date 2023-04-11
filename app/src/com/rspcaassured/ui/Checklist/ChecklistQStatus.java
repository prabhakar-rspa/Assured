package com.rspcaassured.ui.Checklist;

import java.io.Serializable;

public class ChecklistQStatus implements Serializable {
    private String sectionName;
    private int notAns;
    private int noComments;

    public ChecklistQStatus() {
    }

    public ChecklistQStatus(String sectionName, int notAns, int noComments) {
        this.sectionName = sectionName;
        this.notAns = notAns;
        this.noComments = noComments;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public int getNotAns() {
        return notAns;
    }

    public void setNotAns(int notAns) {
        this.notAns = notAns;
    }

    public int getNoComments() {
        return noComments;
    }

    public void setNoComments(int noComments) {
        this.noComments = noComments;
    }
}
