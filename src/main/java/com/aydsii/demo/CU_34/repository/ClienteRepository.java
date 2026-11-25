package com.aydsii.demo.CU_34.repository;

import com.aydsii.demo.CU_34.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de {@link Cliente}. Usa findById heredado de JpaRepository. */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
