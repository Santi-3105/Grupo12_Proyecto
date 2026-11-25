package com.aydsii.demo.CU_12.repository;

import com.aydsii.demo.CU_12.model.Carrito;
import com.aydsii.demo.CU_12.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Acceso a datos de {@link Carrito}. */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    /**
     * Busca el carrito de un cliente según su estado (ej: el carrito ACTIVO).
     * Spring Data genera la consulta a partir del nombre del método:
     * "cliente.idCliente = ? AND estado = ?".
     */
    Optional<Carrito> findByCliente_IdClienteAndEstado(Long idCliente, EstadoCarrito estado);
}
