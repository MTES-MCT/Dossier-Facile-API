package fr.dossierfacile.api.front.register.form;

import fr.dossierfacile.api.front.form.interfaces.FormWithTenantId;
import fr.dossierfacile.api.front.validator.anotation.Extension;
import fr.dossierfacile.api.front.validator.anotation.SizeFile;
import fr.dossierfacile.api.front.validator.group.ApiPartner;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@Extension
public abstract class DocumentForm implements FormWithTenantId {

    @NotNull(groups = ApiPartner.class)
    private Long tenantId;

    @SizeFile(max = 10)
    private List<MultipartFile> documents = new ArrayList<>();

}
