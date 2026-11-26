package com.aydsii.demo.CU_12.repository;

import com.aydsii.demo.CU_34.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de {@link Envio}. Usa save heredado. */
@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
}
