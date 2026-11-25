package com.aydsii.demo.CU_34.exception;

/** Se lanza cuando un producto no esta disponible o no alcanza el stock. Responde HTTP 409. */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
