# Biblioteca Escolar — Inger Frez
Aplicación de escritorio Java 17, Swing/JFrame y MySQL/JDBC para la EFT de Desarrollo Orientado a Objetos II.

## Inicio rápido (Windows)
1. Descomprime TODO el ZIP. Mantén juntos el JAR, `lib` y `config.properties`.
2. En XAMPP inicia MySQL (puerto 3307). Apache solo es necesario para phpMyAdmin.
3. Si ya importaste los scripts originales, NO los repitas. Si la base está vacía, importa primero el script de creación y luego el de poblamiento de `sql/`.
4. Ejecuta `iniciar.bat`. Necesitas Java 17 o superior disponible en PATH. También puedes usar `java -jar BibliotecaEscolar.jar` desde esta carpeta.
5. Cuenta bibliotecaria: `antonia@correo.cl` / `clave123`. Cuenta estudiante: `carlos@correo.cl` / `clave123`.

## IntelliJ IDEA
File → Open → selecciona la carpeta BibliotecaEscolar o su pom.xml → Open as Project si aparece. Selecciona JDK 17 o superior. Espera la importación de Maven y ejecuta `cl.duoc.biblioteca.Main` con el triángulo verde. El directorio de trabajo debe ser la carpeta del proyecto para leer config.properties.
Si Maven no descarga dependencias: File → Project Structure → Modules → Dependencies → + → JARs or directories → selecciona `lib/mysql-connector-j-8.4.0.jar`. El conector ya viene incluido.
Para recompilar sin Maven, ejecuta compilar.bat con un JDK 17 instalado. Con Maven: `mvn clean package`; el ejecutable con dependencias estará en target/.

## Configuración y datos
`config.properties` usa localhost:3307, base biblioteca, usuario root y contraseña vacía. Si tu configuración cambia, edita este archivo. Es configuración de desarrollo local; no publiques credenciales personales.
La columna original fecha_devolucion se interpreta como VENCIMIENTO porque los registros no devueltos ya la traen. La aplicación agrega automáticamente fecha_devolucion_real al iniciar. Los préstamos históricos ya devueltos no tienen fecha real conocida: queda NULL, sin inventar datos. Los registros abiertos de 2025 aparecerán atrasados; esto es correcto con la fecha actual.
El stock del script se interpreta como cantidad disponible; no se descuenta nuevamente al importar préstamos de ejemplo. Cada préstamo nuevo descuenta una unidad y cada devolución válida repone una.
El RUT vincula usuarios con estudiantes. Luis Gómez no tiene cuenta en el poblado: para habilitar su acceso, modifica su ficha e ingresa una contraseña. Las nuevas fichas crean su cuenta estudiante en la misma transacción. Una contraseña vacía al editar conserva la existente.

## Funcionalidades
- Autenticación y autorización: bibliotecario administra; estudiante ve catálogo, realiza sus préstamos/devoluciones y consulta su historial.
- CRUD completo de libros, estudiantes y categorías. Eliminar registros con relaciones activas/históricas se bloquea por integridad referencial, conservando el historial.
- Búsqueda textual en cada tabla, selección de categoría y stock no negativo.
- Préstamos con plazo configurable (7 días), devolución real y estado de atraso.
- Reportes de préstamos activos, libros más prestados e historial por estudiante.

## Diseño para la pauta
- Modelo: Entidad y Persona abstractas; Usuario y Estudiante heredan Persona; Libro, Categoria y Prestamo heredan Entidad. valores() se sobrescribe y el DAO lo invoca de forma polimórfica.
- Interfaz CrudDAO y clase AbstractDAO; DAO específicos para las cinco entidades. El CRUD de préstamos existe en el DAO, pero la interfaz expone préstamo y devolución para proteger stock e historial.
- MVC: view contiene Swing; controller maneja eventos; service aplica reglas; dao es la única capa que ejecuta operaciones de datos (DatabaseConnection configura la conexión y migración).
- Singleton DatabaseConnection con getInstance() y una conexión compartida. Todos los DAO sincronizan sobre el mismo monitor; no cierran la conexión compartida al cerrar ResultSet/PreparedStatement.
- SwingWorker ejecuta JDBC fuera del EDT, y done() actualiza la interfaz en el EDT.
- prestar/devolver synchronized y transacciones JDBC con commit/rollback. UPDATE stock condicionado a stock>0 impide sobregiro incluso entre procesos distintos. Devolución bloquea la fila con FOR UPDATE y evita reponer dos veces.
- La tabla prestamos es el historial, conservado después de devolver; no se duplica en otra tabla.
- PreparedStatement parametriza valores y reduce riesgo de inyección.
- Las contraseñas se conservan en texto tal como las entrega el script del ramo para compatibilidad académica. Para un despliegue real se requiere migración a hashes, credenciales restringidas y conexión cifrada.

## GitHub y entrega
Crea un repositorio público BibliotecaEscolar. Sube esta carpeta (excepto target y .idea). Incluye código, SQL, README, config de ejemplo, ejecutable y conector. Revisa que no existan contraseñas personales. Usa commits descriptivos, por ejemplo: "Implementar modelos y DAO", "Integrar préstamos transaccionales y roles", "Documentar ejecución y pruebas". No afirmes una historia de commits que no realizaste.
Entrega el ZIP, enlace público del repositorio y video Kaltura de 3–5 minutos. Ver GUIÓN_VIDEO.md y PRUEBAS.md.
