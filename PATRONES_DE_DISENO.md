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
Existen varias librerías para escribir logs (Logback, Log4j2, java.util.logging), cada una con **su propia API**. Si el código usara directamente una,
quedaría atado a ella: cambiarla obligaría a reescribir todas las líneas de log del proyecto. Además, las librerías que uno usa (Spring, Hibernate)
vienen con la suya y habría varios logs mezclados.

### La solución: una FACHADA (patrón *Facade*)
Una **fachada** es una interfaz simple que se interpone entre el cliente y un subsistema complejo o intercambiable. SLF4J ("Simple Logging **Facade** for Java")
es esa interfaz: define **qué** se puede hacer (`info`, `warn`, `error`...) pero **no cómo**. Quién escribe de verdad se decide aparte, por configuración.

```
 NUESTRO CÓDIGO                 FACHADA                    IMPLEMENTACIÓN REAL            SALIDA
┌────────────────────┐   ┌───────────────────┐   ┌───────────────────────────┐   ┌──────────────┐
│ HistorialCompras-  │   │  slf4j-api        │   │  logback-classic          │   │ consola      │
│ Service            │──▶│  org.slf4j.Logger │──▶│  (el "binding" que trae   │──▶│ archivo      │
│ ResenaService ...  │   │  LoggerFactory    │   │   Spring Boot)            │   │ logs/        │
└────────────────────┘   └───────────────────┘   └───────────────────────────┘   └──────────────┘
   solo conoce esto  ─────────┘                    ◀── se puede cambiar (p. ej. por Log4j2) sin tocar el código
```
La ventaja clave: **para cambiar Logback por Log4j2 se cambia una dependencia en el `pom.xml`; ni una línea de código Java**.
(Dato fino: Logback implementa la interfaz de SLF4J directamente; otros, como Log4j2, necesitan una pieza intermedia que la adapte.)

### Cómo está implementado en el proyecto
**1) Cada clase pide su logger a la fábrica** (esto es lo único que "sabe" nuestro código):
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(ResenaService.class);
```
- Fijate los `import`: **solo `org.slf4j`**. En ninguna clase aparece `ch.qos.logback`. Eso es la fachada.
- Se pasa la clase para que el log muestre quién lo escribió (`...CU_34.service.ResenaService - mensaje`).
- `private static final`: un logger por clase, creado una vez.
- Lombok `@Slf4j` (que usás en CU_12) genera **exactamente esa línea** por vos. En CU_34 la dejé escrita para que se vea qué hay detrás.

**2) Se escribe con niveles**, según la gravedad:
| Nivel | Cuándo | Ejemplo en CU_34 |
|---|---|---|
| `debug` | Detalle técnico para desarrollar | `GET /api/clientes/{}/pedidos`, "Compra concretada: true" |
| `info` | Hechos importantes del negocio | "Reseña 7 registrada (cliente 1, producto 3)" |
| `warn` | Algo anómalo causado por el **cliente** (4xx) | "Producto no reseñable: ya reseñaste..." |
| `error` | Falla del **sistema** (5xx) | "Falla al registrar la reseña" + stack trace |

**3) Mensajes parametrizados:** `log.info("Reseña {} registrada", id)` en lugar de `"Reseña " + id + " registrada"`. Las llaves `{}` se reemplazan en orden,
y si el nivel está desactivado la concatenación ni siquiera se hace (más eficiente).

**4) Las excepciones van como último argumento:** `log.error("Falla: {}", ex.getMessage(), ex)` imprime el mensaje y el stack trace completo.

**5) Se loguea una sola vez, en el borde:** los errores se registran en `GlobalExceptionHandler` (infraestructura común), no en cada `catch` del service. "Loguear y relanzar"
es un anti-patrón: el mismo error saldría duplicado en el log.

**6) El nivel se cambia por configuración, sin tocar código** (`application.properties`):
```
logging.level.com.aydsii.demo.CU_34=DEBUG
```
La salida (formato, archivo en `logs/`) la define la configuración de Logback del proyecto, no las clases.

### Guion para explicarlo "con tus palabras" (1 minuto)
> "SLF4J es una fachada de logging: una interfaz común, `Logger`, que oculta qué librería escribe realmente los logs. En mi código solo importo `org.slf4j`,
> pido el logger con `LoggerFactory.getLogger(MiClase.class)` y escribo con `info`, `warn` o `error` según la gravedad. Spring Boot trae Logback como
> implementación, pero si mañana quisiera Log4j2 cambiaría una dependencia del `pom.xml` y no tocaría el código. Registro los errores una sola vez, en el
> manejador de excepciones, con `warn` si el error es del cliente y `error` si es del sistema, para no duplicar mensajes."

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
