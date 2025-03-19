package fr.dossierfacile.api.front.register.form;

import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;

public interface IDocumentResidencyForm {
    DocumentSubCategory getTypeDocumentResidency();

    DocumentCategoryStep getCategoryStep();
}
