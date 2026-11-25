package com.aydsii.demo.CU_34.exception;

import java.util.List;

/**
 * FE-03 de CU-04: "Datos de la reseña inválidos" (falta la calificación, está fuera de rango o el
 * comentario es demasiado largo - RN-04). El handler la traduce a HTTP 400.
 * Guarda la LISTA de errores para informar "qué datos son incorrectos o faltan" de una sola vez.
 */
public class DatosResenaInvalidosException extends RuntimeException {

    private final List<String> errores;

    public DatosResenaInvalidosException(List<String> errores) {
        // String.join une los elementos de la lista usando el separador indicado.
        super("Los datos de la reseña son inválidos: " + String.join("; ", errores));
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
