package fr.dossierfacile.common.entity;

import fr.dossierfacile.common.enums.DocumentCategory;
import fr.dossierfacile.common.enums.TenantFileStatus;
import fr.dossierfacile.common.enums.TenantType;
import fr.dossierfacile.common.enums.TypeGuarantor;
import fr.dossierfacile.common.enums.UserType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.dossierfacile.common.enums.DocumentStatus.DECLINED;
import static fr.dossierfacile.common.enums.DocumentStatus.TO_PROCESS;

@Entity
@Table(name = "tenant")
@DiscriminatorValue("TENANT")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(builderMethodName = "lombokBuilder")
@Slf4j
public class Tenant extends User implements Person, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("id")
    private List<Guarantor> guarantors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_sharing_id")
    private ApartmentSharing apartmentSharing;

    private String tenantFirstName;
    private String tenantLastName;
    private String tenantPreferredName;

    @Column
    private Integer satisfactionSurvey;

    @Column
    @Enumerated(EnumType.STRING)
    private TenantType tenantType;

    @Builder.Default
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<TenantUserApi> tenantsUserApi = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Document> documents = new ArrayList<>();

    private String zipCode;

    private Boolean abroad;

    @Builder.Default
    private Boolean honorDeclaration = Boolean.FALSE;

    @Builder.Default
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(length = 2000)
    @Size(max = 2000)
    private String clarification;

    @Column
    @Enumerated(EnumType.STRING)
    private TenantFileStatus status;

    @Column(name = "operator_date_time")
    private LocalDateTime operatorDateTime;

    private int warnings;

    private String operatorComment;

    private transient String warningMessage;

    public static TenantBuilder<?, ?> builder() {
        TenantBuilder<?, ?> tenantBuilder = Tenant.lombokBuilder();
        tenantBuilder.userType(UserType.TENANT);
        return tenantBuilder;
    }

    public TenantFileStatus getStatus() {
        if (status == null) { // For tenants created before the inclusion of field "status"
            status = computeStatus();
        }
        return status;
    }

    public TenantFileStatus computeStatus() {
        log.info("Computing status for tenant with ID [" + getId() + "]...");

        if (status == TenantFileStatus.ARCHIVED) {
            return TenantFileStatus.ARCHIVED;
        }

        // Gets all tenant documents
        List<Document> allDocuments = (guarantors == null) ?
                documents :
                Stream.concat(documents.stream(),
                                guarantors.stream()
                                        .map(Guarantor::getDocuments)
                                        .flatMap(List::stream))
                        .toList();
        // Check documents status
        if (allDocuments != null && allDocuments.stream().anyMatch(d -> d.getDocumentStatus() == DECLINED)) {
            return TenantFileStatus.DECLINED;
        } else if (!honorDeclaration || !isAllCategories()) {
            return TenantFileStatus.INCOMPLETE;
        } else if (allDocuments.stream().anyMatch(d -> d.getDocumentStatus() == TO_PROCESS)) {
            return TenantFileStatus.TO_PROCESS;
        }

        return TenantFileStatus.VALIDATED;
    }

    public boolean isValidated() {
        return getStatus() == TenantFileStatus.VALIDATED;
    }

    public boolean isAllCategories() {
        // TENANT CHECK

        //I setup the list of mandatory categories
        List<DocumentCategory> tenantOrNaturalPersonGuarantorMandatoryCategories = new ArrayList<>();
        tenantOrNaturalPersonGuarantorMandatoryCategories.add(DocumentCategory.IDENTIFICATION);
        tenantOrNaturalPersonGuarantorMandatoryCategories.add(DocumentCategory.RESIDENCY);
        tenantOrNaturalPersonGuarantorMandatoryCategories.add(DocumentCategory.PROFESSIONAL);
        tenantOrNaturalPersonGuarantorMandatoryCategories.add(DocumentCategory.FINANCIAL);
        tenantOrNaturalPersonGuarantorMandatoryCategories.add(DocumentCategory.TAX);

        //I setup the list of currently existing categories on the tenant
        List<DocumentCategory> tenantCategories = new ArrayList<>();
        List<Document> documentList = getDocuments();
        if (documentList == null) {
            return false;
        }
        for (Document document : getDocuments()) {
            tenantCategories.add(document.getDocumentCategory());
        }

        // I check that all the mandatory categories are present in the list of existing categories
        for (DocumentCategory documentCategory : tenantOrNaturalPersonGuarantorMandatoryCategories) {
            if (!tenantCategories.contains(documentCategory)) {
                return false;
            }
        }

        //GUARANTOR CHECK
        if (!guarantors.isEmpty()) {
            for (Guarantor guarantor : getGuarantors()) {
                if (guarantor.getDocuments() == null || guarantor.getDocuments().isEmpty()) {
                    return false;
                } else if (guarantor.getTypeGuarantor() == TypeGuarantor.ORGANISM) {
                    // Must have exactly one Document of type GUARANTEE_PROVIDER_CERTIFICATE
                    if (guarantor.getDocuments().size() != 1 || !guarantor.getDocuments().get(0).getDocumentCategory().equals(DocumentCategory.GUARANTEE_PROVIDER_CERTIFICATE)) {
                        return false;
                    }
                } else if (guarantor.getTypeGuarantor() == TypeGuarantor.NATURAL_PERSON) {
                    //I setup the list of currently existing categories on the guarantor
                    List<DocumentCategory> guarantorCategories = new ArrayList<>();
                    for (Document guarantorDocument : guarantor.getDocuments()) {
                        guarantorCategories.add(guarantorDocument.getDocumentCategory());
                    }

                    // I check that all the mandatory categories are present in the list of existing categories (same as tenant list for a natural guarantor)
                    for (DocumentCategory documentCategory : tenantOrNaturalPersonGuarantorMandatoryCategories) {
                        if (!guarantorCategories.contains(documentCategory)) {
                            return false;
                        }
                    }
                } else if (guarantor.getTypeGuarantor() == TypeGuarantor.LEGAL_PERSON) {
                    //I setup the list of mandatory categories for a legal person
                    List<DocumentCategory> legalPersonGuarantorMandatoryCategories = new ArrayList<>();
                    legalPersonGuarantorMandatoryCategories.add(DocumentCategory.IDENTIFICATION);
                    legalPersonGuarantorMandatoryCategories.add(DocumentCategory.IDENTIFICATION_LEGAL_PERSON);

                    //I setup the list of currently existing categories on the guarantor
                    List<DocumentCategory> guarantorCategories = new ArrayList<>();
                    for (Document guarantorDocument : guarantor.getDocuments()) {
                        guarantorCategories.add(guarantorDocument.getDocumentCategory());
                    }

                    // I check that all the mandatory categories are present in the list of existing categories
                    for (DocumentCategory documentCategory : legalPersonGuarantorMandatoryCategories) {
                        if (!guarantorCategories.contains(documentCategory)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void lastUpdateDateProfile(LocalDateTime localDateTime, DocumentCategory documentCategory) {
        this.lastUpdateDate = localDateTime == null ? LocalDateTime.now() : localDateTime;
        if (documentCategory != null) {
            log.info("Updating document {} of the tenant", documentCategory);
        } else {
            log.info("Updating tenant profile on {}", this.lastUpdateDate);
        }
    }

    public int getTotalSalary() {
        return documents.stream().filter(d -> d.getDocumentCategory() == DocumentCategory.FINANCIAL).map(Document::getMonthlySum)
                .filter(Objects::nonNull).reduce(0, Integer::sum);
    }

    public int getGuarantorsTotalSalary() {
        return guarantors.stream().map(Guarantor::getTotalSalary).reduce(0, Integer::sum);
    }

    /**
     * Tenant has been created by partner and has never been loggued in DF Website
     */
    public boolean isBelongToPartner() {
        return this.getKeycloakId() == null && this.getTenantsUserApi().size() == 1;
    }

    @Override
    public String getFirstName() {
        return tenantFirstName != null ? tenantFirstName : super.getFirstName();
    }

    @Override
    public String getLastName() {
        return tenantLastName != null ? tenantLastName : super.getLastName();
    }

    @Override
    public String getPreferredName() {
        return tenantPreferredName != null ? tenantPreferredName : super.getPreferredName();
    }

    @Override
    public void setFirstName(String firstName) {
        super.setFirstName(firstName);
        this.tenantFirstName = firstName;
    }

    @Override
    public void setLastName(String lastName) {
        super.setLastName(lastName);
        this.tenantLastName = lastName;
    }

    @Override
    public void setPreferredName(String preferredName) {
        super.setPreferredName(preferredName);
        this.tenantPreferredName = preferredName;
    }

}
