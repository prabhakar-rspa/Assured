package com.rspcaassured.objects;

import android.text.TextUtils;

import com.salesforce.androidsdk.mobilesync.model.SalesforceObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;

import org.json.JSONObject;

public class AssessmentQuestionObject extends SalesforceObject {
    // fields
    public static final String NAME = "Name";
    public static final String ASSESSMENT_CHECKLIST = "Assessment_Checklist__c";
    public static final String SECTION = "Section__c";
    public static final String SECTION_ORDER = "Section_Order__c";
    public static final String SUBSECTION = "Subsection__c";
    public static final String SUBSECTION_ORDER = "Subsection_Order__c";
    public static final String STANDARD = "Standard__c";
    public static final String STANDARD_ORDER = "Standard_Order__c";
    public static final String SUMMARY = "Summary__c";
    public static final String COMPLIANT = "Compliant__c";
    public static final String COMMENTS_ACTION = "Comments_Action__c";
    public static final String GUIDANCE_NOTES = "Guidance_Notes__c";
    public static final String QUESTION_ANSWERED = "Question_Answered__c";
    public static final String CONTENT_VERSION = "ContentVersion_URL__c";
    //public static final String IMAGE_UPLOAD_LOCAL = "imageUploadLocal";
    public static final String IMAGE_UPLOAD_LOCAL = "ImageUploadLocal__c";
    public static final String NOTES_UPLOAD_LOCAL = "notesUploadLocal"; //update this field
    public static final String EVIDENCE_REQUIRED = "Evidence_Required__c";
    public static final String OTHER_EVIDENCE = "Other_Evidence__c";
    public static final String CORRECTION = "Correction__c";

    private final boolean isLocallyCreated;
    private final boolean isLocallyDeleted;
    private final boolean isLocallyModified;

    /**
     * Parameterized constructor.
     *
     * @param data Raw data for object.
     */
    public AssessmentQuestionObject(JSONObject data) {
        super(data);
        objectType = "Assessment_Checklist_Question__c";
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

    public String getAssessmentChecklist() {
        return sanitizeText(rawData.optString(ASSESSMENT_CHECKLIST));
    }

    public String getSECTION() {
        return sanitizeText(rawData.optString(SECTION));
    }

    public Integer getSectionOrder() {
        return Integer.valueOf(sanitizeText(rawData.optString(SECTION_ORDER)));
    }

    public String getSUBSECTION() {
        return sanitizeText(rawData.optString(SUBSECTION));
    }

    public Integer getSubsectionOrder() {
        return Integer.valueOf(sanitizeText(rawData.optString(SUBSECTION_ORDER)));
    }

    public String getSTANDARD() {
        return sanitizeText(rawData.optString(STANDARD));
    }

    public Integer getStandardOrder() {
        return Integer.valueOf(sanitizeText(rawData.optString(STANDARD_ORDER)));
    }

    public String getSUMMARY() {
        return sanitizeText(rawData.optString(SUMMARY));
    }

    public String getCOMPLIANT() {
        return sanitizeText(rawData.optString(COMPLIANT));
    }

    public String getCommentsAction() {
        return sanitizeText(rawData.optString(COMMENTS_ACTION));
    }

    public String getGuidanceNotes() {
        return sanitizeText(rawData.optString(GUIDANCE_NOTES));
    }

    public Boolean getQuestionAnswered() {
        return Boolean.valueOf(sanitizeText(rawData.optString(QUESTION_ANSWERED)));
    }

    public String getContentVersion() {
        return sanitizeText(rawData.optString(CONTENT_VERSION));
    }

    public String getImageUploadLocal(){
        return sanitizeText(rawData.optString(IMAGE_UPLOAD_LOCAL));
    }

    public String getEvidenceRequired(){
        return sanitizeText(rawData.optString(EVIDENCE_REQUIRED));
    }

    public String getOtherEvidence(){
        return sanitizeText(rawData.optString(OTHER_EVIDENCE));
    }

    public String getCorrection(){
        return sanitizeText(rawData.optString(CORRECTION));
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
