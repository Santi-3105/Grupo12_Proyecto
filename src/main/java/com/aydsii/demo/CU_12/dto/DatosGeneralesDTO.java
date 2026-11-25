package com.aydsii.demo.CU_12.dto;

import com.aydsii.demo.CU_34.model.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos generales del pedido dentro de {@link PedidoEstadoDTO}. */
public record DatosGeneralesDTO(
        Long idPedido,
        LocalDateTime fecha,
        TipoEntrega tipoEntrega,
        BigDecimal total
) {
}
