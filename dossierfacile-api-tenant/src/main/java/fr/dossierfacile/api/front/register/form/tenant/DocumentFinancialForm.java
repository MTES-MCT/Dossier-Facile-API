package fr.dossierfacile.api.front.register.form.tenant;

import fr.dossierfacile.api.front.register.form.DocumentForm;
import fr.dossierfacile.api.front.register.form.IDocumentFinancialForm;
import fr.dossierfacile.api.front.validator.annotation.*;
import fr.dossierfacile.api.front.validator.annotation.common.financial.MonthlySumValue;
import fr.dossierfacile.api.front.validator.annotation.tenant.financial.NoDocumentCustomTextFinancial;
import fr.dossierfacile.api.front.validator.annotation.tenant.financial.NumberOfDocumentFinancial;
import fr.dossierfacile.api.front.validator.group.FinancialDocumentGroup;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

import static fr.dossierfacile.common.enums.DocumentCategoryStep.*;
import static fr.dossierfacile.common.enums.DocumentSubCategory.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@NoDocumentCustomTextFinancial
@NumberOfDocumentFinancial
@NumberOfPages(category = DocumentCategory.FINANCIAL, max = 50)
@MonthlySumValue
// The validation is only effective in this group: FinancialDocumentGroup
// This group is added for tenant side validation
// This group is also added for partner side only if a categoryStep is present in the payload
@FinancialDocument(groups = FinancialDocumentGroup.class)
public class DocumentFinancialForm extends DocumentForm implements IDocumentFinancialForm {

    @Parameter(description = "Identifiant du document est nécessaire poour les mises à jour")
    private Long id;

    @NotNull
    @DocumentSubcategorySubset(anyOf =
            {SALARY, SOCIAL_SERVICE, RENT, PENSION, SCHOLARSHIP, NO_INCOME})
    private DocumentSubCategory typeDocumentFinancial;

    @Nullable
    @DocumentCategoryStepSubset(anyOf = {
            SALARY_EMPLOYED_LESS_3_MONTHS,
            SALARY_EMPLOYED_MORE_3_MONTHS,
            SALARY_EMPLOYED_NOT_YET,
            SALARY_FREELANCE_AUTOENTREPRENEUR,
            SALARY_FREELANCE_OTHER,
            SALARY_INTERMITTENT,
            SALARY_ARTIST_AUTHOR,
            SALARY_UNKNOWN,
            SOCIAL_SERVICE_CAF_LESS_3_MONTHS,
            SOCIAL_SERVICE_CAF_MORE_3_MONTHS,
            SOCIAL_SERVICE_FRANCE_TRAVAIL_LESS_3_MONTHS,
            SOCIAL_SERVICE_FRANCE_TRAVAIL_MORE_3_MONTHS,
            SOCIAL_SERVICE_FRANCE_TRAVAIL_NOT_YET,
            SOCIAL_SERVICE_APL_LESS_3_MONTHS,
            SOCIAL_SERVICE_APL_MORE_3_MONTHS,
            SOCIAL_SERVICE_APL_NOT_YET,
            SOCIAL_SERVICE_AAH_LESS_3_MONTHS,
            SOCIAL_SERVICE_AAH_MORE_3_MONTHS,
            SOCIAL_SERVICE_AAH_NOT_YET,
            SOCIAL_SERVICE_OTHER,
            PENSION_STATEMENT,
            PENSION_NO_STATEMENT,
            PENSION_DISABILITY_LESS_3_MONTHS,
            PENSION_DISABILITY_MORE_3_MONTHS,
            PENSION_DISABILITY_NOT_YET,
            PENSION_ALIMONY,
            PENSION_UNKNOWN,
            RENT_RENTAL_RECEIPT,
            RENT_RENTAL_NO_RECEIPT,
            RENT_ANNUITY_LIFE,
            RENT_OTHER
    })
    private DocumentCategoryStep categoryStep;

    private Integer monthlySum;

    @NotNull
    private Boolean noDocument;

    @LengthOfText(max = 2000)
    private String customText;
}
