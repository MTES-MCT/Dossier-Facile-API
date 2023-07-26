package fr.dossierfacile.common.mapper;

import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import fr.dossierfacile.common.model.apartment_sharing.DocumentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static fr.dossierfacile.common.enums.DocumentSubCategory.DRIVERS_LICENSE;
import static fr.dossierfacile.common.enums.DocumentSubCategory.OTHER;
import static fr.dossierfacile.common.enums.DocumentSubCategory.OTHER_IDENTIFICATION;
import static org.assertj.core.api.Assertions.assertThat;

interface DocumentMappingTest {

    DocumentModel mapDocument(Document document);

    @ParameterizedTest
    @EnumSource(value = DocumentSubCategory.class,
            names = {"INTERMITTENT", "STAY_AT_HOME_PARENT", "NO_ACTIVITY", "ARTIST"})
    @DisplayName("New professional categories are replaced by 'OTHER' in field 'documentSubCategory'")
    default void should_replace_new_professional_categories_by_other(DocumentSubCategory subCategory) {
        DocumentModel documentModel = mapDocumentWithSubCategory(subCategory);

        assertThat(documentModel.getDocumentSubCategory()).isEqualTo(OTHER);
    }

    @ParameterizedTest
    @CsvSource({
            "GUEST_ORGANISM, GUEST",
            "SHORT_TERM_RENTAL, TENANT",
            "OTHER_RESIDENCY, GUEST"
    })
    @DisplayName("New residency categories are replaced by existing ones in field 'documentSubCategory'")
    default void should_replace_new_residency_categories(DocumentSubCategory newSubCategory, DocumentSubCategory existingSubCategory) {
        DocumentModel documentModel = mapDocumentWithSubCategory(newSubCategory);

        assertThat(documentModel.getDocumentSubCategory()).isEqualTo(existingSubCategory);
    }

    @Test
    @DisplayName("Driver's license is replaced by 'OTHER_IDENTIFICATION' in field 'documentSubCategory'")
    default void should_replace_drivers_license_by_other() {
        DocumentModel documentModel = mapDocumentWithSubCategory(DRIVERS_LICENSE);

        assertThat(documentModel.getDocumentSubCategory()).isEqualTo(OTHER_IDENTIFICATION);
    }

    @ParameterizedTest
    @EnumSource(value = DocumentSubCategory.class)
    @DisplayName("All categories are mapped as is in field 'subCategory'")
    default void should_expose_all_categories(DocumentSubCategory subCategory) {
        DocumentModel documentModel = mapDocumentWithSubCategory(subCategory);

        assertThat(documentModel.getSubCategory()).isEqualTo(subCategory);
    }

    private DocumentModel mapDocumentWithSubCategory(DocumentSubCategory subCategory) {
        Document document = Document.builder()
                .documentSubCategory(subCategory)
                .build();

        return mapDocument(document);
    }

}