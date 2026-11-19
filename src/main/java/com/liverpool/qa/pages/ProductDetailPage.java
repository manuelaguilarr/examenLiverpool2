package com.liverpool.qa.pages;

import com.liverpool.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ProductDetailPage extends BasePage {

    private By btnAgregarABolsa = By.xpath("//button[@data-testid='add-to-bag-button']");

    // Excluye tallas deshabilitadas o con cursor bloqueado para evitar clics en opciones agotadas
    private By primeraTallaDisponible = By.xpath("(//label[contains(@for, 'size-picker') and not(contains(@class, 'cursor-not-allowed')) and .//input[not(@disabled)]])[1]");
    private By btnSelectorTamano = By.xpath("//button[contains(@data-testid, 'selection-button-simple-picker')]");
    private By opcionesListaTamano = By.xpath("//span[contains(normalize-space(), 'cm')]");
    // Contador numérico sobre el ícono de la bolsa en el header
    private By contadorBolsa = By.xpath("//*[@data-testid='blt26617d4f2e17657d-header-shopping-cart-header-cart-quantity']");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
    }

    // Centra el elemento en el viewport para reducir intercepciones por headers fijos o banners
    private void scrollAlCentro(WebElement elemento) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", elemento);
    }

    /**
     * Resuelve la selección de talla/tamaño según el componente renderizado en el PDP;
     * si el producto no maneja variantes de talla, continúa sin interrumpir el flujo.
     */
    public void seleccionarTallaSiAplica() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));

        // Variante 1: botones directos de talla (size-picker)
        try {
            WebElement labelTalla = shortWait.until(ExpectedConditions.presenceOfElementLocated(primeraTallaDisponible));
            scrollAlCentro(labelTalla);
            shortWait.until(ExpectedConditions.elementToBeClickable(labelTalla)).click();
            return;
        } catch (Exception ignored) {
            // No aplica selector directo; se evalúa menú desplegable
        }

        // Variante 2: dropdown de tamaño (ej. medidas en cm)
        try {
            WebElement btnTamano = shortWait.until(ExpectedConditions.elementToBeClickable(btnSelectorTamano));
            scrollAlCentro(btnTamano);
            btnTamano.click();

            List<WebElement> opcionesCm = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(opcionesListaTamano));

            for (WebElement opcion : opcionesCm) {
                if (opcion.isDisplayed() && opcion.isEnabled()) {
                    scrollAlCentro(opcion);
                    try {
                        opcion.click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opcion);
                    }
                    break;
                }
            }
        } catch (Exception ignored) {
            // Producto sin selección de talla/tamaño
        }
    }

    /**
     * Espera a que el botón de agregar esté listo, lo centra en pantalla y añade el producto a la bolsa.
     */
    public void agregarABolsa() {
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(btnAgregarABolsa));
        scrollAlCentro(btn);
        btn.click();
    }

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
     * Agrega el producto a la bolsa y espera a que el contador del header incremente.
     */
    public boolean agregarABolsaYConfirmar() {
        int cantidadAnterior = obtenerCantidadEnBolsa();

        WebElement btn = waitForClickability(btnAgregarABolsa);
        scrollToCenter(btn);
        btn.click();

        try {
            return waitSeconds(8).until(d -> obtenerCantidadEnBolsa() > cantidadAnterior);
        } catch (Exception e) {
            return false;
        }
    }
}