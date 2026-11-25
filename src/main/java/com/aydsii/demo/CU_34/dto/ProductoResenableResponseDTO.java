package com.aydsii.demo.CU_34.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Producto que el cliente compró (compra concretada) y todavía no reseñó (CU-04, paso 3).
 * {@code record} = clase inmutable de datos. Java genera solo el constructor, los accesores
 * (se llaman como el campo, SIN "get": {@code dto.idPedido()}), equals, hashCode y toString.
 * {@code @Schema} solo documenta el campo en Swagger UI; no cambia el comportamiento.
 */
public record ProductoResenableResponseDTO(
        @Schema(description = "Id del producto", example = "3") Long idProducto,
        @Schema(description = "Nombre del producto", example = "Mouse inalámbrico") String nombre,
        @Schema(description = "Categoría del producto", example = "Periféricos") String categoria) {
}
