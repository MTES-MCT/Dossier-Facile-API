package fr.dossierfacile.api.front.model.tenant;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentStatus;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentModel {
    private Long id;
    private DocumentCategory documentCategory;
    private DocumentSubCategory documentSubCategory; // TODO delete this field 6 months from now
    private DocumentSubCategory subCategory;
    private Boolean noDocument;
    private String customText;
    private Integer monthlySum;
    private DocumentStatus documentStatus;
    private DocumentDeniedReasonsModel documentDeniedReasons;
    private String name;
    private List<FileModel> files;
}
