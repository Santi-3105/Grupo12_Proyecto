package com.aydsii.demo.exception;

import com.aydsii.demo.CU_34.dto.ApiResponse;
import com.aydsii.demo.CU_34.exception.ProductoNoEncontradoException;
import com.aydsii.demo.CU_34.exception.ProductoNoResenableException;
import com.aydsii.demo.CU_12.exception.PedidoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void productoNoEncontradoEnCu34MantieneElEnvoltorioApiResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/resenas");

        var response = handler.handleNoEncontrado(new ProductoNoEncontradoException(2L), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }

    @Test
    void pedidoNoEncontradoEnCu12MantieneProblemDetail() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/pedidos/2");

        var response = handler.handleNoEncontrado(new PedidoNoEncontradoException(2L), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ProblemDetail.class, response.getBody());
    }

    @Test
    void productoNoResenableRespondeConflictoConApiResponse() {
        var response = handler.handleProductoNoResenable(
                new ProductoNoResenableException("Producto ya reseñado"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }
}
