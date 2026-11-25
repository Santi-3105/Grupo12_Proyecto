package com.aydsii.demo.CU_34.logging;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DEMOSTRACIÓN DEL PATRÓN SIMPLE LOGGING FACADE (SLF4J). Test de Java puro (no levanta Spring).
 * Al correrlo, mirá la CONSOLA: imprime qué interfaz usa nuestro código y qué clase real ejecuta por detrás.
 */
class LoggingFacadeTest {

    // Nuestro código pide el logger a la FACHADA. Fijate los imports: solo "org.slf4j", nunca "ch.qos.logback".
    private static final Logger log = LoggerFactory.getLogger(LoggingFacadeTest.class);

    @Test
    void nuestroCodigoSoloDependeDeLaInterfazDeLaFachada() {
        // Logger (de SLF4J) es una INTERFAZ: define qué se puede hacer (info, warn, error...) pero no cómo.
        assertTrue(Logger.class.isInterface());

        System.out.println("Interfaz que usa nuestro código (la FACHADA)  : " + Logger.class.getName());
        System.out.println("Clase real que ejecuta por detrás (el BINDING): " + log.getClass().getName());
        // Con Spring Boot por defecto verás: ch.qos.logback.classic.Logger (es decir, Logback).
        // Si mañana se cambia Logback por Log4j2 en el pom.xml, esa línea cambia... y este código NO.

        log.info("Mensaje de prueba. Las llaves {} se reemplazan por los argumentos, en orden: {}", "así", 42);
    }

    @Test
    void conElMismoNombreSiempreSeObtieneElMismoLogger() {
        // La fábrica guarda los loggers por nombre y devuelve siempre el mismo: no se crea uno nuevo por cada pedido.
        assertSame(LoggerFactory.getLogger("demo"), LoggerFactory.getLogger("demo"));
    }
}
