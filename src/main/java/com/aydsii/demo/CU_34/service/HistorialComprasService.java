package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.dto.PedidoResumenResponseDTO;
import com.aydsii.demo.CU_34.exception.ClienteInactivoException;
import com.aydsii.demo.CU_34.exception.ErrorObtencionException;
import com.aydsii.demo.CU_34.model.Cliente;
import com.aydsii.demo.CU_34.model.Pedido;
import com.aydsii.demo.CU_34.repository.ClienteRepository;
import com.aydsii.demo.CU_34.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del CU-03: Consultar historial de compras.
 */
@Service
@RequiredArgsConstructor
public class HistorialComprasService {

    private static final Logger log = LoggerFactory.getLogger(HistorialComprasService.class);

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    /**
     * Recupera las compras del cliente, ordenadas de la más reciente a la más antigua.
     * Si no hay compras, devuelve una lista vacía.
     */
    @Transactional(readOnly = true)
    public List<PedidoResumenResponseDTO> listarHistorial(Long idCliente) {
        log.info("CU-03 - Listando historial de compras del cliente {}", idCliente);

        Cliente cliente = buscarCliente(idCliente);
        validarClienteActivo(cliente);
        List<Pedido> pedidos = buscarPedidosPorCliente(cliente);

        if (pedidos.isEmpty()) {
            log.debug("El cliente {} no tiene compras registradas", idCliente);
            return List.of();
        }

        List<PedidoResumenResponseDTO> historial = pedidos.stream()
                .map(this::mapearAResumenDTO)
                .toList();
        log.debug("Historial del cliente {}: {} compras", idCliente, historial.size());
        return historial;
    }

    private Cliente buscarCliente(Long idCliente) {
        try {
            return clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new ClienteInactivoException(idCliente));
        } catch (DataAccessException e) {
            throw new ErrorObtencionException(e);
        }
    }

    private void validarClienteActivo(Cliente cliente) {
        if (!cliente.estaActivo()) {
            throw new ClienteInactivoException(cliente.getIdCliente());
        }
    }

    private List<Pedido> buscarPedidosPorCliente(Cliente cliente) {
        try {
            return pedidoRepository.findByCliente_IdClienteOrderByFechaDesc(cliente.getIdCliente());
        } catch (DataAccessException e) {
            throw new ErrorObtencionException(e);
        }
    }

    private PedidoResumenResponseDTO mapearAResumenDTO(Pedido pedido) {
        return new PedidoResumenResponseDTO(
                pedido.getIdPedido(),
                pedido.getFecha(),
                pedido.getModalidadCompra(),
                pedido.getTotal());
    }
}
