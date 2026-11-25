package com.aydsii.demo.CU_34.controller;

import com.aydsii.demo.CU_34.dto.ApiResponse;
import com.aydsii.demo.CU_34.dto.ProductoResenableResponseDTO;
import com.aydsii.demo.CU_34.dto.ResenaRequestDTO;
import com.aydsii.demo.CU_34.dto.ResenaResponseDTO;
import com.aydsii.demo.CU_34.service.ResenaService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Punto de entrada HTTP del CU-04: Agregar reseña de productos.
 * Solo recibe la solicitud, delega en {@link ResenaService} y arma la respuesta HTTP.
 * El cliente se identifica con el idCliente (en la URL o en el body).
 */
@RestController
@RequestMapping("/api") // prefijo común; cada método agrega su ruta completa
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "CU-04: reseñas de productos comprados")
public class ResenaController {

    // PATRÓN SIMPLE LOGGING FACADE: logger de la fachada SLF4J.
    private static final Logger log = LoggerFactory.getLogger(ResenaController.class);

    private final ResenaService resenaService;

    /** GET /api/clientes/{idCliente}/productos-resenables — diagrama dinámico: paso 1. */
    @GetMapping("/clientes/{idCliente}/productos-resenables")
    @Operation(summary = "Listar productos para reseñar",
            description = "Devuelve los productos que el cliente compró (compra ya concretada: entregado o retirado) "
                    + "y todavía no reseñó. Si no hay ninguno devuelve una lista vacía (HTTP 200). "
                    + "Responde 403 si el cliente no existe o está desactivado.")
    public ResponseEntity<ApiResponse<List<ProductoResenableResponseDTO>>> listarProductosParaResenar(
            @Parameter(description = "ID del cliente que quiere reseñar", example = "1")
            @PathVariable("idCliente") Long idCliente) {

        log.debug("GET /api/clientes/{}/productos-resenables", idCliente);

        List<ProductoResenableResponseDTO> productos = resenaService.listarProductosParaResenar(idCliente);

        // FA-01: sin productos para reseñar -> igual 200, con otro mensaje
        String mensaje = productos.isEmpty()
                ? "No hay productos disponibles para reseñar"
                : "Productos disponibles para reseñar obtenidos correctamente";

        ApiResponse<List<ProductoResenableResponseDTO>> apiResponse =
                ApiResponse.success(HttpStatus.OK.value(), mensaje, productos);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    /**
     * POST /api/resenas — diagrama dinámico: paso 2 (el "2.1" lo hace el service).
     * {@code @RequestBody} convierte el JSON del cuerpo de la petición en un objeto ResenaRequestDTO.
     * Acá NO se usa {@code @Valid}: la validación la hace el service (paso 2.1.3 del diagrama) y devuelve
     * todos los errores juntos.
     */
    @PostMapping("/resenas")
    @Operation(summary = "Agregar reseña de un producto",
            description = "Registra la calificación (1 a 5) y un comentario opcional sobre un producto que el cliente compró "
                    + "y cuya compra ya fue concretada. Solo se permite una reseña por producto. "
                    + "Errores: 400 datos inválidos, 403 cliente desactivado, 404 producto inexistente, "
                    + "409 producto no reseñable (compra no concretada o ya reseñado), 503 error al registrar.")
    public ResponseEntity<ApiResponse<ResenaResponseDTO>> agregarResena(
            @RequestBody ResenaRequestDTO resenaRequestDTO) {

        log.debug("POST /api/resenas - request: {}", resenaRequestDTO);

        ResenaResponseDTO resenaCreada = resenaService.agregarResena(resenaRequestDTO);

        ApiResponse<ResenaResponseDTO> apiResponse =
                ApiResponse.success(HttpStatus.CREATED.value(), "Reseña registrada correctamente", resenaCreada);

        // 201 Created: se creó un recurso nuevo (la reseña)
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
