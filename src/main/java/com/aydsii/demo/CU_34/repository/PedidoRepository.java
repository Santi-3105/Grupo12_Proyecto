package com.aydsii.demo.CU_34.repository;

import com.aydsii.demo.CU_34.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de {@link Pedido}.
 */
//Indica que es de tipo repositorio
@Repository
//Al extender de JpaRepository nos da metodos como save, findById, findAll, delete, sin tener que escribirlos
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Pedidos de un cliente, del más nuevo al más viejo (paso 3 del diagrama de listado).
     * {@code @EntityGraph} trae las relaciones necesarias en una sola consulta para los casos
     * de uso que comparten este repositorio.
     */
    @EntityGraph(attributePaths = "estado")
    List<Pedido> findByCliente_IdClienteOrderByFechaDesc(Long idCliente);

    /**
     * Busca un pedido por id SOLO si pertenece al cliente indicado (paso 16 del diagrama).
     * Si el pedido no existe o es de otro cliente devuelve Optional vacío (justamente la funcion del optional, puede estar vacio como no),
     * y desde afuera ambos casos se ven igual (404).
     */
    @EntityGraph(attributePaths = {"estado", "detalles", "detalles.producto"})
    Optional<Pedido> findByIdPedidoAndCliente_IdCliente(Long idPedido, Long idCliente);

    /**
     * Comprueba si el cliente compró el producto en alguno de los estados indicados.
     * Los estados concretos los define CU_34 y se reciben como parámetro.
     */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Pedido p
            JOIN p.detalles d
            WHERE p.cliente.idCliente = :idCliente
              AND d.producto.idProducto = :idProducto
              AND p.estado.nombre IN :estados
            """)
    boolean existsCompraConcretada(@Param("idCliente") Long idCliente,
                                   @Param("idProducto") Long idProducto,
                                   @Param("estados") Collection<String> estados);
}
