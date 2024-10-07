package fr.dossierfacile.api.front.model.dfc.apartment_sharing;

import com.fasterxml.jackson.annotation.JsonInclude;
import fr.dossierfacile.api.front.model.tenant.DocumentModel;
import fr.dossierfacile.common.enums.TypeGuarantor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuarantorModel {
    private Long id;
    private String firstName;
    private String lastName;
    private String legalPersonName;
    private TypeGuarantor typeGuarantor;
    private List<DocumentModel> documents;
}
