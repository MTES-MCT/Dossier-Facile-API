package fr.dossierfacile.common.service;

import fr.dossierfacile.common.entity.ApartmentSharing;
import fr.dossierfacile.common.entity.Document;
import fr.dossierfacile.common.entity.Guarantor;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.TenantUserApi;
import fr.dossierfacile.common.entity.TenantUserApiKey;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.PartnerCallBackType;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.dossierfacile.common.mapper.ApplicationFullMapper;
import fr.dossierfacile.common.model.LightAPIInfoModel;
import fr.dossierfacile.common.model.TenantLightAPIInfoModel;
import fr.dossierfacile.common.model.apartment_sharing.ApplicationModel;
import fr.dossierfacile.common.repository.TenantCommonRepository;
import fr.dossierfacile.common.repository.TenantUserApiRepository;
import fr.dossierfacile.common.service.interfaces.CallbackLogService;
import fr.dossierfacile.common.service.interfaces.PartnerCallBackService;
import fr.dossierfacile.common.service.interfaces.RequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerCallBackServiceImpl implements PartnerCallBackService {

    private final TenantCommonRepository tenantRepository;
    private final TenantUserApiRepository tenantUserApiRepository;
    private final ApplicationFullMapper applicationFullMapper;
    private final RequestService requestService;
    private final CallbackLogService callbackLogService;

    @Value("${callback.domain:default}")
    private String callbackDomain;

    public void sendCallBack(Tenant tenant) {
        TenantFileStatus tenantFileStatus = tenant.getStatus();
        if (tenantFileStatus == TenantFileStatus.VALIDATED
                || tenantFileStatus == TenantFileStatus.TO_PROCESS) {

            List<TenantUserApi> tenantUserApiList = tenant.getTenantsUserApi();
            if (tenantUserApiList != null && !tenantUserApiList.isEmpty()) {
                for (TenantUserApi tenantUserApi : tenantUserApiList
                ) {
                    UserApi userApi = tenantUserApi.getUserApi();
                    if (userApi.getVersion() != null && userApi.getUrlCallback() != null) {
                        sendCallBack(
                                tenant,
                                userApi,
                                tenantFileStatus == TenantFileStatus.VALIDATED ?
                                        PartnerCallBackType.VERIFIED_ACCOUNT :
                                        PartnerCallBackType.CREATED_ACCOUNT
                        );
                    }
                }
            }
        }
    }

    public void registerTenant(String internalPartnerId, Tenant tenant, UserApi userApi) {
        TenantUserApi tenantUserApi = tenantUserApiRepository.findFirstByTenantAndUserApi(tenant, userApi);
        if (tenantUserApi == null) {
            tenantUserApi = TenantUserApi.builder()
                    .id(new TenantUserApiKey(tenant.getId(), userApi.getId()))
                    .tenant(tenant)
                    .userApi(userApi)
                    .allInternalPartnerId(
                            internalPartnerId != null && !internalPartnerId.isEmpty() ?
                                    Collections.singletonList(internalPartnerId) :
                                    Collections.emptyList()
                    )
                    .build();
            tenantUserApiRepository.save(tenantUserApi);
        } else {
            if (internalPartnerId != null && !internalPartnerId.isEmpty()) {
                if (tenantUserApi.getAllInternalPartnerId() == null) {
                    tenantUserApi.setAllInternalPartnerId(Collections.singletonList(internalPartnerId));
                } else if (!tenantUserApi.getAllInternalPartnerId().contains(internalPartnerId)) {
                    tenantUserApi.getAllInternalPartnerId().add(internalPartnerId);
                }
                tenantUserApiRepository.save(tenantUserApi);
            }
        }

        if (userApi.getVersion() != null && userApi.getUrlCallback() != null && (
                tenant.getStatus() == TenantFileStatus.VALIDATED
                        || tenant.getStatus() == TenantFileStatus.TO_PROCESS)) {
            sendCallBack(
                    tenant,
                    userApi,
                    tenant.getStatus() == TenantFileStatus.VALIDATED ?
                            PartnerCallBackType.VERIFIED_ACCOUNT :
                            PartnerCallBackType.CREATED_ACCOUNT
            );
        }
    }

    public void sendCallBack(Tenant tenant, UserApi userApi, PartnerCallBackType partnerCallBackType) {
        ApartmentSharing apartmentSharing = tenant.getApartmentSharing();

        switch (userApi.getVersion()) {
            case 1: {
                String fullAccessUrl = (partnerCallBackType == PartnerCallBackType.VERIFIED_ACCOUNT) ? (callbackDomain + "/file/" + apartmentSharing.getToken()) : "";
                String publicAccessUrl = (partnerCallBackType == PartnerCallBackType.VERIFIED_ACCOUNT) ? (callbackDomain + "/public-file/" + apartmentSharing.getTokenPublic()) : "";

                LightAPIInfoModel lightAPIInfoModel = LightAPIInfoModel.builder()
                        .partnerCallBackType(partnerCallBackType)
                        .url(fullAccessUrl)
                        .publicUrl(publicAccessUrl)
                        .emails(new ArrayList<>())
                        .internalPartnersId(new ArrayList<>())
                        .tenantLightAPIInfos(new ArrayList<>())
                        .build();

                List<Tenant> tenantList = tenantRepository.findAllByApartmentSharing(apartmentSharing);
                for (Tenant t : tenantList) {
                    if (t.getEmail() != null && !t.getEmail().isEmpty()) {
                        lightAPIInfoModel.getEmails().add(t.getEmail());
                    }

                    TenantUserApi tenantUserApi = tenantUserApiRepository.findFirstByTenantAndUserApi(t, userApi);
                    if (tenantUserApi != null && tenantUserApi.getAllInternalPartnerId() != null && !tenantUserApi.getAllInternalPartnerId().isEmpty()) {
                        lightAPIInfoModel.getInternalPartnersId().addAll(tenantUserApi.getAllInternalPartnerId());
                    }

                    Map<String, String> hashMapFiles = new HashMap<>();

                    List<Document> tenantDocuments = t.getDocuments();
                    for (Document d : tenantDocuments) {
                        hashMapFiles.put("tenantFile" + auxiliarDocumentCategory(d.getDocumentCategory()), d.getName());
                    }

                    List<Guarantor> guarantors = t.getGuarantors();
                    boolean hasGuarantors = (guarantors != null && !guarantors.isEmpty());
                    if (hasGuarantors) {
                        for (Guarantor g : guarantors) {
                            List<Document> documents = g.getDocuments();
                            for (Document d : documents) {
                                String pathToDocument = d.getName();
                                hashMapFiles.put("guarantorFile" + auxiliarDocumentCategory(d.getDocumentCategory()), pathToDocument);
                            }
                        }
                    }

                    lightAPIInfoModel.getTenantLightAPIInfos().add(
                            TenantLightAPIInfoModel.builder()
                                    .email(t.getEmail())
                                    .salary(t.getTotalSalary())
                                    .tenantSituation(t.getTenantSituation().name())
                                    .guarantor(hasGuarantors)
                                    .listFiles(hashMapFiles)
                                    .allInternalPartnerId(tenantUserApi != null ? tenantUserApi.getAllInternalPartnerId() : Collections.emptyList())
                                    .build()
                    );
                }

                requestService.send(lightAPIInfoModel, userApi.getUrlCallback(), userApi.getPartnerApiKeyCallback());
                callbackLogService.createCallbackLogForInternalPartnerLight(tenant, userApi.getId(), tenant.getStatus(), lightAPIInfoModel);
                break;
            }
            case 2: {
                ApplicationModel applicationModel = applicationFullMapper.toApplicationModel(apartmentSharing);
                List<Tenant> tenantList = tenantRepository.findAllByApartmentSharing(apartmentSharing);
                for (Tenant t : tenantList) {

                    TenantUserApi tenantUserApi = tenantUserApiRepository.findFirstByTenantAndUserApi(t, userApi);
                    if (tenantUserApi != null) {
                        if (tenantUserApi.getAllInternalPartnerId() != null && !tenantUserApi.getAllInternalPartnerId().isEmpty()) {
                            applicationModel.getTenants().stream()
                                    .filter(tenantObject -> Objects.equals(tenantObject.getId(), t.getId()))
                                    .findFirst()
                                    .ifPresent(tenantModel -> tenantModel.setAllInternalPartnerId(tenantUserApi.getAllInternalPartnerId()));
                        }
                    }
                }
                applicationModel.setPartnerCallBackType(partnerCallBackType);
                requestService.send(applicationModel, userApi.getUrlCallback(), userApi.getPartnerApiKeyCallback());
                callbackLogService.createCallbackLogForPartnerModel(tenant, userApi.getId(), tenant.getStatus(), applicationModel);
                break;
            }
            default:
                log.error("send Callback failed");
                break;
        }
    }

    private int auxiliarDocumentCategory(DocumentCategory documentCategory) {
        switch (documentCategory) {
            case IDENTIFICATION:
                return 1;
            case RESIDENCY:
                return 2;
            case PROFESSIONAL:
                return 3;
            case FINANCIAL:
                return 4;
            case TAX:
                return 5;
            default:
                return 0;
        }
    }
}
