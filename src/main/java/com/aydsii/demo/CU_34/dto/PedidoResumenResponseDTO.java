package com.aydsii.demo.CU_34.dto;

import com.aydsii.demo.CU_34.model.ModalidadCompra;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una fila del listado del historial de compras (CU-03, paso 4): número de compra, fecha,
 * modalidad (web o presencial) y monto total.
 * <p>
 * {@code record} = clase inmutable de datos. Java genera solo el constructor, los accesores
 * (se llaman como el campo, SIN "get": {@code dto.idPedido()}), equals, hashCode y toString.
 * {@code @Schema} solo documenta el campo en Swagger UI; no cambia el comportamiento.
 */
public record PedidoResumenResponseDTO(
        @Schema(description = "Número de compra", example = "12") Long idPedido,
        @Schema(description = "Fecha y hora de la compra") LocalDateTime fecha,
        @Schema(description = "Modalidad de la compra: WEB o PRESENCIAL", example = "WEB") ModalidadCompra modalidadCompra,
        @Schema(description = "Monto total de la compra", example = "25400.00") BigDecimal total) {
}
