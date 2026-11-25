-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 25-11-2026 a las 14:33:26
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `demo_aydsii_local`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carritos`
--

CREATE TABLE `carritos` (
  `id_carrito` bigint(20) NOT NULL,
  `estado` enum('ACTIVO','FINALIZADO') NOT NULL,
  `id_cliente` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `carritos`
--

INSERT INTO `carritos` (`id_carrito`, `estado`, `id_cliente`) VALUES
(1, 'FINALIZADO', 1),
(2, 'ACTIVO', 3),
(3, 'ACTIVO', 4);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `clientes`
--

CREATE TABLE `clientes` (
  `id_cliente` bigint(20) NOT NULL,
  `activo` bit(1) NOT NULL,
  `apellido` varchar(80) NOT NULL,
  `dni` varchar(20) NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `nombre` varchar(80) NOT NULL,
  `telefono` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `clientes`
--

INSERT INTO `clientes` (`id_cliente`, `activo`, `apellido`, `dni`, `email`, `fecha_nacimiento`, `nombre`, `telefono`) VALUES
(1, b'1', 'Pérez', '30111222', 'juan@mail.com', '1990-05-20', 'Juan', '2302111111'),
(2, b'0', 'Gómez', '28333444', 'maria@mail.com', '1985-03-10', 'María', '2302222222'),
(3, b'1', 'López', '35555666', 'carlos@mail.com', '1995-08-01', 'Carlos', '2302333333'),
(4, b'1', 'Díaz', '33777888', 'ana@mail.com', '1992-11-30', 'Ana', '2302444444');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalles_pedido`
--

