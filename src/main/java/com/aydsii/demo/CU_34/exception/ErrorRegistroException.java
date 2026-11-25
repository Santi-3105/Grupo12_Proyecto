package com.aydsii.demo.CU_34.exception;

/** FE-04 de CU-04: "Error al registrar la reseña". El handler la traduce a HTTP 503. */
public class ErrorRegistroException extends RuntimeException {

    public ErrorRegistroException(Throwable causa) {
        super("No se pudo registrar la reseña en este momento. Reintente más tarde", causa);
    }
}
