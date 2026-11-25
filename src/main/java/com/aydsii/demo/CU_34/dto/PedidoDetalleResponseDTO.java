package com.aydsii.demo.CU_34.dto;

import com.aydsii.demo.CU_34.model.ModalidadCompra;
import com.aydsii.demo.CU_34.model.TipoEntrega;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de UNA compra (CU-03, paso 6): datos generales, forma de entrega (envío o retiro)
 * y la lista de productos adquiridos.
 * {@code record} = clase inmutable de datos. Java genera solo el constructor, los accesores
 * (se llaman como el campo, SIN "get": {@code dto.idPedido()}), equals, hashCode y toString.
 * {@code @Schema} solo documenta el campo en Swagger UI; no cambia el comportamiento.
 */
public record PedidoDetalleResponseDTO(
        @Schema(description = "Número de compra", example = "12") Long idPedido,
        @Schema(description = "Fecha y hora de la compra") LocalDateTime fecha,
        @Schema(description = "WEB o PRESENCIAL", example = "WEB") ModalidadCompra modalidadCompra,
        @Schema(description = "Forma de entrega: ENVIO o RETIRO", example = "ENVIO") TipoEntrega tipoEntrega,
        @Schema(description = "Monto total de la compra", example = "25400.00") BigDecimal total,
        @Schema(description = "Productos adquiridos") List<DetallePedidoResponseDTO> detalles) {
}
