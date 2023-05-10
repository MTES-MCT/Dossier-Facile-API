package fr.dossierfacile.common.repository;

import fr.dossierfacile.common.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedFileRepository extends JpaRepository<File, Long> {
}