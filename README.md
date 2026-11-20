Automatización QA - Flujo de Compra Liverpool (Selenium + TestNG)
Este proyecto es un framework de automatización de pruebas funcionales desarrollado en Java utilizando Selenium WebDriver y TestNG bajo el patrón de diseño Page Object Model (POM).
Su objetivo principal es validar de forma rápida, repetible y confiable el flujo crítico de selección y agregado de productos a la bolsa de compras en el portal de Liverpool, cubriendo escenarios positivos, negativos y de frontera (edge cases).
---
Estructura del Proyecto
El proyecto separa la lógica de interacción con la página web de las validaciones de negocio:
```text
src/
 ├── main/java/com/liverpool/qa/
 │    ├── base/
 │    │    └── BasePage.java            # Núcleo técnico: esperas explícitas, scroll y clics seguros
 │    └── pages/
 │         ├── HomePage.java            # Navegación por categorías, cookies y selección en catálogo
 │         ├── ProductDetailPage.java   # Selección de tallas, control de cantidades y alertas de stock
 │         └── BagPage.java             # Validación del resumen de bolsa e inicio de checkout
 └── test/java/com/liverpool/qa/
      └── tests/
           └── LiverpoolCheckoutTest.java # Suite de pruebas (Casos de Flujo, Negativos y Edge)
```
---
Cobertura de Pruebas
La suite está organizada en tres niveles de validación:
CF-01 (Casos de Flujo): Valida de punta a punta la navegación por categoría y subcategoría, selección del primer artículo, elección de variante/talla (si aplica), agregado a la bolsa con verificación de incremento en el contador y disponibilidad del botón "Comprar".
CN-01 (Casos Negativos): Verifica que el sistema y el framework respondan de manera controlada ante categorías o subcategorías inexistentes en el menú principal.
CE-01 (Casos Edge / Frontera): Evalúa las reglas de límite de inventario intentando agregar más de 100 unidades de un mismo artículo y validando el mensaje de advertencia en pantalla.
---
Requisitos Previos
Antes de ejecutar el proyecto, asegúrate de contar con:
Java JDK 17 o superior configurado en tus variables de entorno (`JAVA\_HOME`).
Apache Maven instalado (o integrado desde IntelliJ IDEA).
Google Chrome actualizado.
IntelliJ IDEA (recomendado para ejecución y gestión de Git/PR).
---
Configuración de Sesión Persistente (Paso a Paso)
Este proyecto utiliza un perfil dedicado de Google Chrome (`user-data-dir`) para mantener una sesión persistente. Esto aísla las pruebas de tu navegador personal, reduce fricciones con controles anti-bot de e-commerce y conserva preferencias básicas de navegación.
1. Crear la carpeta del perfil en tu equipo
Crea el siguiente directorio en tu disco local (si no existe):
Windows:
```cmd
  mkdir C:\\selenium-profiles\\liverpool-qa
  ```
  ```
2. Inicializar el perfil manualmente (Primera vez)
Para que Chrome genere su estructura interna de archivos y te permita aceptar preferencias iniciales si es necesario, ábrelo una primera vez apuntando a esa carpeta:
En Windows (CMD o PowerShell):
```cmd
  "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe" --user-data-dir="C:\\selenium-profiles\\liverpool-qa"
  ```
Una vez que abra la ventana limpia de Chrome:
Navega a `https://www.liverpool.com.mx/tienda/home`.
Acepta el banner de cookies si aparece y verifica que la tienda cargue con normalidad.
Cierra completamente esa ventana de Chrome antes de ejecutar las pruebas automatizadas (Chrome bloquea el perfil si dos instancias intentan usarlo al mismo tiempo).
3. Verificar la ruta en `LiverpoolCheckoutTest.java`
Confirma que en el método `@BeforeMethod` de `LiverpoolCheckoutTest.java` la ruta coincida con el directorio que creaste:
```java
ChromeOptions options = new ChromeOptions();
options.addArguments("user-data-dir=C:\\\\selenium-profiles\\\\liverpool-qa");
options.addArguments("--disable-blink-features=AutomationControlled");
```
Ejecución de las Pruebas
Desde IntelliJ IDEA
Abre el proyecto y espera a que Maven descargue las dependencias del `pom.xml`.
Navega a `src/test/java/com/liverpool/qa/tests/LiverpoolCheckoutTest.java`.
Haz clic en el ícono verde de Run junto al nombre de la clase para ejecutar toda la suite, o junto a cada `@Test` para correr un escenario individual.
Desde Terminal (Maven)
```bash
mvn clean test
```
