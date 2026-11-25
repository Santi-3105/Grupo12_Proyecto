package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.config.ReglasResena;
import com.aydsii.demo.CU_34.dto.ProductoResenableResponseDTO;
import com.aydsii.demo.CU_34.dto.ResenaRequestDTO;
import com.aydsii.demo.CU_34.dto.ResenaResponseDTO;
import com.aydsii.demo.CU_34.exception.ClienteInactivoException;
import com.aydsii.demo.CU_34.exception.DatosResenaInvalidosException;
import com.aydsii.demo.CU_34.exception.ErrorRegistroException;
import com.aydsii.demo.CU_34.exception.ProductoNoEncontradoException;
import com.aydsii.demo.CU_34.exception.ProductoNoResenableException;
import com.aydsii.demo.CU_34.model.Cliente;
import com.aydsii.demo.CU_34.model.Producto;
import com.aydsii.demo.CU_34.model.Resena;
import com.aydsii.demo.CU_34.repository.ClienteRepository;
import com.aydsii.demo.CU_34.repository.PedidoRepository;
import com.aydsii.demo.CU_34.repository.ProductoRepository;
import com.aydsii.demo.CU_34.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lógica de negocio del CU-04: Agregar reseña de productos.
 */
// Indica que es de tipo Service, indica que puede inyectar dependecias y definir la logica del negocio
@Service
@RequiredArgsConstructor // genera el constructor con los 4 repositorios "final" sin valor inicial
public class ResenaService {

    // PATRÓN SIMPLE LOGGING FACADE: el código depende SOLO de la interfaz org.slf4j.Logger. Es decir, no le interesa para quien va a escribir (que liberia usara, en este caso Slf4j)
    // "static final": un logger por clase. Esta línea es EXACTAMENTE lo que Lombok genera con @Slf4j.
    private static final Logger log = LoggerFactory.getLogger(ResenaService.class);

    // PATRÓN SINGLETON: no se hace "new ReglasResena()" (el constructor es privado). Se pide la instancia unica
    private final ReglasResena reglas = ReglasResena.getInstancia();

    private final ClienteRepository clienteRepository; // compartido con CU_12
    private final ProductoRepository productoRepository; // compartido con CU_12
    private final PedidoRepository pedidoRepository; // compartido con CU_12
    private final ResenaRepository resenaRepository;

    // =====================================================================
    //  PASO 1: LISTAR PRODUCTOS PARA RESEÑAR
    // =====================================================================

    /**
     * Productos que el cliente compró (compra concretada) y todavía no reseñó.
     *
     * @return lista de productos; vacía si no hay ninguno (FA-01)
     */
    @Transactional(readOnly = true) // solo lectura: no modifica datos
    public List<ProductoResenableResponseDTO> listarProductosParaResenar(Long idCliente) {
        log.info("CU-04 - Listando productos para reseñar del cliente {}", idCliente);

        // Pasos 1.1.1 y 1.1.2: buscarCliente + validarClienteActivo (FE-02)
        Cliente cliente = buscarCliente(idCliente);
        validarClienteActivo(cliente);

        // Paso 1.1.3: buscarProductosParaResenar(idCliente) -> productos.
        // Los estados que significan "compra concretada" salen del Singleton (RN-02).
        List<Producto> productos = productoRepository.findProductosParaResenar(
                cliente.getIdCliente(), reglas.getEstadosCompraConcretada());

        // FA-01: no hay productos para reseñar -> listaVacia
        if (productos.isEmpty()) {
            log.debug("El cliente {} no tiene productos para reseñar", idCliente);
            return List.of();
        }

        // Paso 1.1.4 (loop "por cada producto"): «create» ProductoResenableResponseDTO(idProducto, nombre, categoria)
        return productos.stream()
                .map(this::mapearAProductoResenableDTO)
                .toList();
    }

    // =====================================================================
    //  PASO 2: AGREGAR LA RESEÑA
    // =====================================================================

