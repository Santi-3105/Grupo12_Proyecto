package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.exception.PedidoNoEncontradoException;
import com.aydsii.demo.CU_34.model.Cliente;
import com.aydsii.demo.CU_34.model.DetallePedido;
import com.aydsii.demo.CU_34.model.Pedido;
import com.aydsii.demo.CU_34.repository.ClienteRepository;
import com.aydsii.demo.CU_34.repository.PedidoRepository;
import com.aydsii.demo.CU_34.dto.DetallePedidoResponseDTO;
import com.aydsii.demo.CU_34.dto.PedidoDetalleResponseDTO;
import com.aydsii.demo.CU_34.dto.PedidoResumenResponseDTO;
import com.aydsii.demo.CU_34.exception.ClienteInactivoException;
import com.aydsii.demo.CU_34.exception.ErrorObtencionException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio del CU-03: Consultar historial de compras.
 * Es de SOLO CONSULTA (RN-04): no hay ningún método que modifique o elimine compras.
 */
// Indica que es de tipo Service, indica que puede inyectar dependecias y definir la logica del negocio
@Service
// @RequiredArgsConstructor (Lombok): genera el constructor que recibe los campos "final" sin valor inicial.
// Spring lo usa para inyectar los repositorios (inyección de dependencias por constructor).
@RequiredArgsConstructor
public class HistorialComprasService {

    // PATRÓN SIMPLE LOGGING FACADE: el código depende SOLO de la interfaz org.slf4j.Logger. Es decir, no le interesa para quien va a escribir (que liberia usara, en este caso Slf4j)
    // "static final": un logger por clase. Esta línea es EXACTAMENTE lo que Lombok genera con @Slf4j.
    private static final Logger log = LoggerFactory.getLogger(HistorialComprasService.class);

    // "final" = se asigna una sola vez (en el constructor) y no se puede reasignar después.
    private final ClienteRepository clienteRepository; // compartido con CU_12
    private final PedidoRepository pedidoRepository; // compartido con CU_12

    /**
     * Lista las compras del cliente, de la más reciente a la más antigua (RN-03).
     * <p>
     * {@code @Transactional(readOnly = true)}: transacción SOLO DE LECTURA.
     * @return resumen de cada compra; lista vacía si todavía no tiene compras (FA-01)
     */
    @Transactional(readOnly = true)
    public List<PedidoResumenResponseDTO> listarHistorial(Long idCliente) {
        log.info("CU-03 - Listando historial de compras del cliente {}", idCliente);

        // Paso 1.1.1: buscarCliente(idCliente) -> cliente
        Cliente cliente = buscarCliente(idCliente);

        // Paso 1.1.2: validarClienteActivo(cliente). FE-02: inexistente o inactivo -> excepción
        validarClienteActivo(cliente);

        // Paso 1.1.3: buscarPedidosPorCliente(cliente), ya ordenados de más reciente a más antiguo
        List<Pedido> pedidos = buscarPedidosPorCliente(cliente);

        // FA-01: el cliente no tiene compras -> historialVacio (el controller responde 200 con un mensaje)
        if (pedidos.isEmpty()) {
            log.debug("El cliente {} no tiene compras registradas", idCliente);
            return List.of();
        }

        // Paso 1.1.4 (loop "por cada pedido"): «create» PedidoResumenResponseDTO
        // .stream().map(...).toList(): recorre la lista, transforma cada Pedido en su DTO y arma una lista nueva.
        // "this::mapearAResumenDTO" es una referencia a un método de esta clase.
        List<PedidoResumenResponseDTO> historial = pedidos.stream()
                .map(this::mapearAResumenDTO)
                .toList();
        log.debug("Historial del cliente {}: {} compras", idCliente, historial.size());
        return historial;
    }

