package fr.dossierfacile.api.front.register.form.tenant;

import fr.dossierfacile.api.front.register.form.DocumentForm;
import fr.dossierfacile.api.front.validator.anotation.DocumentSubcategorySubset;
import fr.dossierfacile.api.front.validator.anotation.LengthOfText;
import fr.dossierfacile.api.front.validator.anotation.NumberOfPages;
import fr.dossierfacile.api.front.validator.anotation.tenant.tax.NumberOfDocumentTax;
import fr.dossierfacile.api.front.validator.anotation.tenant.tax.OtherTaxCustomText;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

import java.util.List;

import static fr.dossierfacile.common.enums.DocumentSubCategory.LESS_THAN_YEAR;
import static fr.dossierfacile.common.enums.DocumentSubCategory.MY_NAME;
import static fr.dossierfacile.common.enums.DocumentSubCategory.MY_PARENTS;
import static fr.dossierfacile.common.enums.DocumentSubCategory.OTHER_TAX;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@NumberOfDocumentTax
@OtherTaxCustomText
@NumberOfPages(category = DocumentCategory.TAX, max = 10)
public class DocumentTaxForm extends DocumentForm {

    @NotNull
    @DocumentSubcategorySubset(anyOf =
            {MY_NAME, MY_PARENTS, LESS_THAN_YEAR, OTHER_TAX})
    private DocumentSubCategory typeDocumentTax;

    @NotNull
    private Boolean noDocument;

    @LengthOfText(max = 1355)
    private String customText;

    private Boolean avisDetected;
}