    /**
     * Registra la reseña de un producto.
     * <p>
     * {@code @Transactional} (sin readOnly): este método ESCRIBE en la base. Si algo falla (cualquier
     * RuntimeException), Spring hace rollback y no queda nada a medias.
     */
    @Transactional
    public ResenaResponseDTO agregarResena(ResenaRequestDTO dto) {
        // Guarda previa: sin idCliente no se puede ni buscar al cliente (findById(null) rompería).
        if (dto == null || dto.idCliente() == null) {
            throw new DatosResenaInvalidosException(List.of("El idCliente es obligatorio"));
        }
        log.info("CU-04 - Agregando reseña del cliente {} sobre el producto {}", dto.idCliente(), dto.idProducto());

        // Pasos 2.1.1 y 2.1.2: buscarCliente + validarClienteActivo (FE-02)
        Cliente cliente = buscarCliente(dto.idCliente());
        validarClienteActivo(cliente);

        // Paso 2.1.3: validarDatosResena(dto) (FE-03, RN-04)
        validarDatosResena(dto);

        // Paso 2.1.4: buscarProducto(idProducto) -> producto. Si no existe -> ProductoNoEncontradoException (404)
        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(() -> new ProductoNoEncontradoException(dto.idProducto()));

        // Paso 2.1.5: existsCompraConcretada(idCliente, idProducto) -> compraConcretada (RN-02)
        boolean compraConcretada = pedidoRepository.existsCompraConcretada(
                cliente.getIdCliente(), producto.getIdProducto(), reglas.getEstadosCompraConcretada());

        // Paso 2.1.6: existsByClienteAndProducto(cliente, producto) -> resenaExistente (RN-03)
        boolean resenaExistente = resenaRepository.existsByClienteAndProducto(cliente, producto);
        log.debug("Compra concretada: {} - Reseña existente: {}", compraConcretada, resenaExistente);

        // Bloque alt "[RN-02 / RN-03: compra no concretada o producto ya reseñado]" -> ProductoNoResenableException
        if (!compraConcretada) {
            throw new ProductoNoResenableException(
                    "Solo se puede reseñar un producto comprado cuya compra ya fue concretada (entregado o retirado)");
        }
        if (resenaExistente) {
            throw new ProductoNoResenableException(
                    "Ya registraste una reseña para este producto: se permite una sola por producto");
        }

        // Paso 2.1.7: «create» Resena(cliente, producto, calificacion, comentario, fecha)
        Resena resena = new Resena(
                cliente,
                producto,
                dto.calificacion(),
                normalizarComentario(dto.comentario()),
                LocalDateTime.now()); // RN-05: la reseña queda con la fecha de publicación

        // Paso 2.1.8: save(resena) -> resenaGuardada. FE-04: si falla -> ErrorRegistroException
        Resena resenaGuardada = guardarResena(resena);

        // Paso 2.1.9: «create» ResenaResponseDTO(idResena, idCliente, idProducto, calificacion, comentario, fecha)
        log.info("CU-04 - Reseña {} registrada (cliente {}, producto {})",
                resenaGuardada.getIdResena(), cliente.getIdCliente(), producto.getIdProducto());
        return new ResenaResponseDTO(
                resenaGuardada.getIdResena(),
                cliente.getIdCliente(),
                producto.getIdProducto(),
                resenaGuardada.getCalificacion(),
                resenaGuardada.getComentario(),
                resenaGuardada.getFecha());
    }

    // ---------------------------------------------------------------------
    //  Métodos privados (son los "mensajes a sí mismo" del diagrama)
    // ---------------------------------------------------------------------

    /** Pasos 1.1.1 / 2.1.1: busca el cliente. Optional.orElseThrow: si no está, lanza la excepción. */
    private Cliente buscarCliente(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteInactivoException(idCliente));
    }

    /** Pasos 1.1.2 / 2.1.2 (RN-01): solo un cliente activo puede reseñar. */
    private void validarClienteActivo(Cliente cliente) {
        if (!cliente.estaActivo()) {
            throw new ClienteInactivoException(cliente.getIdCliente());
        }
    }

    /**
     * Paso 2.1.3 (FE-03, RN-04): valida los datos y junta TODOS los errores en una lista, así el cliente
     * corrige todo de una vez. Las reglas (rango, longitud) NO están escritas acá: se las pregunta al
     * Singleton ReglasResena, que es la única fuente de verdad.
     */
    private void validarDatosResena(ResenaRequestDTO dto) {
        List<String> errores = new ArrayList<>();

        if (dto.idProducto() == null) {
            errores.add("El idProducto es obligatorio");
        }
        if (dto.calificacion() == null) {
            errores.add("La calificación es obligatoria");
        } else if (!reglas.esCalificacionValida(dto.calificacion())) {
            errores.add("La calificación debe estar entre " + reglas.getCalificacionMinima()
                    + " y " + reglas.getCalificacionMaxima());
        }
        if (!reglas.esComentarioValido(dto.comentario())) {
            errores.add("El comentario no puede superar los " + reglas.getLongitudMaximaComentario() + " caracteres");
        }

        if (!errores.isEmpty()) {
            throw new DatosResenaInvalidosException(errores);
        }
    }

    /**
     * Limpia el comentario: le saca espacios de los costados y, si quedó vacío, lo guarda como null.
     * {@code isBlank()} es true si el texto está vacío o tiene solo espacios.
     */
    private String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }

    /**
     * Paso 2.1.8: guarda y sincroniza la reseña con la base. El flush dentro del try permite traducir
     * restricciones de integridad detectadas por la base antes de que termine la transacción.
     * Hay dos fallas posibles y el ORDEN de los catch importa (el más
     * específico primero, porque DataIntegrityViolationException "es una" DataAccessException):
     * Por ejemplo:
     * DataIntegrityViolationException: la restricción UNIQUE (cliente, producto) frenó un duplicado (dos pedidos simultáneos).
     * Es el caso RN-03, no una falla del sistema.
     * DataAccessException (cualquier otra falla de la base): FE-04 -> ErrorRegistroException.
     */
    private Resena guardarResena(Resena resena) {
        try {
            return resenaRepository.saveAndFlush(resena);
        } catch (DataIntegrityViolationException e) {
            throw new ProductoNoResenableException(
                    "Ya registraste una reseña para este producto: se permite una sola por producto");
        } catch (DataAccessException e) {
            throw new ErrorRegistroException(e); // el handler la registra en el log con su stack trace
        }
    }

    /**
     * Paso 1.1.4: convierte un Producto en el DTO resumido.
     * {@code Objects.toString(x, null)} convierte a texto de forma segura: si la categoría es null devuelve
     * null en vez de romper (y funciona tanto si la categoría es un String como un enum).
     */
    private ProductoResenableResponseDTO mapearAProductoResenableDTO(Producto producto) {
        return new ProductoResenableResponseDTO(
                producto.getIdProducto(),
                producto.getNombre(),
                Objects.toString(producto.getCategoria(), null));
    }
}
