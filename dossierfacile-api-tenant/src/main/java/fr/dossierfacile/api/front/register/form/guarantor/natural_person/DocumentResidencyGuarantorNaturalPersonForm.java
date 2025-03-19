package fr.dossierfacile.api.front.register.form.guarantor.natural_person;

import fr.dossierfacile.api.front.register.form.IDocumentResidencyForm;
import fr.dossierfacile.api.front.register.form.guarantor.DocumentGuarantorFormAbstract;
import fr.dossierfacile.api.front.validator.annotation.*;
import fr.dossierfacile.api.front.validator.annotation.guarantor.natural_person.residency.CustomTextResidencyGuarantorNaturalPerson;
import fr.dossierfacile.api.front.validator.annotation.guarantor.natural_person.residency.NumberOfDocumentResidencyGuarantorNaturalPerson;
import fr.dossierfacile.api.front.validator.group.Dossier;
import fr.dossierfacile.api.front.validator.group.ResidencyDocumentGroup;
import fr.dossierfacile.common.config.ApiVersion;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.enums.TypeGuarantor;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static fr.dossierfacile.common.enums.DocumentCategoryStep.GUEST_NO_PROOF;
import static fr.dossierfacile.common.enums.DocumentCategoryStep.GUEST_PROOF;
import static fr.dossierfacile.common.enums.DocumentSubCategory.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@CustomTextResidencyGuarantorNaturalPerson
@NumberOfDocumentResidencyGuarantorNaturalPerson
@NumberOfPages(category = DocumentCategory.RESIDENCY, max = 20)
@ResidencyDocument(isGuarantorMode = true, groups = ResidencyDocumentGroup.class)
public class DocumentResidencyGuarantorNaturalPersonForm extends DocumentGuarantorFormAbstract implements IDocumentResidencyForm {

    @NotNull
    @DocumentSubcategorySubset(
            anyOf = {
                    TENANT,
                    OWNER,
                    GUEST,
                    GUEST_COMPANY,
                    GUEST_ORGANISM,
                    SHORT_TERM_RENTAL,
                    OTHER_RESIDENCY
            }, groups = {ApiVersion.V4.class, Dossier.class}
    )
    private DocumentSubCategory typeDocumentResidency;

    @Nullable
    @DocumentCategoryStepSubset(anyOf = {GUEST_PROOF, GUEST_NO_PROOF})
    private DocumentCategoryStep categoryStep;

    private TypeGuarantor typeGuarantor = TypeGuarantor.NATURAL_PERSON;

    @LengthOfText(max = 2000)
    private String customText;

}
