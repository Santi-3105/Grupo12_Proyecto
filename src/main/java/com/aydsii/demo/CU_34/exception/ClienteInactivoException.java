package com.aydsii.demo.CU_34.exception;

/**
 * FE-02 de CU-03 y CU-04: "Cuenta de cliente desactivada" (RN-01). Siguiendo el diagrama
 * ("cliente inexistente o inactivo") se usa en los dos casos. El handler la traduce a HTTP 403.
 * {@code extends RuntimeException}: excepción "unchecked": no obliga a declarar {@code throws} en cada
 * método, y hace que {@code @Transactional} deshaga (rollback) lo hecho.
 */
public class ClienteInactivoException extends RuntimeException {

    public ClienteInactivoException(Long idCliente) {
        super("El cliente " + idCliente + " no existe o su cuenta se encuentra desactivada, "
                + "por lo que no puede realizar esta operación");
    }
}
