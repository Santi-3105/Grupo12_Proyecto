package com.aydsii.demo.CU_34.repository;

import com.aydsii.demo.CU_34.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de {@link Producto}. Usa findById y save heredados. */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
