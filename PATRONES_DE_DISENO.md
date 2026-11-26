# Patrones de diseño del proyecto: Singleton y Simple Logging Facade

## 0. ¿Qué es un patrón de diseño?
Es una **receta probada** para un problema que aparece una y otra vez al programar. No es código para copiar: es una *idea* con nombre,
así los programadores se entienden ("acá usé un Singleton") sin explicar todo de nuevo. Cada patrón tiene siempre tres partes:
**el problema, la solución y las consecuencias**. Así conviene exponerlos.

---

## 1. SINGLETON

### El problema
Hay datos que tienen que ser **uno solo** para todo el sistema. En el CU-04 son las reglas de las reseñas: calificación de 1 a 5,
comentario de hasta 500 caracteres y qué estados significan "compra concretada". Si cada clase las escribiera por su cuenta (o hiciera
`new ReglasResena()`), podrían quedar valores distintos en distintos lugares, y un cambio habría que hacerlo en muchos sitios.

### La solución
Que **la propia clase** garantice que existe una única instancia. Son 3 reglas (están numeradas en `CU_34/config/ReglasResena.java`):

| # | Regla | En el código |
|---|---|---|
| 1 | Un atributo `static` guarda LA instancia | `private static final ReglasResena INSTANCIA = new ReglasResena();` |
| 2 | Constructor `private`: nadie puede hacer `new` | `private ReglasResena() { ... }` |
| 3 | Un método `static` público la entrega | `public static ReglasResena getInstancia()` |

Uso: `ReglasResena.getInstancia().esCalificacionValida(7)`. Además la clase es `final` para que nadie herede de ella y se saltee el constructor privado,
y la lista de estados es inmodificable (`List.of`) para que nadie altere la regla compartida desde afuera.

### Dónde se usa
- `ResenaService` pide la instancia (`private final ReglasResena reglas = ReglasResena.getInstancia();`) y le pregunta si la calificación y el comentario son válidos.
- Los repositorios reciben los estados "concretada" por parámetro, desde el Singleton: **un solo lugar** donde cambiarlos.

### Singleton GoF vs singleton de Spring (pregunta muy probable)
| | Singleton GoF (nuestra `ReglasResena`) | Singleton de Spring (`@Service`, `@Repository`...) |
|---|---|---|
| Quién garantiza una sola instancia | **La propia clase** (constructor privado) | **El contenedor de Spring** (scope por defecto) |
| ¿Se puede hacer `new`? | No, es privado | Sí, podrías (Spring simplemente no lo hace) |
| Alcance | Una por JVM / classloader | Una por contenedor de Spring |
| Cómo se obtiene | `Clase.getInstancia()` | Inyección en el constructor |

Los dos persiguen lo mismo ("una sola instancia compartida"), pero con mecanismos distintos. En este proyecto aparecen ambos: los services y repositories son
singletons de Spring, y `ReglasResena` es el patrón GoF implementado a mano.

---

## 2. SIMPLE LOGGING FACADE (SLF4J)

### El problema
Hay distintas bibliotecas que pueden escribir registros de ejecución (logs). Cada una tiene sus propias clases y métodos. Si el código de negocio
dependiera directamente de una biblioteca concreta, cambiarla podría obligar a modificar muchas clases.

### La solución: una fachada
SLF4J ofrece una API común entre el código de la aplicación y la biblioteca que escribe los logs. En el código se usan `org.slf4j.Logger` y
`LoggerFactory`; una implementación compatible, elegida entre las dependencias del proyecto, se encarga de producir la salida.

```
Código de la aplicación      API común                 Implementación elegida       Destinos
Controller / Service  ───▶  SLF4J: Logger       ───▶  Log4j2                 ───▶  consola
                            LoggerFactory             (Log4jLogger)                 logs/demo.log
```

La fachada reduce el acoplamiento: las clases de negocio hablan con SLF4J en vez de importar directamente clases de Log4j2. La biblioteca real
se selecciona y configura por separado. Cambiarla requiere modificar las dependencias/configuración, pero no la forma en que el código usa `Logger`.