    /**
     * Devuelve el detalle de UNA compra del cliente: productos, cantidades, precios, total, modalidad de compra y forma de entrega.
     *
     * @throws PedidoNoEncontradoException si el pedido no existe o no es de ese cliente (RN-02, HTTP 404)
     */
    @Transactional(readOnly = true)
    public PedidoDetalleResponseDTO obtenerDetalle(Long idCliente, Long idPedido) {
        log.info("CU-03 - Consultando detalle de la compra {} del cliente {}", idPedido, idCliente);

        // Pasos 2.1.1 y 2.1.2: igual que en el listado
        Cliente cliente = buscarCliente(idCliente);
        validarClienteActivo(cliente);

        // Paso 2.1.3: buscarPedidoDeCliente(idPedido, cliente)
        Pedido pedido = buscarPedidoDeCliente(idPedido, cliente);

        // Paso 2.1.4 (loop "por cada detalle"): «create» DetallePedidoResponseDTO
        List<DetallePedidoResponseDTO> detalles = pedido.getDetalles().stream()
                .map(this::mapearADetalleDTO)
                .toList();

        // Paso 2.1.5: «create» PedidoDetalleResponseDTO(idPedido, fecha, modalidadCompra, tipoEntrega, total, detalles)
        return new PedidoDetalleResponseDTO(
                pedido.getIdPedido(),
                pedido.getFecha(),
                pedido.getModalidadCompra(),
                pedido.getTipoEntrega(),
                pedido.getTotal(),
                detalles);
    }

    // ---------------------------------------------------------------------
    //  Métodos privados (son los "mensajes a sí mismo" del diagrama)
    // ---------------------------------------------------------------------

    /**
     * Paso 1.1.1 / 2.1.1: busca el cliente por id.
     * {@code findById} devuelve un {@code Optional<Cliente>}: una "caja" que puede tener el cliente o estar vacía.
     * {@code orElseThrow} dice "si está vacía, lanzá esta excepción".
     */
    private Cliente buscarCliente(Long idCliente) {
        try {
            return clienteRepository.findById(idCliente)
                    .orElseThrow(() -> new ClienteInactivoException(idCliente));
        } catch (DataAccessException e) {
            throw new ErrorObtencionException(e);
        }
    }

    /** Paso 1.1.2 / 2.1.2 (RN-01): solo un cliente activo puede consultar su historial. */
    private void validarClienteActivo(Cliente cliente) {
        if (!cliente.estaActivo()) {
            throw new ClienteInactivoException(cliente.getIdCliente());
        }
    }

    /**
     * Paso 1.1.3: recupera las compras del cliente (web y presenciales).
     * FE-03: si la base falla, se captura la DataAccessException (excepción base de Spring para errores de
     * acceso a datos) y se la traduce a ErrorObtencionException, guardando la original como causa.
     */
    private List<Pedido> buscarPedidosPorCliente(Cliente cliente) {
        try {
            return pedidoRepository.findByCliente_IdClienteOrderByFechaDesc(cliente.getIdCliente());
        } catch (DataAccessException e) {
            throw new ErrorObtencionException(e);
        }
    }

    /**
     * Paso 2.1.3: busca UNA compra, solo si es del cliente (RN-02). Si no existe o es de otro cliente
     * -> PedidoNoEncontradoException (404); desde afuera ambos casos se ven igual.
     */
    private Pedido buscarPedidoDeCliente(Long idPedido, Cliente cliente) {
        Pedido pedido;
        try {
            pedido = pedidoRepository.findByIdPedidoAndCliente_IdCliente(idPedido, cliente.getIdCliente())
                    .orElse(null);
        } catch (DataAccessException e) {
            throw new ErrorObtencionException(e);
        }
        if (pedido == null) {
            throw new PedidoNoEncontradoException(idPedido);
        }
        return pedido;
    }

    /** Paso 1.1.4: convierte un Pedido (entidad) en la fila del listado (DTO). */
    private PedidoResumenResponseDTO mapearAResumenDTO(Pedido pedido) {
        return new PedidoResumenResponseDTO(
                pedido.getIdPedido(),
                pedido.getFecha(),
                pedido.getModalidadCompra(),
                pedido.getTotal());
    }

    /** Paso 2.1.4: convierte un DetallePedido (entidad) en el DTO del detalle. */
    private DetallePedidoResponseDTO mapearADetalleDTO(DetallePedido detalle) {
        return new DetallePedidoResponseDTO(
                detalle.getProducto().getIdProducto(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal());
    }
}
