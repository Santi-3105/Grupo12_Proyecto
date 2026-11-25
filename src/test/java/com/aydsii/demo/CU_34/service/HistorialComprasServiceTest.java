package com.aydsii.demo.CU_34.service;

import com.aydsii.demo.CU_34.exception.ErrorObtencionException;
import com.aydsii.demo.CU_34.repository.ClienteRepository;
import com.aydsii.demo.CU_34.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistorialComprasServiceTest {

    @Test
    void fallaAlBuscarClienteSeTraduceAErrorObtencion() {
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        PedidoRepository pedidoRepository = mock(PedidoRepository.class);
        when(clienteRepository.findById(1L))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        HistorialComprasService service = new HistorialComprasService(clienteRepository, pedidoRepository);

        assertThrows(ErrorObtencionException.class, () -> service.listarHistorial(1L));
    }
}
