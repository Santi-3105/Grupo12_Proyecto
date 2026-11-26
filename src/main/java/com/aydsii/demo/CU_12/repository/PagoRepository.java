package com.aydsii.demo.CU_12.repository;

import com.aydsii.demo.CU_34.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de {@link Pago}. Usa save heredado. */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
}
