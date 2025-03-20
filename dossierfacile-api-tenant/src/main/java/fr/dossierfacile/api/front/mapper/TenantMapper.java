package fr.dossierfacile.api.front.mapper;

import fr.dossierfacile.api.front.model.dfc.tenant.ConnectedTenantModel;
import fr.dossierfacile.api.front.model.tenant.*;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.File;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.ApplicationType;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.dossierfacile.common.mapper.CategoriesMapper;
import fr.dossierfacile.common.mapper.MapDocumentCategories;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Mapper(componentModel = "spring")
public abstract class TenantMapper {
    private static final String PATH = "api/document/resource";
    private static final String PREVIEW_PATH = "/api/file/preview/";
    private static final String DOSSIER_PDF_PATH = "/api/application/fullPdf/";
    private static final String DOSSIER_PATH = "/file/";

    @Value("${application.base.url}")
    protected String applicationBaseUrl;

    @Value("${tenant.base.url}")
    protected String tenantBaseUrl;

    protected CategoriesMapper categoriesMapper;

    @Autowired
    public void setCategoriesMapper(CategoriesMapper categoriesMapper) {
        this.categoriesMapper = categoriesMapper;
    }

    @Mapping(target="firstName", source="tenant.tenantFirstName")
    @Mapping(target="lastName", source="tenant.tenantLastName")
    @Mapping(target="preferredName", source="tenant.tenantPreferredName")
    public abstract TenantModel toTenantModel(Tenant tenant, @Context UserApi userApi);

    @Mapping(target = "name", expression = "java((document.getWatermarkFile() != null )? applicationBaseUrl + \"/" + PATH + "/\" + document.getName() : null)")
    @Mapping(target = "files", expression = "java((userApi == null || isHybrid(userApi))? mapFiles(document.getFiles()) : null)")
    @MapDocumentCategories
    public abstract DocumentModel toDocumentModel(Document document, @Context UserApi userApi);

    public abstract List<FileModel> mapFiles(List<File> files);

    @Mapping(target = "connectedTenantId", source = "id")
    public abstract ConnectedTenantModel toTenantModelDfc(Tenant tenant, @Context UserApi userApi);

    @Mapping(target = "preview", expression = "java((documentFile.getPreview() != null )? applicationBaseUrl + \"" + PREVIEW_PATH + "\" + documentFile.getId() : null)")
    @Mapping(target = "size", source = "documentFile.storageFile.size")
    @Mapping(target = "contentType", source = "documentFile.storageFile.contentType")
    @Mapping(target = "originalName", source = "documentFile.storageFile.name")
    public abstract FileModel toFileModel(File documentFile);

    boolean isHybrid(UserApi userApi) {
        return userApi != null && userApi.getName().startsWith("hybrid-");
    }

    @AfterMapping
    void modificationsAfterMapping(@MappingTarget TenantModel.TenantModelBuilder tenantModelBuilder, @Context UserApi userApi) {
        TenantModel tenantModel = tenantModelBuilder.build();
        ApartmentSharingModel apartmentSharingModel = tenantModel.getApartmentSharing();
        if (apartmentSharingModel.getStatus() == TenantFileStatus.VALIDATED) {
            apartmentSharingModel.setDossierPdfUrl(applicationBaseUrl + DOSSIER_PDF_PATH + apartmentSharingModel.getToken());
            apartmentSharingModel.setDossierUrl(tenantBaseUrl + DOSSIER_PATH + apartmentSharingModel.getToken());
        } else {
            apartmentSharingModel.setToken(null);
            apartmentSharingModel.setTokenPublic(null);
        }
        var isDossierUser = SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("SCOPE_dossier"));
        var filePath = isDossierUser ? "/api/file/resource/" : "/api-partner/tenant/" + tenantModel.getId() + "/file/resource/";
        setDocumentDeniedReasonsAndDocumentAndFilesRoutes(tenantModel.getDocuments(), filePath, false, userApi);

        tenantModel.getApartmentSharing().getTenants().stream().filter(t -> Objects.equals(t.getId(), tenantModel.getId())).forEach(
                t -> {
                    t.setDocuments(null);
                    t.setGuarantors(null);
                }
        );
        Optional.ofNullable(tenantModel.getApartmentSharing().getTenants())
                .ifPresent(coTenantModels -> coTenantModels.forEach(coTenantModel -> {
                    // If the tenant is a couple, we want to show the documents of the other tenant
                    // Otherwise we only load the preview.
                    setDocumentDeniedReasonsAndDocumentAndFilesRoutes(
                            coTenantModel.getDocuments(),
                            filePath,
                            tenantModel.getApartmentSharing().getApplicationType() != ApplicationType.COUPLE,
                            userApi
                    );
                    Optional.ofNullable(coTenantModel.getGuarantors())
                            .ifPresent(guarantorModels -> guarantorModels.forEach(guarantorModel -> setDocumentDeniedReasonsAndDocumentAndFilesRoutes(guarantorModel.getDocuments(), filePath, true, userApi)));
                }));

