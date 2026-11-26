package com.aydsii.demo.CU_34.controller;

import com.aydsii.demo.CU_34.dto.ApiResponse;
import com.aydsii.demo.CU_34.dto.ResenaRequestDTO;
import com.aydsii.demo.CU_34.dto.ResenaResponseDTO;
import com.aydsii.demo.CU_34.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Punto de entrada HTTP del CU-04: Agregar reseña de productos.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "CU-04: agregar reseñas de productos comprados")
public class ResenaController {

    private static final Logger log = LoggerFactory.getLogger(ResenaController.class);

    private final ResenaService resenaService;

    /**
     * POST /api/resenas registra una reseña si el cliente y la compra cumplen las reglas del caso de uso.
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

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
