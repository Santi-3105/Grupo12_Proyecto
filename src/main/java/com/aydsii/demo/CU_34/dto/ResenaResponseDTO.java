package com.aydsii.demo.CU_34.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Reseña ya registrada (CU-04, paso 9). Incluye el id generado por la base y la fecha de publicación. 
 * {@code record} = clase inmutable de datos. Java genera solo el constructor, los accesores
 * (se llaman como el campo, SIN "get": {@code dto.idPedido()}), equals, hashCode y toString.
 * {@code @Schema} solo documenta el campo en Swagger UI; no cambia el comportamiento.
*/
public record ResenaResponseDTO(
        @Schema(description = "Id de la reseña generado por la base", example = "1") Long idResena,
        @Schema(description = "Cliente que la escribió", example = "1") Long idCliente,
        @Schema(description = "Producto reseñado", example = "3") Long idProducto,
        @Schema(description = "Calificación de 1 a 5", example = "5") Integer calificacion,
        @Schema(description = "Comentario (puede ser null)", example = "Muy buen producto") String comentario,
        @Schema(description = "Fecha y hora de publicación") LocalDateTime fecha) {
}
