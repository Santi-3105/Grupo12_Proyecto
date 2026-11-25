package com.aydsii.demo.CU_34.exception;

/**
 * FE-03 de CU-03: "Error al obtener la información" (no se pudieron recuperar las compras o el detalle).
 * El handler la traduce a HTTP 503. Recibe la excepción original (causa) para no perder el motivo técnico:
 * el handler la escribe en el log junto con el stack trace.
 */
public class ErrorObtencionException extends RuntimeException {

    public ErrorObtencionException(Throwable causa) {
        // super(mensaje, causa): el mensaje es para el cliente; la causa queda "encadenada" para el log.
        super("No se pudo obtener la información en este momento. Reintente más tarde", causa);
    }
}
