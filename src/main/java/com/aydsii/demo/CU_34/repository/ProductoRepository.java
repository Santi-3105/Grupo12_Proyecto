package com.aydsii.demo.CU_34.repository;

import com.aydsii.demo.CU_34.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/** Acceso a datos de {@link Producto}. Usa findById y save heredados. */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Productos que el cliente compró en alguno de los estados indicados y todavía no reseñó.
     * DISTINCT evita repetir productos adquiridos en más de un pedido.
     */
    @Query("""
            SELECT DISTINCT d.producto
            FROM Pedido p
            JOIN p.detalles d
            WHERE p.cliente.idCliente = :idCliente
              AND p.estado.nombre IN :estados
              AND NOT EXISTS (
                    SELECT r.idResena FROM Resena r
                    WHERE r.cliente.idCliente = :idCliente
                      AND r.producto = d.producto)
            """)
    List<Producto> findProductosParaResenar(@Param("idCliente") Long idCliente,
                                            @Param("estados") Collection<String> estados);
}
