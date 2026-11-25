package com.aydsii.demo.CU_34.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DEMOSTRACIÓN DEL PATRÓN SINGLETON. Es un test de Java puro (no levanta Spring ni la base de datos).
 * Se corre desde VS Code (botón "Run Test" al lado de cada método) y cada test prueba UNA de las
 * características del patrón. Ideal para mostrarle a la profesora que el patrón realmente funciona.
 */
class ReglasResenaTest {

    @Test
    void getInstanciaDevuelveSiempreLaMismaInstancia() {
        ReglasResena primera = ReglasResena.getInstancia();
        ReglasResena segunda = ReglasResena.getInstancia();

        // assertSame compara con "==": ¿son EXACTAMENTE el mismo objeto en memoria? (no "iguales": el mismo)
        assertSame(primera, segunda);
    }

    @Test
    void elConstructorEsPrivadoAsiQueNadiePuedeHacerNew() throws NoSuchMethodException {
        // Reflection: se le "pregunta" a la clase por su constructor y por sus modificadores.
        Constructor<ReglasResena> constructor = ReglasResena.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    void laClaseEsFinalAsiQueNadiePuedeHeredarDeElla() {
        assertTrue(Modifier.isFinal(ReglasResena.class.getModifiers()));
    }

    @Test
    void lasReglasTienenLosValoresDelCasoDeUso() {
        ReglasResena reglas = ReglasResena.getInstancia();

        assertTrue(reglas.esCalificacionValida(1));
        assertTrue(reglas.esCalificacionValida(5));
        assertFalse(reglas.esCalificacionValida(0));
        assertFalse(reglas.esCalificacionValida(6));
        assertFalse(reglas.esCalificacionValida(null)); // no se ingresó
        assertTrue(reglas.esComentarioValido(null));    // el comentario es opcional
        assertFalse(reglas.esComentarioValido("x".repeat(501)));
    }

    @Test
    void laListaDeEstadosNoSePuedeModificarDesdeAfuera() {
        // Si se pudiera modificar, cualquiera podría cambiar la regla compartida y romper a los demás.
        assertThrows(UnsupportedOperationException.class,
                () -> ReglasResena.getInstancia().getEstadosCompraConcretada().add("OTRO"));
    }
}
