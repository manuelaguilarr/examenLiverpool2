package com.liverpool.qa.pages;

import com.liverpool.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class ProductDetailPage extends BasePage {

    private By btnAgregarABolsa = By.xpath("//button[@data-testid='add-to-bag-button']");

    // Excluye tallas deshabilitadas o con cursor bloqueado para evitar clics en opciones agotadas
    private By primeraTallaDisponible = By.xpath("(//label[contains(@for, 'size-picker') and not(contains(@class, 'cursor-not-allowed')) and .//input[not(@disabled)]])[1]");
    private By btnSelectorTamano = By.xpath("//button[contains(@data-testid, 'selection-button-simple-picker')]");
    private By opcionesListaTamano = By.xpath("//span[contains(normalize-space(), 'cm')]");

    // Contador numérico sobre el ícono de la bolsa en el header
    private By contadorBolsa = By.xpath("//*[@data-testid='blt26617d4f2e17657d-header-shopping-cart-header-cart-quantity']");

    // Localizadores para CE-01 (Caso Edge: cantidad excedida > 100 piezas)
    private By inputCantidadPDP = By.xpath(
            "//input[@name='quantity' or @aria-label='cantidad' or contains(@data-testid,'-configurator-quantity-input')]"
    );
    private By spanMensajeCantidadExcedida = By.xpath(
            "//span[normalize-space()='Algunos artículos ya no están disponibles en la cantidad solicitada.'] | " +
                    "//span[contains(normalize-space(.), 'no están disponibles en la cantidad solicitada.')]"
    );

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    // =========================================================================
    // ACCIONES EN EL PDP
    // =========================================================================

    /**
     * Resuelve la selección de talla/tamaño según el componente renderizado en el PDP;
     * si el producto no maneja variantes de talla, continúa sin interrumpir el flujo.
     */
    public void seleccionarTallaSiAplica() {
        WebDriverWait shortWait = waitSeconds(4);

        // Variante 1: botones directos de talla (size-picker)
        try {
            WebElement labelTalla = shortWait.until(ExpectedConditions.presenceOfElementLocated(primeraTallaDisponible));
            clicSeguro(labelTalla);
            return;
        } catch (Exception ignored) {
            // No aplica selector directo; se evalúa menú desplegable
        }

        // Variante 2: dropdown de tamaño (ej. medidas en cm)
        try {
            WebElement btnTamano = shortWait.until(ExpectedConditions.elementToBeClickable(btnSelectorTamano));
            clicSeguro(btnTamano);

            List<WebElement> opcionesCm = waitSeconds(5)
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(opcionesListaTamano));

            for (WebElement opcion : opcionesCm) {
                if (opcion.isDisplayed() && opcion.isEnabled()) {
                    clicSeguro(opcion);
                    break;
                }
            }
        } catch (Exception ignored) {
            // Producto sin selección de talla/tamaño
        }
    }

    /**
     * Limpia y escribe la cantidad solicitada en el input controlado por React en el PDP.
     */
    public void ingresarCantidad(int cantidad) {
        WebElement inputCantidad = waitForClickability(inputCantidadPDP);
        scrollToCenter(inputCantidad);

        try {
            inputCantidad.click();
            inputCantidad.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            inputCantidad.sendKeys(Keys.BACK_SPACE);
            inputCantidad.sendKeys(String.valueOf(cantidad));
            inputCantidad.sendKeys(Keys.TAB);
        } catch (Exception e) {
            jsClick(inputCantidad);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                    inputCantidad,
                    String.valueOf(cantidad)
            );
        }
    }

    /**
     * Espera a que el botón de agregar esté listo, lo centra en pantalla y añade el producto a la bolsa.
     */
    public void agregarABolsa() {
        WebElement btn = waitSeconds(10).until(ExpectedConditions.elementToBeClickable(btnAgregarABolsa));
        clicSeguro(btn);
    }

    /**
     * Agrega el producto a la bolsa y espera a que el contador del header incremente.
     */
    public boolean agregarABolsaYConfirmar() {
        int cantidadAnterior = obtenerCantidadEnBolsa();
        agregarABolsa();

        try {
            return waitSeconds(8).until(d -> obtenerCantidadEnBolsa() > cantidadAnterior);
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // CONSULTAS Y SOPORTE CE-01
    // =========================================================================

    /**
     * Obtiene la cantidad actual reflejada en el ícono de la bolsa (0 si aún no se muestra badge).
     */
    public int obtenerCantidadEnBolsa() {
        try {
            String texto = waitSeconds(3)
                    .until(ExpectedConditions.visibilityOfElementLocated(contadorBolsa))
                    .getText()
                    .trim();
            return Integer.parseInt(texto.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Espera y retorna el texto del pop-up de disponibilidad cuando se excede la cantidad permitida.
     */
    public String obtenerMensajeCantidadExcedida() {
        try {
            WebElement spanMensaje = waitSeconds(6)
                    .until(ExpectedConditions.visibilityOfElementLocated(spanMensajeCantidadExcedida));
            return spanMensaje.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Flujo auxiliar para CE-01: establece una cantidad excedida, pulsa Agregar a bolsa
     * y captura el mensaje resultante del pop-up.
     */
    public String intentarAgregarCantidadYCapturarMensajeCE01(int cantidad) {
        ingresarCantidad(cantidad);
        agregarABolsa();
        return obtenerMensajeCantidadExcedida();
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    private void clicSeguro(WebElement elemento) {
        scrollToCenter(elemento);
        try {
            elemento.click();
        } catch (Exception e) {
            jsClick(elemento);
        }
    }
}