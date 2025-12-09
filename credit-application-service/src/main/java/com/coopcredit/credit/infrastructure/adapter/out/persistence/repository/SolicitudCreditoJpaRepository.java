package com.coopcredit.credit.infrastructure.adapter.out.persistence.repository;

import com.coopcredit.credit.domain.model.EstadoSolicitud;
import com.coopcredit.credit.infrastructure.adapter.out.persistence.entity.SolicitudCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudCreditoJpaRepository extends JpaRepository<SolicitudCreditoEntity, Long> {

    @Query("SELECT s FROM SolicitudCreditoEntity s LEFT JOIN FETCH s.afiliado LEFT JOIN FETCH s.evaluacion WHERE s.afiliado.id = :afiliadoId")
    List<SolicitudCreditoEntity> findByAfiliadoId(@Param("afiliadoId") Long afiliadoId);

    @Query("SELECT s FROM SolicitudCreditoEntity s LEFT JOIN FETCH s.afiliado LEFT JOIN FETCH s.evaluacion WHERE s.estado = :estado")
    List<SolicitudCreditoEntity> findByEstado(@Param("estado") EstadoSolicitud estado);

    @Query("SELECT s FROM SolicitudCreditoEntity s LEFT JOIN FETCH s.afiliado LEFT JOIN FETCH s.evaluacion")
    List<SolicitudCreditoEntity> findAllWithDetails();
}
