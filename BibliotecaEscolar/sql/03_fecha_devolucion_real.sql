-- La aplicación aplica automáticamente esta migración en el primer acceso.
-- Ejecutar manualmente SOLO si la columna todavía no existe.
USE biblioteca;
ALTER TABLE prestamos ADD COLUMN fecha_devolucion_real DATE NULL;
