package com.dztech.auth.service;

import java.util.List;

public final class DriverFieldVerificationFields {

    private static final List<String> VERIFIABLE_FIELDS = List.of(
            "FULL_NAME",
            "DOB",
            "GENDER",
            "EMERGENCY_CONTACT_NAME",
            "EMERGENCY_CONTACT_NUMBER",
            "PERMANENT_ADDRESS",
            "CURRENT_ADDRESS",
            "MOTHER_TONGUE",
            "RELATIONSHIP",
            "LANGUAGES",
            "LICENSE_NUMBER",
            "LICENSE_TYPE",
            "BATCH",
            "EXPIRY_DATE",
            "TRANSMISSION",
            "EXPERIENCE",
            "GOV_ID_TYPE",
            "GOV_ID_NUMBER",
            "EXPIRY_DATE_KYC",
            "BLOOD_GROUP",
            "QUALIFICATION",
            "BATCH_NUMBER",
            "BATCH_EXPIRY_DATE",
            "FATHER_NAME",
            "PROFILE_PHOTO",
            "LICENSE_FRONT",
            "LICENSE_BACK",
            "GOV_ID_FRONT",
            "GOV_ID_BACK");

    private DriverFieldVerificationFields() {}

    public static List<String> all() {
        return VERIFIABLE_FIELDS;
    }
}
