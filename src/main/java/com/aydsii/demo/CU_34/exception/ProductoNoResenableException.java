package com.aydsii.demo.CU_34.exception;

/**
 * El producto no se puede reseñar: la compra no fue concretada (RN-02) o el cliente ya lo reseñó (RN-03).
 * El handler la traduce a HTTP 409 (conflicto con el estado actual).
 */
public class ProductoNoResenableException extends RuntimeException {

    public ProductoNoResenableException(String motivo) {
        super(motivo);
    }
}
