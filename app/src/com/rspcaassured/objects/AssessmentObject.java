package com.rspcaassured.objects;

import android.text.TextUtils;

import com.salesforce.androidsdk.mobilesync.model.SalesforceObject;
import com.salesforce.androidsdk.mobilesync.target.SyncTarget;
import com.salesforce.androidsdk.mobilesync.util.Constants;

import org.json.JSONObject;

public class AssessmentObject extends SalesforceObject {
    public static final String NAME = "Name";
    public static final String APPLICATION_ID = "Application__c";
    public static final String CHECKLIST_TYPE = "Checklist_Type__c";
    public static final String STATUS = "Status__c";
    public static final String ANIMALS = "Animals__c";
    public static final String REARING_SYSTEM = "Rearing_System__c";
    public static final String ASSESSMENT_COMPLETION = "Assessment_Completion__c";
    public static final String SIGNED_ASSESSOR = "Signed_by_Assessor__c";
    public static final String SIGNED_ASSESSOR_DATE = "Signed_by_Assessor_Date__c";
    public static final String SIGNED_MEMBER = "Signed_by_Member__c";
    public static final String SIGNED_MEMBER_DATE = "Signed_by_Member_Date__c";

    public static final String MEMBER_SIGNATURE_STRING = "Member_Signature_String__c";
    public static final String ASSESSOR_SIGNATURE_STRING = "Assessor_Signature_String__c";

    private final boolean isLocallyCreated;
    private final boolean isLocallyDeleted;
    private final boolean isLocallyModified;

    /**
     * Parameterized constructor.
     *
     * @param data Raw data for object.
     */
    public AssessmentObject(JSONObject data) {
        super(data);
        objectType = "Assessments__c";
        objectId = data.optString(Constants.ID);
        name = data.optString(NAME);
        isLocallyCreated = data.optBoolean(SyncTarget.LOCALLY_CREATED);
        isLocallyDeleted = data.optBoolean(SyncTarget.LOCALLY_DELETED);
        final boolean isLocallyUpdated = data.optBoolean(SyncTarget.LOCALLY_UPDATED);
        isLocallyModified = isLocallyCreated || isLocallyUpdated || isLocallyDeleted;
    }
    /**
     * Returns name of the application.
     *
     * @return name of the application.
     */


    public String getMemberSignature() {
        return sanitizeText(rawData.optString(MEMBER_SIGNATURE_STRING));
    }

    public String getAssessorSignature() {
        return sanitizeText(rawData.optString(ASSESSOR_SIGNATURE_STRING));
    }

    public String getName() {
        return sanitizeText(rawData.optString(NAME));
    }

    /**
     * Returns application Id linked to the assessment.
     *
     * @return application Id.
     */
    public String getApplicationId() {
        return sanitizeText(rawData.optString(APPLICATION_ID));
    }

    /**
     * Returns checklist type of the assessment.
     *
     * @return checklist type.
     */
    public String getChecklistType() {
        return sanitizeText(rawData.optString(CHECKLIST_TYPE));
    }

    /**
     * Returns status of the assessment.
     *
     * @return status.
     */
    public String getStatus() {
        return sanitizeText(rawData.optString(STATUS));
    }

    /**
     * Returns animals of the assessment.
     *
     * @return animals.
     */
    public String getAnimals() {
        return sanitizeText(rawData.optString(ANIMALS));
    }

    /**
     * Returns rearing system for the animals in assessment.
     *
     * @return rearing system.
     */
    public String getRearingSystem(){
        return sanitizeText(rawData.optString(REARING_SYSTEM));
    }

    /**
     * Returns assessment completion.
     *
     * @return assessment completion.
     */
    public String getAssessmentCompletion() {
        return sanitizeText(rawData.optString(ASSESSMENT_COMPLETION));
    }

    public String getSignedAssessor(){
        return sanitizeText(rawData.optString(SIGNED_ASSESSOR));
    }

    public String getSignedAssessorDate(){
        return sanitizeText(rawData.optString(SIGNED_ASSESSOR_DATE));
    }

    public String getSignedMember(){
        return sanitizeText(rawData.optString(SIGNED_MEMBER));
    }

    public String getSignedMemberDateDate(){
        return sanitizeText(rawData.optString(SIGNED_MEMBER_DATE));
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
     * @param text
     * @return Returns "" if the text is null or empty, otherwise same text is returned.
     */
    private String sanitizeText(String text) {
        if (TextUtils.isEmpty(text) || text.equals(Constants.NULL_STRING)) {
            return Constants.EMPTY_STRING;
        }
        return text;
    }
}
