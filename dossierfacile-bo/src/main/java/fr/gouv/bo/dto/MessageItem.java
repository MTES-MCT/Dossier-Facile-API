package fr.gouv.bo.dto;

import fr.dossierfacile.common.entity.DocumentAnalysisReport;
import fr.dossierfacile.common.entity.DocumentDeniedReasons;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.DocumentCategoryStep;
import fr.dossierfacile.common.enums.DocumentSubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageItem {

    private String commentDoc;
    private Integer monthlySum;
    private String customTex;
    private DocumentCategory documentCategory;
    private DocumentSubCategory documentSubCategory;
    private DocumentCategoryStep documentCategoryStep;
    @Builder.Default
    private List<ItemDetail> itemDetailList = new ArrayList<>();
    private Long documentId;
    private String documentName;
    @Builder.Default
    private List<DisplayableFile> analyzedFiles = new ArrayList<>();
    private Boolean avisDetected;
    private DocumentDeniedReasons previousDeniedReasons;
    private DocumentAnalysisReport documentAnalysisReport;
    private String analysisReportComment;

}