CREATE TABLE `detalles_pedido` (
  `id_detalle` bigint(20) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(12,2) NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `id_pedido` bigint(20) NOT NULL,
  `id_producto` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `detalles_pedido`
--

INSERT INTO `detalles_pedido` (`id_detalle`, `cantidad`, `precio_unitario`, `subtotal`, `id_pedido`, `id_producto`) VALUES
(1, 1, 850000.00, 850000.00, 1, 1),
(2, 2, 12500.50, 25001.00, 1, 2);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `envios`
--

CREATE TABLE `envios` (
  `id_envio` bigint(20) NOT NULL,
  `calle` varchar(120) NOT NULL,
  `codigo_postal` varchar(15) NOT NULL,
  `codigo_seguimiento` varchar(80) DEFAULT NULL,
  `empresa_transporte` varchar(80) DEFAULT NULL,
  `fecha_despacho` date DEFAULT NULL,
  `fecha_entrega` date DEFAULT NULL,
  `fecha_estimada_entrega` date DEFAULT NULL,
  `localidad` varchar(80) NOT NULL,
  `numero` varchar(20) NOT NULL,
  `provincia` varchar(80) NOT NULL,
  `ubicacion_actual` varchar(120) DEFAULT NULL,
  `id_pedido` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `envios`
--

INSERT INTO `envios` (`id_envio`, `calle`, `codigo_postal`, `codigo_seguimiento`, `empresa_transporte`, `fecha_despacho`, `fecha_entrega`, `fecha_estimada_entrega`, `localidad`, `numero`, `provincia`, `ubicacion_actual`, `id_pedido`) VALUES
(1, 'San Martín', '6360', NULL, NULL, NULL, NULL, NULL, 'General Pico', '123', 'La Pampa', NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estados_pedido`
--

CREATE TABLE `estados_pedido` (
  `id_estado` bigint(20) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `estados_pedido`
--

INSERT INTO `estados_pedido` (`id_estado`, `descripcion`, `nombre`) VALUES
(1, 'Pedido creado, pendiente de confirmación', 'PENDIENTE'),
(2, 'Pedido confirmado y pagado', 'CONFIRMADO'),
(3, 'Pedido despachado', 'ENVIADO'),
(4, 'Pedido entregado al cliente', 'ENTREGADO'),
(5, 'Pedido cancelado', 'CANCELADO');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `items_carrito`
--

CREATE TABLE `items_carrito` (
  `id_item` bigint(20) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `id_carrito` bigint(20) NOT NULL,
  `id_producto` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `items_carrito`
--

INSERT INTO `items_carrito` (`id_item`, `cantidad`, `id_carrito`, `id_producto`) VALUES
(1, 1, 1, 1),
(2, 2, 1, 2),
(3, 50, 3, 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `medios_pago`
--

CREATE TABLE `medios_pago` (
  `id_medio_pago` bigint(20) NOT NULL,
  `descripcion` varchar(80) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `medios_pago`
--

INSERT INTO `medios_pago` (`id_medio_pago`, `descripcion`) VALUES
(1, 'Efectivo'),
(2, 'Tarjeta de crédito'),
(3, 'Transferencia bancaria');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos`
--

CREATE TABLE `pagos` (
  `id_pago` bigint(20) NOT NULL,
  `estado` enum('APROBADO','PENDIENTE') NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `monto` decimal(12,2) NOT NULL,
  `id_medio_pago` bigint(20) NOT NULL,
  `id_pedido` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `pagos`
--

INSERT INTO `pagos` (`id_pago`, `estado`, `fecha`, `monto`, `id_medio_pago`, `id_pedido`) VALUES
(1, 'APROBADO', '2026-11-25 10:23:19.000000', 875001.00, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedidos`
--

CREATE TABLE `pedidos` (
  `id_pedido` bigint(20) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `modalidad_compra` varchar(20) NOT NULL DEFAULT 'WEB',
  `tipo_entrega` enum('ENVIO','RETIRO') NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `id_cliente` bigint(20) NOT NULL,
  `id_estado` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `pedidos`
--

INSERT INTO `pedidos` (`id_pedido`, `fecha`, `modalidad_compra`, `tipo_entrega`, `total`, `id_cliente`, `id_estado`) VALUES
(1, '2026-11-25 10:23:19.000000', 'WEB', 'ENVIO', 875001.00, 1, 4);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id_producto` bigint(20) NOT NULL,
  `categoria` varchar(80) DEFAULT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` bit(1) NOT NULL,
  `nombre` varchar(120) NOT NULL,
  `precio` decimal(12,2) NOT NULL,
  `stock` int(11) NOT NULL,
  `version` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id_producto`, `categoria`, `descripcion`, `estado`, `nombre`, `precio`, `stock`, `version`) VALUES
(1, 'Computación', 'Notebook 8GB RAM / 256GB SSD', b'1', 'Notebook 15\"', 850000.00, 4, 1),
(2, 'Periféricos', 'Mouse óptico inalámbrico', b'1', 'Mouse inalámbrico', 12500.50, 8, 1),
(3, 'Periféricos', 'Teclado mecánico retroiluminado', b'1', 'Teclado mecánico', 45000.00, 8, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `resenas`
--

CREATE TABLE `resenas` (
  `id_resena` bigint(20) NOT NULL,
  `calificacion` int(11) NOT NULL,
  `comentario` varchar(500) DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  `id_cliente` bigint(20) NOT NULL,
  `id_producto` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `resenas`
--

INSERT INTO `resenas` (`id_resena`, `calificacion`, `comentario`, `fecha`, `id_cliente`, `id_producto`) VALUES
(1, 5, 'Muy buen producto', '2026-11-25 10:27:12.000000', 1, 1),
(2, 4, 'Muy buen producto 2.0', '2026-11-25 10:28:29.000000', 1, 2);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `carritos`
--
ALTER TABLE `carritos`
  ADD PRIMARY KEY (`id_carrito`);

--
-- Indices de la tabla `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id_cliente`);

--
-- Indices de la tabla `detalles_pedido`
--
ALTER TABLE `detalles_pedido`
  ADD PRIMARY KEY (`id_detalle`);

--
-- Indices de la tabla `envios`
--
ALTER TABLE `envios`
  ADD PRIMARY KEY (`id_envio`);

--
-- Indices de la tabla `estados_pedido`
--
ALTER TABLE `estados_pedido`
  ADD PRIMARY KEY (`id_estado`);

--
-- Indices de la tabla `items_carrito`
--
ALTER TABLE `items_carrito`
  ADD PRIMARY KEY (`id_item`);

--
-- Indices de la tabla `medios_pago`
--
ALTER TABLE `medios_pago`
  ADD PRIMARY KEY (`id_medio_pago`);

--
-- Indices de la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD PRIMARY KEY (`id_pago`);

--
-- Indices de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  ADD PRIMARY KEY (`id_pedido`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id_producto`);

--
-- Indices de la tabla `resenas`
--
ALTER TABLE `resenas`
  ADD PRIMARY KEY (`id_resena`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `carritos`
--
ALTER TABLE `carritos`
  MODIFY `id_carrito` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id_cliente` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `detalles_pedido`
--
ALTER TABLE `detalles_pedido`
  MODIFY `id_detalle` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `envios`
--
ALTER TABLE `envios`
  MODIFY `id_envio` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `estados_pedido`
--
ALTER TABLE `estados_pedido`
  MODIFY `id_estado` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `items_carrito`
--
ALTER TABLE `items_carrito`
  MODIFY `id_item` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `medios_pago`
--
ALTER TABLE `medios_pago`
  MODIFY `id_medio_pago` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `pagos`
--
ALTER TABLE `pagos`
  MODIFY `id_pago` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  MODIFY `id_pedido` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id_producto` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `resenas`
--
ALTER TABLE `resenas`
  MODIFY `id_resena` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
