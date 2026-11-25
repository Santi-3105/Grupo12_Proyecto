package com.aydsii.demo.CU_34.repository;

import com.aydsii.demo.CU_34.model.Cliente;
import com.aydsii.demo.CU_34.model.Producto;
import com.aydsii.demo.CU_34.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Acceso a datos de {@link Resena} (CU-04).
 * Al extender JpaRepository ya viene con {@code save(resena)} (paso 2.1.8 del diagrama).
 */
@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    /**
     * Paso 2.1.6: ¿este cliente ya reseñó este producto? (RN-03).
     * Sin SQL: "existsBy" + "Cliente" + "And" + "Producto" => SELECT COUNT(*) > 0 FROM resenas WHERE id_cliente = ? AND id_producto = ?
     */
    //Basicamente utilizamos una query como funcion, es decir el exist nos indica que exista un cliente y el and anida con el producto
    //Es lo mismo que hacer la query
    boolean existsByClienteAndProducto(Cliente cliente, Producto producto);
}
