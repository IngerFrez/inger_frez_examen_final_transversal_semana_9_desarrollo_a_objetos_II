# Verificación funcional en tu computador
Antes de grabar, prueba con datos temporales identificables (por ejemplo "Libro de prueba EFT"). No elimines tus datos originales.

| Caso | Resultado esperado |
|---|---|
| Antonia / clave123 | Accede a todos los módulos |
| Contraseña incorrecta | Rechaza acceso sin abrir ventana principal |
| Crear/modificar/eliminar categoría sin libros | Persistencia tras actualizar y reiniciar |
| Crear/modificar/eliminar libro sin préstamos | Persistencia, categoría correcta, stock no negativo |
| ISBN duplicado / stock negativo / campos vacíos | Rechazo con mensaje |
| Crear estudiante con contraseña | Crea ficha y cuenta con el mismo RUT |
| Editar ficha y dejar clave vacía | Conserva acceso y contraseña |
| Eliminar estudiante sin préstamos | Elimina ficha y cuenta estudiante |
| Eliminar libro/categoría/estudiante relacionado | Bloquea eliminación, conserva datos |
| Préstamo nuevo | Reduce stock en 1, vencimiento a 7 días, historial persistente |
| Devolver | Aumenta stock en 1 y registra fecha real |
| Intentar segunda devolución | No incrementa stock nuevamente |
| Stock 0 | No permite préstamo |
| Carlos | Solo ve/opera sus préstamos; no aparece CRUD administrativo |
| Reportes | Ranking, historial elegido, activos |
| Préstamos abiertos de 2025 | Estado atrasado |
| Buscar | Filtra la tabla y conserva selección/ID correcto |
| MySQL detenido | Error controlado; al reiniciar permite reintentar |

## Concurrencia entre aplicaciones
Crea un libro temporal con stock 1 y abre dos instancias del programa. En ambas prepara un préstamo del mismo libro y confírmalo casi al mismo tiempo. Solo una debe registrar préstamo; la otra informa falta de stock. Al actualizar ambas, stock 0 y un solo préstamo nuevo. Prueba devolver el mismo préstamo desde ambas: solo una devuelve, stock final 1.
Prueba usando un libro sin préstamos previos para que el resultado sea inequívoco.

## Alcance de la verificación entregada
Consulta VALIDACION.md para las comprobaciones efectivamente ejecutadas durante la preparación. La conexión a tu XAMPP requiere validación local.
