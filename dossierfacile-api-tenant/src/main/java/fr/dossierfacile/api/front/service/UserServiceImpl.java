package fr.dossierfacile.api.front.service;

import fr.dossierfacile.api.front.exception.PasswordRecoveryTokenNotFoundException;
import fr.dossierfacile.api.front.exception.UserNotFoundException;
import fr.dossierfacile.api.front.mapper.TenantMapper;
import fr.dossierfacile.api.front.model.tenant.TenantModel;
import fr.dossierfacile.api.front.repository.PasswordRecoveryTokenRepository;
import fr.dossierfacile.api.front.repository.UserRepository;
import fr.dossierfacile.api.front.service.interfaces.ApartmentSharingService;
import fr.dossierfacile.api.front.service.interfaces.KeycloakService;
import fr.dossierfacile.api.front.service.interfaces.MailService;
import fr.dossierfacile.api.front.service.interfaces.PasswordRecoveryTokenService;
import fr.dossierfacile.api.front.service.interfaces.UserApiService;
import fr.dossierfacile.api.front.service.interfaces.UserService;
import fr.dossierfacile.common.entity.ApartmentSharing;
import fr.dossierfacile.common.entity.ConfirmationToken;
import fr.dossierfacile.common.entity.PasswordRecoveryToken;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.User;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.enums.ApplicationType;
import fr.dossierfacile.common.enums.LogType;
import fr.dossierfacile.common.enums.PartnerCallBackType;
import fr.dossierfacile.common.enums.TenantType;
import fr.dossierfacile.common.exceptions.ConfirmationTokenNotFoundException;
import fr.dossierfacile.common.model.WebhookDTO;
import fr.dossierfacile.common.repository.ConfirmationTokenRepository;
import fr.dossierfacile.common.repository.TenantCommonRepository;
import fr.dossierfacile.common.service.interfaces.LogService;
import fr.dossierfacile.common.service.interfaces.PartnerCallBackService;
import fr.dossierfacile.common.service.interfaces.TenantCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;
    private final MailService mailService;
    private final PasswordRecoveryTokenService passwordRecoveryTokenService;
    private final TenantMapper tenantMapper;
    private final TenantCommonRepository tenantRepository;
    private final LogService logService;
    private final KeycloakService keycloakService;
    private final UserApiService userApiService;
    private final PartnerCallBackService partnerCallBackService;
    private final ApartmentSharingService apartmentSharingService;
    private final TenantCommonService tenantCommonService;

    @Override
    @Transactional
    public long confirmAccount(String token) {
        ConfirmationToken confirmationToken = confirmationTokenRepository.findByToken(token).orElseThrow(() -> new ConfirmationTokenNotFoundException(token));
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        user.setConfirmationToken(null);
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
        confirmationTokenRepository.delete(confirmationToken);
        if (user.getKeycloakId() != null) {
            keycloakService.confirmKeycloakUser(user.getKeycloakId());
            // else we assume it's a partner account
        }
        tenantRepository.resetWarnings(user.getId());
        return user.getId();
    }

    @Override
    public TenantModel createPassword(User user, String password) {
        keycloakService.createKeyCloakPassword(user.getKeycloakId(), password);
        return tenantMapper.toTenantModel(tenantRepository.getById(user.getId()));
    }

    @Override
    public TenantModel createPassword(String token, String password) {
        PasswordRecoveryToken passwordRecoveryToken = passwordRecoveryTokenRepository.findByToken(token)
                .orElseThrow(() -> new PasswordRecoveryTokenNotFoundException(token));

        // check if keycloak is correctly synchronised
        User user = passwordRecoveryToken.getUser();
        var keycloakId = keycloakService.getKeycloakId(user.getEmail());
        if (!StringUtils.equals(keycloakId, user.getKeycloakId())){
            log.warn("Tenant keycloakId has been synchronized - user_id: " + user.getId());
            user.setKeycloakId(keycloakId);
            userRepository.save(user);
        }

        TenantModel tenantModel = createPassword(passwordRecoveryToken.getUser(), password);

        passwordRecoveryTokenRepository.delete(passwordRecoveryToken);
        return tenantModel;
    }

    @Override
    public void forgotPassword(String email) {
        Tenant tenant = tenantRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));

        PasswordRecoveryToken passwordRecoveryToken = passwordRecoveryTokenService.create(tenant);
        mailService.sendEmailNewPassword(tenant, passwordRecoveryToken);
    }

    @Override
    @Transactional
    public void deleteAccount(Tenant tenant) {
        List<WebhookDTO> webhookDTOList = new ArrayList<>();
        ApartmentSharing apartmentSharing = tenant.getApartmentSharing();
        apartmentSharing.groupingAllTenantUserApisInTheApartment().forEach((tenantUserApi) -> {
            UserApi userApi = tenantUserApi.getUserApi();
            webhookDTOList.add(partnerCallBackService.getWebhookDTO(tenant, userApi, PartnerCallBackType.DELETED_ACCOUNT));
        });
        saveAndDeleteInfoByTenant(tenant);
        logService.saveLog(LogType.ACCOUNT_DELETE, tenant.getId());
        if (tenant.getTenantType() == TenantType.CREATE) {
            keycloakService.deleteKeycloakUsers(tenant.getApartmentSharing().getTenants());
            apartmentSharingService.delete(tenant.getApartmentSharing());
        } else {
            keycloakService.deleteKeycloakUser(tenant);
            userRepository.delete(tenant);
            apartmentSharingService.removeTenant(tenant.getApartmentSharing(), tenant);
        }
        for (WebhookDTO webhookDTO : webhookDTOList) {
            partnerCallBackService.sendCallBack(tenant, webhookDTO);
        }
    }

    @Override
    @Transactional
    public Boolean deleteCoTenant(Tenant tenant, Long coTenantId) {
        if (tenant.getTenantType().equals(TenantType.CREATE)) {
            ApartmentSharing apartmentSharing = tenant.getApartmentSharing();
            Optional<Tenant> coTenant = apartmentSharing.getTenants().stream()
                    .filter(t -> t.getId().equals(coTenantId) && t.getTenantType().equals(TenantType.JOIN)).findFirst();
            if (coTenant.isPresent()) {
                deleteAccount(coTenant.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public void linkTenantToPartner(Tenant tenant, String partner, String internalPartnerId) {
        userApiService.findByName(partner)
                .ifPresent(userApi -> {
                    if (tenant.getApartmentSharing().getApplicationType() == ApplicationType.COUPLE) {
                        tenant.getApartmentSharing().getTenants()
                                .stream()
                                .forEach(t -> partnerCallBackService.registerTenant(
                                        (tenant.getId() == t.getId()) ? internalPartnerId : null, t, userApi));
                    } else {
                        partnerCallBackService.registerTenant(internalPartnerId, tenant, userApi);
                    }
                });
    }

    @Override
    public void logout(String keycloakId) {
        keycloakService.logout(keycloakId);
    }

    @Override
    public void unlinkFranceConnect(Tenant tenant) {
        User user = userRepository.findById(tenant.getId()).orElseThrow(IllegalArgumentException::new);
        user.setFranceConnect(false);
        userRepository.save(tenant);
        keycloakService.unlinkFranceConnect(tenant);
    }

    private void saveAndDeleteInfoByTenant(Tenant tenant) {
        mailService.sendEmailAccountDeleted(tenant);
        tenantCommonService.recordAndDeleteTenantData(tenant.getId());
    }

}