### Cómo está implementado en este proyecto
**1) Las clases solicitan un logger a `LoggerFactory`:**
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(ResenaService.class);
```
- El tipo declarado es la interfaz de SLF4J; el código no declara un logger de Log4j2.
- Se pasa la clase para identificar el origen del mensaje.
- `static final` mantiene un logger asociado a la clase.
- En CU_12 y en `GlobalExceptionHandler` se usa Lombok `@Slf4j`, que genera un logger de SLF4J.

**2) La implementación elegida es Log4j2.** En `pom.xml` se agrega `spring-boot-starter-log4j2` y se excluye el logging por defecto de Spring
Boot en los starters correspondientes. Así se evita mezclar implementaciones. En la prueba actual, la clase concreta que expone el adaptador es
`org.apache.logging.slf4j.Log4jLogger`.

**3) La configuración de salida está en `src/main/resources/log4j2-spring.xml`.** Los logs de la aplicación (`com.aydsii`) se configuran en
`DEBUG`; se envían a la consola y al archivo `logs/demo.log`. Log4j2 se ocupa del formato y de la rotación del archivo. Esa configuración es
distinta de la fachada: SLF4J define cómo llama el código al logger; Log4j2 decide cómo y dónde se escribe.

**4) Los niveles expresan la importancia:** `debug` para información de diagnóstico; `info` para eventos relevantes del flujo; `warn` para
situaciones esperables o errores del pedido; y `error` para fallas del sistema. Por ejemplo, los controllers registran la llegada de una petición
en `debug`, los services informan acciones del caso de uso en `info`, y el manejador global registra errores de cliente con `warn` y fallas
inesperadas o de infraestructura con `error`.

**5) Los mensajes pueden recibir valores mediante `{}`**, por ejemplo `log.info("CU-03 - Cliente {}", idCliente)`. Esto evita construir el texto
con concatenaciones y deja los valores en su lugar correspondiente. Al pasar una excepción como último argumento a `log.error`, el backend puede
incluir su detalle y la traza.

### Cómo demostrarlo
El test `src/test/java/com/aydsii/demo/CU_34/logging/LoggingFacadeTest.java` es una prueba pequeña de Java; no necesita levantar Spring ni acceder
a la base de datos. Ejecutarlo desde la carpeta del proyecto:
```powershell
.\mvnw.cmd -Dtest=LoggingFacadeTest test
```
En la salida se muestran la interfaz que usa el código (`org.slf4j.Logger`) y la clase concreta proporcionada en esta configuración
(`org.apache.logging.slf4j.Log4jLogger`), además de un mensaje `INFO` de ejemplo. El test también verifica que `Logger` es una interfaz y que
pedir dos veces el logger con el mismo nombre devuelve el mismo objeto. Esto demuestra el uso de la fachada en esta ejecución; no demuestra por sí
solo cómo sería cambiar la implementación.

Para completar la explicación, se pueden señalar tres lugares: los imports `org.slf4j` en las clases CU_34, `spring-boot-starter-log4j2` en
`pom.xml` y los destinos de salida en `log4j2-spring.xml`.

### Guion para explicarlo con palabras simples
> “Para registrar lo que va pasando, el código no escribe directamente contra Log4j2. Usa SLF4J, que es como un idioma común o una capa intermedia:
> las clases piden un `Logger` y registran mensajes con niveles como `debug`, `info`, `warn` y `error`. En este proyecto, Log4j2 es quien recibe esos
> mensajes y los muestra en la consola y en `logs/demo.log`. Lo puedo mostrar ejecutando el test: imprime la interfaz SLF4J y la clase concreta que
> está detrás. Así se ve que el código depende de la fachada y la salida queda a cargo de la implementación configurada.”

## 3. Dependencias entre módulos (útil para la vista de Módulos del documento de arquitectura)
```
        ┌──────────────┐   usa (modelos y repositorios compartidos)     ┌──────────────┐
        │    CU_12     │ ──────────────────────────────────────────────▶│    CU_34     │
        │ Comprar /    │                                                │ Historial /  │
        │ Estado pedido│                                                │ Reseñas      │
        └──────────────┘                                                └──────────────┘
                      GlobalExceptionHandler (común) atiende ambos módulos
 Cada módulo tiene sus capas: controller → service → repository → model (+ dto, exception, config)
```
