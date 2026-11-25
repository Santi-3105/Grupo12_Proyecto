package com.aydsii.demo.CU_34.controller;

import com.aydsii.demo.CU_34.dto.ApiResponse;
import com.aydsii.demo.CU_34.dto.PedidoDetalleResponseDTO;
import com.aydsii.demo.CU_34.dto.PedidoResumenResponseDTO;
import com.aydsii.demo.CU_34.service.HistorialComprasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Punto de entrada HTTP del CU-03: Consultar historial de compras.
 * Solo recibe la solicitud, delega en {@link HistorialComprasService} y arma la respuesta HTTP.
 * Los errores los resuelve el GlobalExceptionHandler compartido.
 * El cliente se identifica con el {idCliente} de la URL
 * (FE-01 "sesión no iniciada" queda fuera hasta que exista autenticación).
 */
@RestController // Spring: clase que atiende HTTP y devuelve JSON (no vistas HTML)
// @RequestMapping a nivel de clase: prefijo común. {idCliente} es una parte VARIABLE de la URL.
@RequestMapping("/api/clientes/{idCliente}/pedidos")
@RequiredArgsConstructor // Lombok: constructor con el service "final" (inyección de dependencias)
@Tag(name = "Historial de compras", description = "CU-03: consulta de las compras realizadas por el cliente")
public class HistorialComprasController {

    // PATRÓN SIMPLE LOGGING FACADE: logger de la fachada SLF4J (equivale a @Slf4j de Lombok).
    private static final Logger log = LoggerFactory.getLogger(HistorialComprasController.class);

    private final HistorialComprasService historialComprasService;

    /**
     * GET /api/clientes/{idCliente}/pedidos — diagrama dinámico: paso 1 (el "1.1" lo hace el service).
     * Tipo de retorno, de afuera hacia adentro: ResponseEntity (respuesta HTTP completa)
     * > ApiResponse (nuestro envoltorio {status, message, data})
     * > List<PedidoResumenResponseDTO>>>; (el "data": una lista de resúmenes).
     */
    @GetMapping // sin ruta propia: responde a GET /api/clientes/{idCliente}/pedidos
    @Operation(summary = "Listar historial de compras",
            description = "Devuelve las compras del cliente (web y presenciales), de la más reciente a la más antigua: "
                    + "número de compra, fecha, modalidad y total. Si todavía no tiene compras devuelve una lista vacía (HTTP 200). "
                    + "Responde 403 si el cliente no existe o está desactivado.")
    public ResponseEntity<ApiResponse<List<PedidoResumenResponseDTO>>> listarHistorial(
            // @PathVariable: toma el valor de {idCliente} de la URL (ej: /api/clientes/5/pedidos -> 5).
            @Parameter(description = "ID del cliente que consulta", example = "1")
            @PathVariable("idCliente") Long idCliente) {

        log.debug("GET /api/clientes/{}/pedidos", idCliente);

        // Paso 1.1: delega en el service (puede lanzar ClienteInactivoException o ErrorObtencionException)
        List<PedidoResumenResponseDTO> historial = historialComprasService.listarHistorial(idCliente);

        // FA-01 (sin compras) y flujo normal responden 200; solo cambia el mensaje
        String mensaje = historial.isEmpty()
                ? "El cliente todavía no tiene compras realizadas"
                : "Historial de compras obtenido correctamente";

        ApiResponse<List<PedidoResumenResponseDTO>> apiResponse =
                ApiResponse.success(HttpStatus.OK.value(), mensaje, historial);

        // .value() convierte el enum HttpStatus.OK al número 200
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    /**
     * GET /api/clientes/{idCliente}/pedidos/{idPedido} — diagrama dinámico: paso 2 (el "2.1" lo hace el service).
     * Si la compra no existe o no es del cliente, el service lanza PedidoNoEncontradoException (404).
     */
    @GetMapping("/{idPedido}") // se suma al prefijo de la clase: /api/clientes/{idCliente}/pedidos/{idPedido}
    @Operation(summary = "Consultar detalle de una compra",
            description = "Devuelve el detalle de la compra seleccionada: productos, cantidad y precio unitario de cada uno, "
                    + "monto total y forma de entrega (envío o retiro). Solo se pueden ver compras propias.")
    public ResponseEntity<ApiResponse<PedidoDetalleResponseDTO>> obtenerDetalle(
            @Parameter(description = "ID del cliente que consulta", example = "1")
            @PathVariable("idCliente") Long idCliente,
            @Parameter(description = "ID de la compra seleccionada", example = "1")
            @PathVariable("idPedido") Long idPedido) {

        log.debug("GET /api/clientes/{}/pedidos/{}", idCliente, idPedido);

        PedidoDetalleResponseDTO detalle = historialComprasService.obtenerDetalle(idCliente, idPedido);

        ApiResponse<PedidoDetalleResponseDTO> apiResponse =
                ApiResponse.success(HttpStatus.OK.value(), "Detalle de la compra obtenido correctamente", detalle);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
