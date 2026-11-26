package com.aydsii.demo.CU_34.controller;

import com.aydsii.demo.CU_34.dto.ApiResponse;
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
@RestController
@RequestMapping("/api/clientes/{idCliente}/pedidos")
@RequiredArgsConstructor
@Tag(name = "Historial de compras", description = "CU-03: consulta de las compras realizadas por el cliente")
public class HistorialComprasController {

    private static final Logger log = LoggerFactory.getLogger(HistorialComprasController.class);

    private final HistorialComprasService historialComprasService;

    /**
     * GET /api/clientes/{idCliente}/pedidos.
     * Devuelve una lista de resúmenes de las compras del cliente.
     */
    @GetMapping
    @Operation(summary = "Listar historial de compras",
            description = "Devuelve las compras del cliente (web y presenciales), de la más reciente a la más antigua: "
                    + "número de compra, fecha, modalidad y total. Si todavía no tiene compras devuelve una lista vacía (HTTP 200). "
                    + "Responde 403 si el cliente no existe o está desactivado.")
    public ResponseEntity<ApiResponse<List<PedidoResumenResponseDTO>>> listarHistorial(
            @Parameter(description = "ID del cliente que consulta", example = "1")
            @PathVariable("idCliente") Long idCliente) {

        log.debug("GET /api/clientes/{}/pedidos", idCliente);
        List<PedidoResumenResponseDTO> historial = historialComprasService.listarHistorial(idCliente);

        String mensaje = historial.isEmpty()
                ? "El cliente todavía no tiene compras realizadas"
                : "Historial de compras obtenido correctamente";

        ApiResponse<List<PedidoResumenResponseDTO>> apiResponse =
                ApiResponse.success(HttpStatus.OK.value(), mensaje, historial);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
