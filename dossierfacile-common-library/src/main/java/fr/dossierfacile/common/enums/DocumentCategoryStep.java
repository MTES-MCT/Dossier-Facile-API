package fr.dossierfacile.common.enums;

import lombok.Getter;

@Getter
public enum DocumentCategoryStep {

    // Residency
    TENANT_RECEIPT("document_category_step.TENANT_RECEIPT"),
    TENANT_PROOF("document_category_step.TENANT_PROOF"),
    GUEST_PROOF("document_category_step.GUEST_PROOF"),
    GUEST_NO_PROOF("document_category_step.GUEST_NO_PROOF"),

    // Professional
    SALARY_EMPLOYED_LESS_3_MONTHS("document_category_step.SALARY_EMPLOYED_LESS_3_MONTHS"),
    SALARY_EMPLOYED_MORE_3_MONTHS("document_category_step.SALARY_EMPLOYED_MORE_3_MONTHS"),
    SALARY_EMPLOYED_NOT_YET("document_category_step.SALARY_EMPLOYED_NOT_YET"),
    SALARY_FREELANCE_AUTOENTREPRENEUR("document_category_step.SALARY_FREELANCE_AUTOENTREPRENEUR"),
    SALARY_FREELANCE_OTHER("document_category_step.SALARY_FREELANCE_OTHER"),
    SALARY_INTERMITTENT("document_category_step.SALARY_INTERMITTENT"),
    SALARY_ARTIST_AUTHOR("document_category_step.SALARY_ARTIST_AUTHOR"),
    SALARY_UNKNOWN("document_category_step.SALARY_UNKNOWN"),

    SOCIAL_SERVICE_CAF_LESS_3_MONTHS("document_category_step.SOCIAL_SERVICE_CAF_LESS_3_MONTHS"),
    SOCIAL_SERVICE_CAF_MORE_3_MONTHS("document_category_step.SOCIAL_SERVICE_CAF_MORE_3_MONTHS"),
    SOCIAL_SERVICE_FRANCE_TRAVAIL_LESS_3_MONTHS("document_category_step.SOCIAL_SERVICE_FRANCE_TRAVAIL_LESS_3_MONTHS"),
    SOCIAL_SERVICE_FRANCE_TRAVAIL_MORE_3_MONTHS("document_category_step.SOCIAL_SERVICE_FRANCE_TRAVAIL_MORE_3_MONTHS"),
    SOCIAL_SERVICE_FRANCE_TRAVAIL_NOT_YET("document_category_step.SOCIAL_SERVICE_FRANCE_TRAVAIL_NOT_YET"),
    SOCIAL_SERVICE_APL_LESS_3_MONTHS("document_category_step.SOCIAL_SERVICE_APL_LESS_3_MONTHS"),
    SOCIAL_SERVICE_APL_MORE_3_MONTHS("document_category_step.SOCIAL_SERVICE_APL_MORE_3_MONTHS"),
    SOCIAL_SERVICE_APL_NOT_YET("document_category_step.SOCIAL_SERVICE_APL_NOT_YET"),
    SOCIAL_SERVICE_AAH_LESS_3_MONTHS("document_category_step.SOCIAL_SERVICE_AAH_LESS_3_MONTHS"),
    SOCIAL_SERVICE_AAH_MORE_3_MONTHS("document_category_step.SOCIAL_SERVICE_AAH_MORE_3_MONTHS"),
    SOCIAL_SERVICE_AAH_NOT_YET("document_category_step.SOCIAL_SERVICE_AAH_NOT_YET"),
    SOCIAL_SERVICE_OTHER("document_category_step.SOCIAL_SERVICE_OTHER"),

    PENSION_STATEMENT("document_category_step.PENSION_STATEMENT"),
    PENSION_NO_STATEMENT("document_category_step.PENSION_NO_STATEMENT"),
    PENSION_DISABILITY_LESS_3_MONTHS("document_category_step.PENSION_DISABILITY_LESS_3_MONTHS"),
    PENSION_DISABILITY_MORE_3_MONTHS("document_category_step.PENSION_DISABILITY_MORE_3_MONTHS"),
    PENSION_DISABILITY_NOT_YET("document_category_step.PENSION_DISABILITY_NOT_YET"),
    PENSION_ALIMONY("document_category_step.PENSION_ALIMONY"),
    PENSION_UNKNOWN("document_category_step.PENSION_UNKNOWN"),

    RENT_RENTAL_RECEIPT("document_category_step.RENT_RENTAL_RECEIPT"),
    RENT_RENTAL_NO_RECEIPT("document_category_step.RENT_RENTAL_NO_RECEIPT"),
    RENT_ANNUITY_LIFE("document_category_step.RENT_ANNUITY_LIFE"),
    RENT_OTHER("document_category_step.RENT_OTHER");

    final String label;

    DocumentCategoryStep(String label) {
        this.label = label;
    }

}
