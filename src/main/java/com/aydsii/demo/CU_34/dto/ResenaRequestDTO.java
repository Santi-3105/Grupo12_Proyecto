package com.aydsii.demo.CU_34.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos que envía el cliente para registrar una reseña (CU-04, pasos 4 a 6).
 * <p>
 * Los campos son objetos (Integer/Long) y no primitivos (int/long) a propósito: si el cliente NO envía la
 * calificación, llega null y el service puede avisar "falta la calificación" (FE-03). Con un int
 * llegaría un 0 silencioso. La validación la hace ResenaService (paso 2.1.3 del diagrama) consultando
 * al Singleton ReglasResena.
 * {@code record} = clase inmutable de datos. Java genera solo el constructor, los accesores
 * (se llaman como el campo, SIN "get": {@code dto.idPedido()}), equals, hashCode y toString.
 * {@code @Schema} solo documenta el campo en Swagger UI; no cambia el comportamiento.
 */
public record ResenaRequestDTO(
        @Schema(description = "Cliente que escribe la reseña", example = "1") Long idCliente,
        @Schema(description = "Producto que se reseña", example = "3") Long idProducto,
        @Schema(description = "Calificación de 1 a 5", example = "5") Integer calificacion,
        @Schema(description = "Comentario opcional (máximo 500 caracteres)", example = "Muy buen producto") String comentario) {
}
