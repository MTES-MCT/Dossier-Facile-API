package fr.dossierfacile.api.front.controller;

import fr.dossierfacile.api.front.form.ShareFileByMailForm;
import fr.dossierfacile.api.front.mapper.PropertyOMapper;
import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.property.PropertyOModel;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.register.form.tenant.UrlForm;
import fr.dossierfacile.api.front.security.interfaces.AuthenticationFacade;
import fr.dossierfacile.api.front.service.interfaces.PropertyService;
import fr.dossierfacile.api.front.service.interfaces.TenantService;
import fr.dossierfacile.api.front.service.interfaces.UserService;
import fr.dossierfacile.common.converter.AcquisitionData;
import fr.dossierfacile.common.entity.Property;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.service.interfaces.ProcessingCapacityService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/tenant")
@Slf4j
public class TenantController {

    private final AuthenticationFacade authenticationFacade;
    private final TenantService tenantService;
    private final ProcessingCapacityService processingCapacityService;
    private final TenantMapper tenantMapper;
    private final PropertyService propertyService;
    private final PropertyOMapper propertyMapper;
    private final UserService userService;

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TenantModel> profile(@RequestParam MultiValueMap<String, String> params) {
        Tenant tenant = authenticationFacade.getLoggedTenant(AcquisitionData.from(params));
        tenantService.updateLastLoginDateAndResetWarnings(tenant);
        return ok(tenantMapper.toTenantModel(tenant));
    }

    @GetMapping(value = "/property/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyOModel> getInfoOfPropertyAndOwner(@PathVariable("token") String propertyToken) {
        Property property = propertyService.getPropertyByToken(propertyToken);
        return ok(propertyMapper.toPropertyModel(property));
    }

    @DeleteMapping("/deleteCoTenant/{id}")
    public ResponseEntity<Void> deleteCoTenant(@PathVariable Long id) {
        Tenant tenant = authenticationFacade.getLoggedTenant();
        return (userService.deleteCoTenant(tenant, id) ? ok() : status(HttpStatus.FORBIDDEN)).build();
    }

    @PostMapping(value = "/linkFranceConnect", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> linkFranceConnect(@RequestBody UrlForm urlDTO) {
        String currentUrl = urlDTO.getUrl();
        if (currentUrl == null) {
            return badRequest().build();
        }
        String link = authenticationFacade.getFranceConnectLink(currentUrl);
        return ok(link);
    }

    @PostMapping(value = "/sendFileByMail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendFileByMail(@RequestBody ShareFileByMailForm shareFileByMailForm) {
        Tenant tenant = authenticationFacade.getLoggedTenant();
        try {
            tenantService.sendFileByMail(tenant, shareFileByMailForm.getEmail(), shareFileByMailForm.getShareType());
        } catch (Exception e) {
            return badRequest().build();
        }
        return ok("");
    }

    @PreAuthorize("hasPermissionOnTenant(#tenantId)")
    @GetMapping(value = "/{id}/expectedProcessingTime", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LocalDateTime> expectedProcessingTime(@PathVariable("id") Long tenantId) {
        LocalDateTime expectedProcessingTime = processingCapacityService.getExpectedProcessingTime(tenantId);
        return ok(expectedProcessingTime);
    }

    @GetMapping("/doNotArchive/{token}")
    public ResponseEntity<Void> doNotArchive(@PathVariable String token) {
        tenantService.doNotArchive(token);
        return ok().build();
    }
}
