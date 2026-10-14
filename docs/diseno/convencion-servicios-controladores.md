# Convención de servicios y controladores

Todos los servicios (`XServicio`, `XServicioImpl`) y controladores (`XControlador`) ordenan sus métodos en
**cuatro bloques**, siempre en este orden. Cada bloque empieza con un comentario de sección:

```java
// ---------------------------------------------------------------- Lecturas
// ---------------------------------------------------------------- Creaciones
// ---------------------------------------------------------------- Modificaciones
// ---------------------------------------------------------------- Bajas
```

Solo se escriben los bloques que tienen métodos.

## Servicios

| Bloque | Qué va | Ejemplos |
|---|---|---|
| **Lecturas** | Consultas que no cambian datos, incluidos los cálculos. | `buscarPorId`, `obtenerTodo`, `existePorId`, `buscarPorX`, `buscarConFiltros`, `calcularMontoAPagar`, `generarPdf` |
| **Creaciones** | Altas de registros nuevos. | `guardar`, `registrarX`, `inscribir`, `iniciarPago`, `emitirComprobante`, `crearSolicitud` |
| **Modificaciones** | Todo lo que cambia un registro existente o su estado. | `modificar`, `modificarX`, `reintentarPago`, `consultarEstado`, `procesarNotificacion`, `conciliarPagosPendientes`, `cambiarX`, `establecerPorDefecto` |
| **Bajas** | Bajas lógicas. | `borrar`, `darDeBajaX`, `darDeBajaVarios` |

Reglas:

1. Antes de los bloques van las constantes, los campos inyectados y los constructores.
2. Después de los bloques van los métodos privados (sin bloque propio) y, al final, los tipos anidados.
3. Dentro de cada bloque, primero van los métodos de `CrudServicio` (`buscarPorId`, `obtenerTodo`, `existePorId`,
   `guardar`, `modificar`, `borrar`) y después los del caso de uso, en el orden en que ya estaban.
4. La interfaz `XServicio` usa los mismos bloques y el mismo orden que su `XServicioImpl`.
5. El Javadoc de cada método público vive en la interfaz. Los métodos `@Override` de la implementación lo heredan;
   solo se repite si la implementación agrega algo propio.

## Controladores

| Bloque | Qué va |
|---|---|
| **Lecturas** | Los `@GetMapping` (vistas, consultas de estado, descarga de comprobantes). |
| **Creaciones** | Los `@PostMapping` que dan de alta (`/registrar`, `/inscribir`, `/pagar`, `/responder`, …). |
| **Modificaciones** | Los `@PostMapping` que cambian un registro (`/modificar/{id}`, `/reintentar`, `/webhook`, …). |
| **Bajas** | Los `@PostMapping("/darDeBaja…")`, individuales y masivos. |

Los métodos privados auxiliares van al final de la clase.

## Qué no cambia

Reordenar no modifica nombres, firmas ni comportamiento. Si un método nuevo no encaja claramente en un bloque, se
ubica en el del efecto principal que produce.
