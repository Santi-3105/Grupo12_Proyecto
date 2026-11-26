package com.aydsii.demo.exception;

import com.aydsii.demo.CU_34.dto.ApiResponse;
import com.aydsii.demo.CU_12.exception.CarritoVacioException;
import com.aydsii.demo.CU_12.exception.ClienteNoEncontradoException;
import com.aydsii.demo.CU_12.exception.MedioPagoNoEncontradoException;
import com.aydsii.demo.CU_12.exception.PedidoNoEncontradoException;
import com.aydsii.demo.CU_12.exception.SolicitudInvalidaException;
import com.aydsii.demo.CU_12.exception.StockInsuficienteException;
import com.aydsii.demo.CU_34.exception.ClienteInactivoException;
import com.aydsii.demo.CU_34.exception.DatosResenaInvalidosException;
import com.aydsii.demo.CU_34.exception.ErrorObtencionException;
import com.aydsii.demo.CU_34.exception.ErrorRegistroException;
import com.aydsii.demo.CU_34.exception.ProductoNoEncontradoException;
import com.aydsii.demo.CU_34.exception.ProductoNoResenableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Captura las excepciones de ambos módulos y conserva el formato de error propio de cada API:
 * {@link ProblemDetail} para CU_12 y {@link ApiResponse} para CU_34.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404: algo que se buscó por id no existe. */
    @ExceptionHandler({
            ClienteNoEncontradoException.class,
            MedioPagoNoEncontradoException.class,
            ProductoNoEncontradoException.class,
            PedidoNoEncontradoException.class })
    public ResponseEntity<?> handleNoEncontrado(RuntimeException ex, HttpServletRequest request) {
        if (esRutaCU34(request)) {
            log.warn("Recurso no encontrado: {}", ex.getMessage());
            return responderApi(HttpStatus.NOT_FOUND, ex.getMessage());
        }
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClienteInactivoException.class)
    public ResponseEntity<ApiResponse<?>> handleClienteInactivo(ClienteInactivoException ex) {
        log.warn("Cliente inexistente o inactivo: {}", ex.getMessage());
        return responderApi(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DatosResenaInvalidosException.class)
    public ResponseEntity<ApiResponse<?>> handleDatosResenaInvalidos(DatosResenaInvalidosException ex) {
        log.warn("Reseña con datos inválidos: {}", ex.getErrores());
        return responderApi(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ProductoNoResenableException.class)
    public ResponseEntity<ApiResponse<?>> handleProductoNoResenable(ProductoNoResenableException ex) {
        log.warn("Producto no reseñable: {}", ex.getMessage());
        return responderApi(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ErrorObtencionException.class)
    public ResponseEntity<ApiResponse<?>> handleErrorObtencion(ErrorObtencionException ex) {
        log.error("Falla al obtener la información: {}", ex.getMessage(), ex);
        return responderApi(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(ErrorRegistroException.class)
    public ResponseEntity<ApiResponse<?>> handleErrorRegistro(ErrorRegistroException ex) {
        log.error("Falla al registrar la reseña: {}", ex.getMessage(), ex);
        return responderApi(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    /** 400: carrito vacío o solicitud inconsistente. */
    @ExceptionHandler({ CarritoVacioException.class, SolicitudInvalidaException.class })
    public ResponseEntity<ProblemDetail> handleSolicitudIncorrecta(RuntimeException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * 409: conflicto con el estado actual (sin stock, o modificación concurrente
     * del stock).
     */
    @ExceptionHandler({ StockInsuficienteException.class, OptimisticLockingFailureException.class })
    public ResponseEntity<ProblemDetail> handleConflicto(RuntimeException ex) {
        String mensaje = (ex instanceof OptimisticLockingFailureException)
                ? "El stock fue modificado por otra compra al mismo tiempo, intente nuevamente"
                : ex.getMessage();
        return responder(HttpStatus.CONFLICT, mensaje);
    }

    /** 400: falló alguna validación de los DTO (@NotNull, @NotBlank, ...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, "Datos inválidos - " + detalle);
    }

    /**
     * 400: el JSON está mal formado o tiene un valor inválido (ej: tipoEntrega =
     * "XYZ").
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido (revise el formato JSON y los valores permitidos)");
    }

    /**
     * 400: falta un parámetro obligatorio de la URL (ej: GET /api/pedidos sin
     * ?clienteId=).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleParametroFaltante(MissingServletRequestParameterException ex) {
        return responder(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio '" + ex.getParameterName() + "'");
    }

    /**
     * 400: un parámetro o id de la URL tiene el tipo equivocado (ej:
     * /api/pedidos/abc).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El valor del parámetro '" + ex.getName() + "' es inválido");
    }

    /** 404: la URL pedida no existe. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleRutaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "La ruta solicitada no existe");
    }

    /** 405: la URL existe pero con otro método HTTP (ej: GET en vez de POST). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido para esta ruta");
    }

    /**
     * 500: cualquier error no previsto. Se loguea completo pero al cliente no se le
     * expone el detalle.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenerico(Exception ex) {
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /** Arma la respuesta de error con un ProblemDetail (el mensaje viaja en "detail"). */
    private ResponseEntity<ProblemDetail> responder(HttpStatus status, String mensaje) {
        log.warn("Respuesta de error {}: {}", status.value(), mensaje);
        return ResponseEntity.status(status)
                .body(ProblemDetail.forStatusAndDetail(status, mensaje));
    }

    private ResponseEntity<ApiResponse<?>> responderApi(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(ApiResponse.error(status.value(), mensaje));
    }

    private boolean esRutaCU34(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.matches("/api/clientes/[^/]+/pedidos")
                || path.equals("/api/resenas");
    }
}
