package com.rspcaassured.objects;

import android.text.TextUtils;

import com.salesforce.androidsdk.mobilesync.model.SalesforceObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;

import org.json.JSONObject;

public class AssessmentChecklistSectionObject extends SalesforceObject {
    // fields
    public static final String NAME = "Name";
    public static final String ASSESSMENT_LOOKUP = "Assessment__c";
    public static final String SECTION_NAME = "Checklist_Section_Name__c";
    public static final String SECTION_ORDER = "Checklist_Section_Order__c";
    public static final String SECTION_COMMENTS = "Checklist_Section_Comments__c";
    public static final String ACTIVE_SECTION = "activeSection";

    private final boolean isLocallyCreated;
    private final boolean isLocallyDeleted;
    private final boolean isLocallyModified;

    /**
     * Parameterized constructor.
     *
     * @param data Raw data for object.
     */
    public AssessmentChecklistSectionObject(JSONObject data) {
        super(data);
        objectType = "Assessment_Checklist_Section__c";
        objectId = data.optString(Constants.ID);
        name = data.optString(NAME);
        isLocallyCreated = data.optBoolean(SyncTarget.LOCALLY_CREATED);
        isLocallyDeleted = data.optBoolean(SyncTarget.LOCALLY_DELETED);
        final boolean isLocallyUpdated = data.optBoolean(SyncTarget.LOCALLY_UPDATED);
        isLocallyModified = isLocallyCreated || isLocallyUpdated || isLocallyDeleted;
    }


    public String getNAME() {
        return sanitizeText(rawData.optString(NAME));
    }

    public String getAssessmentLookup() {
        return sanitizeText(rawData.optString(ASSESSMENT_LOOKUP));
    }

    public String getSectionName() {
        return sanitizeText(rawData.optString(SECTION_NAME));
    }

    public Integer getSectionOrder() {
        return Integer.valueOf(sanitizeText(rawData.optString(SECTION_ORDER)));
    }

    public String getSectionComments() {
        return sanitizeText(rawData.optString(SECTION_COMMENTS));
    }

    public Boolean getActiveSection(){
        return Boolean.valueOf(sanitizeText(rawData.optString(ACTIVE_SECTION)));
    }


    /**
     * Returns whether the assessment has been locally modified or not.
     *
     * @return True - if the assessment has been locally modified, False - otherwise.
     */
    public boolean isLocallyModified() {
        return isLocallyModified;
    }

    /**
     * Returns whether the assessment has been locally deleted or not.
     *
     * @return True - if the assessment has been locally deleted, False - otherwise.
     */
    public boolean isLocallyDeleted() {
        return isLocallyDeleted;
    }

    /**
     * Returns whether the assessment has been locally created or not.
     *
     * @return True - if the assessment has been locally created, False - otherwise.
     */
    public boolean isLocallyCreated() {
        return isLocallyCreated;
    }

    /**
     * Checks if the text is empty or null. If it is, return "", otherwise return the text.
     *
     * @param text text value to sanitise
     * @return Returns "" if the text is null or empty, otherwise same text is returned.
     */
    private String sanitizeText(String text) {
        if (TextUtils.isEmpty(text) || text.equals(Constants.NULL_STRING)) {
            return Constants.EMPTY_STRING;
        }
        return text;
    }
}
