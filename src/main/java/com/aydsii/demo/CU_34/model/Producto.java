package com.aydsii.demo.CU_34.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Producto del catálogo.
 */
@Entity
@Table(name = "productos")
//lombok, lo mismo, resuelve codigo repetido
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 80)
    private String categoria;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    /** true = disponible para la venta, false = discontinuado. */
    @Column(nullable = false)
    private Boolean estado = true;

    /**
     * Control de concurrencia: si dos compras intentan descontar el stock
     * del mismo producto a la vez, la segunda falla (HTTP 409) en lugar de pisar el valor.
     */
    @Version
    private Long version;

    public Producto(String nombre, String categoria, String descripcion,
                    BigDecimal precio, Integer stock, Boolean estado) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
    }
}