        Optional.ofNullable(tenantModel.getGuarantors())
                .ifPresent(guarantorModels -> guarantorModels.forEach(guarantorModel -> setDocumentDeniedReasonsAndDocumentAndFilesRoutes(guarantorModel.getDocuments(), filePath, false, userApi)));

    }

    private void setDocumentDeniedReasonsAndDocumentAndFilesRoutes(List<DocumentModel> list, String filePath, boolean previewOnly, UserApi userApi) {
        Optional.ofNullable(list)
                .ifPresent(documentModels -> documentModels.forEach(documentModel -> {
                    DocumentDeniedReasonsModel documentDeniedReasonsModel = documentModel.getDocumentDeniedReasons();
                    if (documentDeniedReasonsModel != null) {
                        List<SelectedOption> selectedOptionList = new ArrayList<>();
                        if (documentDeniedReasonsModel.isMessageData()) {
                            for (int i = 0; i < documentDeniedReasonsModel.getCheckedOptions().size(); i++) {
                                String checkedOption = documentDeniedReasonsModel.getCheckedOptions().get(i);
                                Integer checkedOptionsId = documentDeniedReasonsModel.getCheckedOptionsId().get(i);
                                selectedOptionList.add(SelectedOption.builder()
                                        .id(checkedOptionsId)
                                        .label(checkedOption).build());
                            }
                        } else {
                            for (int i = 0; i < documentDeniedReasonsModel.getCheckedOptions().size(); i++) {
                                String checkedOption = documentDeniedReasonsModel.getCheckedOptions().get(i);
                                selectedOptionList.add(SelectedOption.builder().id(null).label(checkedOption).build());
                            }
                        }
                        documentDeniedReasonsModel.setSelectedOptions(selectedOptionList);
                        documentDeniedReasonsModel.setCheckedOptions(null);
                        documentDeniedReasonsModel.setCheckedOptionsId(null);
                        documentModel.setDocumentDeniedReasons(documentDeniedReasonsModel);
                    }
                    Optional.ofNullable(documentModel.getFiles())
                            .ifPresent(fileModels -> fileModels.forEach(fileModel -> {
                                if (previewOnly || isHybrid(userApi)) {
                                    fileModel.setPath("");
                                } else {
                                    fileModel.setPath(applicationBaseUrl + filePath + fileModel.getId());
                                }
                            }));
                }));
    }

    @AfterMapping
    void modificationsAfterMapping(@MappingTarget ConnectedTenantModel.ConnectedTenantModelBuilder connectedTenantModelBuilder, @Context UserApi userApi) {
        ConnectedTenantModel connectedTenantModel = connectedTenantModelBuilder.build();
        fr.dossierfacile.api.front.model.dfc.apartment_sharing.ApartmentSharingModel apartmentSharingModel = connectedTenantModel.getApartmentSharing();
        if (apartmentSharingModel.getStatus() == TenantFileStatus.VALIDATED) {
            apartmentSharingModel.setDossierPdfUrl(applicationBaseUrl + DOSSIER_PDF_PATH + apartmentSharingModel.getToken());
            apartmentSharingModel.setDossierUrl(tenantBaseUrl + DOSSIER_PATH + apartmentSharingModel.getToken());
        } else {
            apartmentSharingModel.setToken(null);
            apartmentSharingModel.setTokenPublic(null);
        }
        connectedTenantModel.getApartmentSharing().getTenants().forEach(tenantModel -> setDocumentDeniedReasonsAndDocumentAndFilesRoutes(tenantModel.getDocuments(), null, true, userApi));
        connectedTenantModel.getApartmentSharing().getTenants().forEach(tenantModel ->
                Optional.ofNullable(tenantModel.getGuarantors()).ifPresent(guarantorModels ->
                        guarantorModels.forEach(guarantorModel -> setDocumentDeniedReasonsAndDocumentAndFilesRoutes(guarantorModel.getDocuments(), null, true, userApi))));
    }

}
