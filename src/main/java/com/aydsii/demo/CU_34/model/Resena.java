package com.aydsii.demo.CU_34.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Reseña (calificación + comentario) que un cliente escribe sobre un producto que compró (CU-04).*/
// @Entity: le dice a JPA/Hibernate "esta clase es una tabla". Cada objeto Resena es una fila.
@Entity
// @Table: nombre real de la tabla. uniqueConstraints agrega una restricción UNIQUE (cliente, producto):
// la base de datos MISMA impide que un cliente tenga 2 reseñas del mismo producto (RN-03),
@Table(name = "resenas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resena_cliente_producto",
                columnNames = {"id_cliente", "id_producto"}))
public class Resena {

    // @Id = clave primaria. @GeneratedValue(IDENTITY) = la genera MySQL con AUTO_INCREMENT,
    // por eso nunca se asigna a mano: queda en null hasta que se hace save().
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long idResena;

    // @ManyToOne = "muchas reseñas pertenecen a UN cliente". Es la clave foránea (FK).
    // fetch = LAZY: el Cliente NO se carga de la base hasta que alguien lo use (ahorra consultas).
    // optional = false: una reseña no puede existir sin cliente.
    // @JoinColumn: nombre de la columna FK en la tabla "resenas".
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Idem anterior: "muchas reseñas pertenecen a UN producto".
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    // Integer (con mayúscula, objeto) y no int: así podemos distinguir "no se informó" (null) de un valor.
    @Column(nullable = false)
    private Integer calificacion;

    // El comentario es OPCIONAL (RN-04): por eso no tiene nullable = false.
    // length = 500 debe coincidir con ReglasResena.getLongitudMaximaComentario().
    @Column(length = 500)
    private String comentario;

    // Fecha y hora en que se publicó la reseña (RN-05).
    @Column(nullable = false)
    private LocalDateTime fecha;

    /**
     * Constructor vacío que EXIGE JPA para poder armar objetos al leer de la base.
     * Es protected para que el resto del código no lo use por error. ver ReglasResena (constructor privado, etc)
     */
    protected Resena() {
    }

    /** Diagrama dinámico paso 2.1.7: «create»(cliente, producto, calificacion, comentario, fecha). */
    public Resena(Cliente cliente, Producto producto, Integer calificacion,
                  String comentario, LocalDateTime fecha) {
        this.cliente = cliente;
        this.producto = producto;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public Long getIdResena() {
        return idResena;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Producto getProducto() {
        return producto;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
