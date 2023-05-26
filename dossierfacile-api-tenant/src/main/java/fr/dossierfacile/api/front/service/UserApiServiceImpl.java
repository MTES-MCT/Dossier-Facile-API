package fr.dossierfacile.api.front.service;

import fr.dossierfacile.api.front.exception.UserApiNotFoundException;
import fr.dossierfacile.api.front.repository.UserApiRepository;
import fr.dossierfacile.api.front.service.interfaces.UserApiService;
import fr.dossierfacile.common.entity.Tenant;
import fr.dossierfacile.common.entity.TenantUserApi;
import fr.dossierfacile.common.entity.UserApi;
import fr.dossierfacile.common.repository.TenantUserApiRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserApiServiceImpl implements UserApiService {

    private final UserApiRepository userApiRepository;
    private final TenantUserApiRepository tenantUserApiRepository;

    @Override
    public UserApi findById(Long id) {
        return userApiRepository.findById(id)
                .orElseThrow(() -> new UserApiNotFoundException(id));
    }
    @Override
    public Optional<UserApi> findByName(String partner) {
        return userApiRepository.findByName(partner);
    }
    @Override
    public boolean anyTenantIsLinked(UserApi partner, List<Tenant> tenants) {
        Optional<TenantUserApi> result = tenantUserApiRepository.findFirstByUserApiAndTenantIn(partner, tenants);
        return result.isPresent();
    }

}
