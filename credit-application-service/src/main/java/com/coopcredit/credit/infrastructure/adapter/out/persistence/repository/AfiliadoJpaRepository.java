package com.coopcredit.credit.infrastructure.adapter.out.persistence.repository;

import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.AfiliadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AfiliadoJpaRepository extends JpaRepository<AfiliadoEntity, Long> {

    Optional<AfiliadoEntity> findByDocumento(String documento);

    boolean existsByDocumento(String documento);
}
