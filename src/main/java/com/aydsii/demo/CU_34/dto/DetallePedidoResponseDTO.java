package com.aydsii.demo.CU_34.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Un producto dentro del detalle de una compra (CU-03, paso 6): producto adquirido, cantidad,
 * precio unitario y subtotal. Es distinto del DetallePedidoDTO de CU_12: este sigue el diagrama del CU-03.
 * record -> es un tipo especial de clase diseñado para representar datos inmutables de forma concisa, es decir construye automaticamente siguiendo el esquema de la bdd
 */
public record DetallePedidoResponseDTO(
        @Schema(description = "Id del producto", example = "3") Long idProducto,
        @Schema(description = "Nombre del producto", example = "Mouse inalámbrico") String nombreProducto,
        @Schema(description = "Cantidad comprada", example = "2") Integer cantidad,
        @Schema(description = "Precio unitario al momento de la compra", example = "4500.00") BigDecimal precioUnitario,
        @Schema(description = "cantidad x precioUnitario", example = "9000.00") BigDecimal subtotal) {
}
