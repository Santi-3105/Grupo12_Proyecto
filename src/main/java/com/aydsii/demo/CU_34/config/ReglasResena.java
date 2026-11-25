package com.aydsii.demo.CU_34.config;

import java.util.List;

/**
 * PATRÓN DE DISEÑO: SINGLETON.
 * ¿QUÉ PROBLEMA RESUELVE?
 * Hay datos que tienen que ser UNO SOLO para todo el sistema. Acá, las reglas de negocio de las
 * reseñas (RN-04 del CU-04: rango de la calificación y longitud máxima del comentario) y la lista
 * de estados que significan "compra concretada" (RN-02). Si cada clase tuviera su propia copia
 * ("new ReglasResena()" en varios lugares), podrían quedar valores distintos en distintas partes del
 * sistema. Con Singleton garantizamos que existe UNA sola instancia y que todos la comparten.
 * <p>
 * LAS 3 REGLAS DEL SINGLETON (están marcadas con (1), (2) y (3) en el código):
 * <ol>
 *   <li>Un atributo {@code static} que guarda LA instancia única.</li>
 *   <li>Un constructor {@code private}: nadie de afuera puede hacer "new ReglasResena()".</li>
 *   <li>Un método {@code static} público (getInstancia) que entrega siempre esa misma instancia.</li>
 * </ol>
 * {@code final} en la clase: nadie puede heredar de ella y saltearse el constructor privado.
 */
public final class ReglasResena {

    // (1) LA instancia única. "static" = pertenece a la clase (no a un objeto), "final" = no se reasigna.
    // Se crea una sola vez
    private static final ReglasResena INSTANCIA = new ReglasResena();

    // Estado compartido. Son "final": una vez creada la instancia, las reglas no cambian.
    private final int calificacionMinima;
    private final int calificacionMaxima;
    private final int longitudMaximaComentario;
    private final List<String> estadosCompraConcretada;

    // (2) Constructor PRIVADO: solo esta clase puede ejecutarlo (y lo ejecuta una única vez, arriba).
    private ReglasResena() {
        this.calificacionMinima = 1;            // RN-04: "por ejemplo, de 1 a 5"
        this.calificacionMaxima = 5;
        this.longitudMaximaComentario = 500;    // RN-04: longitud máxima del comentario
        // RN-02: nombres de estado (tabla estados_pedido) que equivalen a "entregado o retirado".
        // List.of(...) crea una lista INMODIFICABLE: nadie puede agregarle ni sacarle elementos.
        this.estadosCompraConcretada = List.of("ENTREGADO", "RETIRADO");
    }

    // (3) Punto de acceso GLOBAL: siempre devuelve la misma instancia.
    public static ReglasResena getInstancia() {
        return INSTANCIA;
    }

    // ---------------------------------------------------------------------
    //  Comportamiento: las reglas "saben" validarse (el service solo les pregunta)
    // ---------------------------------------------------------------------

    /** RN-04: ¿la calificación está dentro del rango permitido? (null = no se ingresó = inválida). */
    public boolean esCalificacionValida(Integer calificacion) {
        return calificacion != null
                && calificacion >= calificacionMinima
                && calificacion <= calificacionMaxima;
    }

    /** RN-04: el comentario es opcional (null vale); si viene, no puede superar la longitud máxima. */
    public boolean esComentarioValido(String comentario) {
        return comentario == null || comentario.length() <= longitudMaximaComentario;
    }

    public int getCalificacionMinima() {
        return calificacionMinima;
    }

    public int getCalificacionMaxima() {
        return calificacionMaxima;
    }

    public int getLongitudMaximaComentario() {
        return longitudMaximaComentario;
    }

    /** Estados que cuentan como compra concretada. Lista inmodificable. */
    public List<String> getEstadosCompraConcretada() {
        return estadosCompraConcretada;
    }
}
