package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.dto.ResenaRequestDTO;
import com.aydsii.demo.CU_34.exception.ErrorRegistroException;
import com.aydsii.demo.CU_34.exception.ProductoNoResenableException;
import com.aydsii.demo.CU_34.model.Cliente;
import com.aydsii.demo.CU_34.model.Producto;
import com.aydsii.demo.CU_34.repository.ClienteRepository;
import com.aydsii.demo.CU_34.repository.PedidoRepository;
import com.aydsii.demo.CU_34.repository.ProductoRepository;
import com.aydsii.demo.CU_34.repository.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResenaServiceTest {

    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final ProductoRepository productoRepository = mock(ProductoRepository.class);
    private final PedidoRepository pedidoRepository = mock(PedidoRepository.class);
    private final ResenaRepository resenaRepository = mock(ResenaRepository.class);
    private final ResenaService service = new ResenaService(
            clienteRepository, productoRepository, pedidoRepository, resenaRepository);

    private final Cliente cliente = new Cliente(
            "Ana", "Díaz", "33777888", LocalDate.of(1992, 11, 30),
            "ana@example.com", "2302444444", true);
    private final Producto producto = new Producto(
            "Teclado", "Periféricos", "Teclado mecánico",
            new BigDecimal("45000.00"), 8, true);

    @BeforeEach
    void prepararCompraElegible() {
        cliente.setIdCliente(1L);
        producto.setIdProducto(3L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.existsCompraConcretada(eq(1L), eq(3L), anyCollection())).thenReturn(true);
        when(resenaRepository.existsByClienteAndProducto(cliente, producto)).thenReturn(false);
    }

    @Test
    void unaRestriccionUnicaDetectadaAlHacerFlushSeTraduceAProductoNoResenable() {
        when(resenaRepository.saveAndFlush(any())).thenThrow(
                new DataIntegrityViolationException("duplicate client/product review"));

        assertThrows(ProductoNoResenableException.class, () -> service.agregarResena(
                new ResenaRequestDTO(1L, 3L, 5, "Muy bueno")));

        verify(resenaRepository).saveAndFlush(any());
    }

    @Test
    void otroErrorDePersistenciaSeTraduceAErrorRegistro() {
        when(resenaRepository.saveAndFlush(any())).thenThrow(
                new DataAccessResourceFailureException("database unavailable"));

        assertInstanceOf(ErrorRegistroException.class, assertThrows(
                ErrorRegistroException.class, () -> service.agregarResena(
                        new ResenaRequestDTO(1L, 3L, 5, "Muy bueno"))));

        verify(resenaRepository).saveAndFlush(any());
    }
}
