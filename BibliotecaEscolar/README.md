# Biblioteca Escolar — Inger Frez
Aplicación de escritorio Java 17, Swing/JFrame y MySQL/JDBC para la EFT de Desarrollo Orientado a Objetos II.

## Inicio rápido (Windows)
1. Descomprime TODO el ZIP. Mantén juntos el JAR, `lib` y `config.properties`.
2. Ejecuta `iniciar.bat`. Necesitas Java 17 o superior disponible en PATH. También puedes usar `java -jar BibliotecaEscolar.jar` desde esta carpeta.
3. Cuenta bibliotecaria: `antonia@correo.cl` / `clave123`. Cuenta estudiante: `carlos@correo.cl` / `clave123` o ingerfrez@gmail.com clave 'Merlina2026.'

## IntelliJ IDEA
File → Open → selecciona la carpeta BibliotecaEscolar o su pom.xml → Open as Project si aparece. Selecciona JDK 17 o superior. Espera la importación de Maven y ejecuta `cl.duoc.biblioteca.Main` con el triángulo verde. El directorio de trabajo debe ser la carpeta del proyecto para leer config.properties.
Si Maven no descarga dependencias: File → Project Structure → Modules → Dependencies → + → JARs or directories → selecciona `lib/mysql-connector-j-8.4.0.jar`. El conector ya viene incluido.
Para recompilar sin Maven, ejecuta compilar.bat con un JDK 17 instalado. Con Maven: `mvn clean package`; el ejecutable con dependencias estará en target/.

## Configuración y datos
La columna original fecha_devolucion se interpreta como VENCIMIENTO porque los registros no devueltos ya la traen. La aplicación agrega automáticamente fecha_devolucion_real al iniciar. Los préstamos históricos ya devueltos no tienen fecha real conocida: queda NULL, sin inventar datos. Los registros abiertos de 2025 aparecerán atrasados; esto es correcto con la fecha actual.
El stock del script se interpreta como cantidad disponible; no se descuenta nuevamente al importar préstamos de ejemplo. Cada préstamo nuevo descuenta una unidad y cada devolución válida repone una.
El RUT vincula usuarios con estudiantes. Luis Gómez no tiene cuenta en el poblado: para habilitar su acceso, modifica su ficha e ingresa una contraseña. Las nuevas fichas crean su cuenta estudiante en la misma transacción. Una contraseña vacía al editar conserva la existente.

## Funcionalidades
- Autenticación y autorización: bibliotecario administra; estudiante ve catálogo, realiza sus préstamos/devoluciones y consulta su historial.
- CRUD completo de libros, estudiantes y categorías. Eliminar registros con relaciones activas/históricas se bloquea por integridad referencial, conservando el historial.
- Búsqueda textual en cada tabla, selección de categoría y stock no negativo.
- Préstamos con plazo configurable (7 días), devolución real y estado de atraso.

