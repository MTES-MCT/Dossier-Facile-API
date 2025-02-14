package fr.gouv.bo.repository;

import fr.dossierfacile.common.entity.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @Query("""
            SELECT o FROM Owner o
            WHERE ((:email = '' AND o.email IS NULL) OR LOWER(o.email) LIKE %:email%)
            AND ((:firstName = '' AND o.firstName IS NULL) OR LOWER(o.firstName) LIKE %:firstName%)
            AND ((:lastName = '' AND o.lastName IS NULL) OR LOWER(o.lastName) LIKE %:lastName%)
            """)
    Page<Owner> searchOwners(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            PageRequest page);
}
