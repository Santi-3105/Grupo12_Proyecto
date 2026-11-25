# CU-03 y CU-04 en la carpeta `CU_34`

## 1. Dónde queda cada archivo (`src/main/java/com/aydsii/demo/`)

```
CU_34/                                   <- módulo nuevo, misma estructura que CU_12
 ├── config/      ReglasResena.java                     PATRÓN SINGLETON
 ├── controller/  HistorialComprasController.java, ResenaController.java
 ├── dto/         ApiResponse, PedidoResumenResponseDTO, PedidoDetalleResponseDTO, DetallePedidoResponseDTO,
 │                ProductoResenableResponseDTO, ResenaRequestDTO, ResenaResponseDTO
 ├── exception/   excepciones de reseñas, PedidoNoEncontradoException, ProductoNoEncontradoException,
 │                StockInsuficienteException
 ├── model/       modelos compartidos por CU_12 y CU_34, más Resena.java
 ├── repository/  ClienteRepository, PedidoRepository, ProductoRepository, ResenaRepository
 └── service/     HistorialComprasService.java, ResenaService.java
exception/GlobalExceptionHandler.java   <- manejador común de errores de ambos módulos
```
Y en `src/test/java/com/aydsii/demo/CU_34/`: `config/ReglasResenaTest.java` y `logging/LoggingFacadeTest.java`.

Los modelos y repositorios que comparten ambos casos de uso viven en CU_34. CU_12 los importa desde allí, de modo
que lo necesario para exponer CU_03/CU_04 quede agrupado en esa carpeta. Cada entidad JPA existe una sola vez:
no se duplican clases mapeadas a las mismas tablas. Los modelos propios del carrito permanecen en CU_12.

## 2. Modalidad de compra en `CU_34/model/Pedido.java`
`Pedido` ya incluye el campo persistido `modalidadCompra`, con `WEB` como valor inicial para los pedidos creados
por CU_12. Lombok genera sus accesores. El valor por defecto de la columna también permite actualizar una base
que ya tenga pedidos existentes:

```java
    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad_compra", nullable = false,
            columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'WEB'")
    private ModalidadCompra modalidadCompra = ModalidadCompra.WEB;
```

Con tu `ddl-auto=update`, al arrancar Hibernate crea la tabla `resenas` y la columna `pedidos.modalidad_compra`
si todavía no existen.

## 3. Dependencias entre casos de uso
- CU_34 contiene los modelos, repositorios y excepciones compartidos que usa CU_12; cada entidad JPA se define una sola vez.
- CU_12 importa esos tipos desde `com.aydsii.demo.CU_34`. Sus DTOs, controladores, servicios y el modelo del carrito siguen en CU_12.
- `ApiResponse` está en `CU_34.dto` y ofrece `success(int, String, T)` y `error(int, String)`.
- Los estados de compra concretada (`ENTREGADO` y `RETIRADO`) se configuran en `CU_34/config/ReglasResena.java`.

## 4. Decisiones de diseño
- CU_12 importa de CU_34 los modelos, repositorios y excepciones compartidos, evitando duplicar entidades y beans.
- `PedidoRepository` y `ProductoRepository` incluyen las consultas usadas por ambos casos de uso.
- Las excepciones específicas de reseñas y las compartidas para pedidos/productos están en CU_34.
- `GlobalExceptionHandler` vive en el paquete común `com.aydsii.demo.exception` y maneja errores de ambos módulos.
  Para las rutas de CU_34 conserva el formato `ApiResponse`; las respuestas de error de CU_12 mantienen `ProblemDetail`.

## 5. Cómo probarlo
**Swagger (`/swagger-ui.html`)**
- CU-03: generá compras con `POST /api/pedidos/comprar`; luego `GET /api/clientes/{id}/pedidos` y `GET /api/clientes/{id}/pedidos/{idPedido}`.
  Pedido de OTRO cliente → 404. Cliente sin compras → 200 con lista vacía. Cliente inexistente/inactivo → 403.
- CU-04: poné una compra en estado `ENTREGADO` (phpMyAdmin → tabla `pedidos` → estado). Luego
  `GET /api/clientes/{id}/productos-resenables` y `POST /api/resenas` con `{"idCliente":1,"idProducto":1,"calificacion":5,"comentario":"Muy bueno"}` → 201.
  Repetirlo → 409. `calificacion: 9` o ausente → 400. Producto inexistente → 404.

**Tests de los patrones** (no necesitan base de datos): en VS Code abrí `ReglasResenaTest` y `LoggingFacadeTest` y apretá *Run Test*.
Para el patrón de logging, mirá la consola: imprime la interfaz que usa tu código y la clase real que hay por detrás.

**Logs:** para ver los mensajes DEBUG de este módulo sin tocar código, agregá a `application.properties`:
`logging.level.com.aydsii.demo.CU_34=DEBUG`

## 6. Verificación
`mvn test` ejecuta los tests unitarios, pero `DemoApplicationTests.contextLoads` necesita que MySQL esté iniciado y configurado.
Si no hay conexión a la base, ese test de contexto falla antes de validar el arranque completo.
