package fr.gouv.bo.service;

import fr.dossierfacile.common.entity.Owner;
import fr.gouv.bo.repository.OwnerRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerService {
    private final OwnerRepository ownerRepository;

    public Page<Owner> searchOwners(@NotNull String email, @NotNull String firstName, @NotNull String lastName, PageRequest page) {
        return ownerRepository.searchOwners(email.toLowerCase(), firstName.toLowerCase(), lastName.toLowerCase(), page);
    }

    public Optional<Owner> findById(Long id) {
        return ownerRepository.findById(id);
    }
}