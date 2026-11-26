package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.config.ReglasResena;
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

/**
 * Lógica de negocio del CU-04: Agregar reseña de productos.
 */
@Service
@RequiredArgsConstructor
public class ResenaService {

    private static final Logger log = LoggerFactory.getLogger(ResenaService.class);

    private final ReglasResena reglas = ReglasResena.getInstancia();

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final ResenaRepository resenaRepository;

    /**
     * Registra una reseña si el cliente está activo, compró el producto y aún no lo reseñó.
     */
    @Transactional
    public ResenaResponseDTO agregarResena(ResenaRequestDTO dto) {
        if (dto == null || dto.idCliente() == null) {
            throw new DatosResenaInvalidosException(List.of("El idCliente es obligatorio"));
        }
        log.info("CU-04 - Agregando reseña del cliente {} sobre el producto {}", dto.idCliente(), dto.idProducto());

        Cliente cliente = buscarCliente(dto.idCliente());
        validarClienteActivo(cliente);
        validarDatosResena(dto);

        Producto producto = productoRepository.findById(dto.idProducto())
                .orElseThrow(() -> new ProductoNoEncontradoException(dto.idProducto()));

        boolean compraConcretada = pedidoRepository.existsCompraConcretada(
                cliente.getIdCliente(), producto.getIdProducto(), reglas.getEstadosCompraConcretada());
        boolean resenaExistente = resenaRepository.existsByClienteAndProducto(cliente, producto);
        log.debug("Compra concretada: {} - Reseña existente: {}", compraConcretada, resenaExistente);

        if (!compraConcretada) {
            throw new ProductoNoResenableException(
                    "Solo se puede reseñar un producto comprado cuya compra ya fue concretada (entregado o retirado)");
        }
        if (resenaExistente) {
            throw new ProductoNoResenableException(
                    "Ya registraste una reseña para este producto: se permite una sola por producto");
        }

        Resena resena = new Resena(
                cliente,
                producto,
                dto.calificacion(),
                normalizarComentario(dto.comentario()),
                LocalDateTime.now());
        Resena resenaGuardada = guardarResena(resena);

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

    private Cliente buscarCliente(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteInactivoException(idCliente));
    }

    private void validarClienteActivo(Cliente cliente) {
        if (!cliente.estaActivo()) {
            throw new ClienteInactivoException(cliente.getIdCliente());
        }
    }

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

    private String normalizarComentario(String comentario) {
        if (comentario == null || comentario.isBlank()) {
            return null;
        }
        return comentario.trim();
    }

    private Resena guardarResena(Resena resena) {
        try {
            return resenaRepository.saveAndFlush(resena);
        } catch (DataIntegrityViolationException e) {
            throw new ProductoNoResenableException(
                    "Ya registraste una reseña para este producto: se permite una sola por producto");
        } catch (DataAccessException e) {
            throw new ErrorRegistroException(e);
        }
    }
}
