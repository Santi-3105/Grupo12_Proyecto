package com.aydsii.demo.CU_12.dto;

import com.aydsii.demo.CU_34.model.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos del pedido que se devuelven al cliente luego de comprar.
 * Se usa un DTO (y no la entidad Pedido) para no exponer el modelo interno
 * ni arrastrar relaciones lazy al JSON.
 */
public record PedidoResponseDTO(
        Long idPedido,
        LocalDateTime fecha,
        TipoEntrega tipoEntrega,
        BigDecimal total,
        String estado
) {
}
