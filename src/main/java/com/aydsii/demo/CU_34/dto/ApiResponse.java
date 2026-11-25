package com.aydsii.demo.CU_34.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Envoltorio ÚNICO para TODAS las respuestas de la API, tanto de éxito como de error.
 * Toda respuesta HTTP tiene el mismo formato JSON:
 * { "status": 200, "message": "Pedidos obtenidos correctamente", "data": [ ... ] }
 * { "status": 403, "message": "La cuenta se encuentra desactivada", "data": null }
 * {@code ApiResponse<T>}: la {@code <T>} es un TIPO GENÉRICO: un "casillero" cuyo tipo se completa al usar la clase.
 * {@code ApiResponse<PedidoResponseDTO>} es una respuesta cuyo "data" es un PedidoResponseDTO;
 * {@code ApiResponse<List<ResenaResponseDTO>>} es una respuesta cuyo "data" es una lista. Una sola clase sirve para todo.
 * Se crea con los métodos {@link #success} y {@link #error} (métodos "de fábrica"), no con {@code new}:
 * el constructor es privado, así nadie arma una respuesta con un formato distinto.
 */
// @Schema solo documenta en Swagger UI; no cambia el comportamiento.
@Schema(description = "Formato estándar de respuesta de la API")
public class ApiResponse<T> {

    // "final": se asignan una sola vez, en el constructor. Una respuesta ya armada no se modifica.
    @Schema(description = "Código de estado HTTP", example = "200")
    private final int status;

    @Schema(description = "Mensaje descriptivo de lo que pasó", example = "Operación realizada con éxito")
    private final String message;

    @Schema(description = "Contenido de la respuesta: un objeto, una lista, o null en los errores")
    private final T data;

    /** Constructor PRIVADO: solo se puede usar desde esta misma clase (por success y error). */
    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * Respuesta de ÉXITO.
     * <p>
     * Es {@code static}: se llama sobre la clase ({@code ApiResponse.success(...)}), sin crear un objeto antes.
     *
     * @param status  código HTTP como número (200, 201...). Se obtiene con HttpStatus.OK.value()
     * @param message texto descriptivo para el cliente
     * @param data    el contenido real de la respuesta
     */
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    /**
     * Respuesta de ERROR sin datos: "data" queda en null (es lo que usan los handlers de excepciones).
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    /**
     * Respuesta de ERROR con detalle en "data" (por ejemplo, el mapa campo -> motivo de una validación fallida).
     * Es una "sobrecarga": mismo nombre que el anterior pero con otro parámetro; Java elige cuál usar
     * según los argumentos que le pases. Esto nos sirve para explicar mas que nada cuando se listan los productos a reseñar, cuando directamente no hay nada usa este metodo
     */
    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }
    
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
