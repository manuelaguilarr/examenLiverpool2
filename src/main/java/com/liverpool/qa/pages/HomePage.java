package com.liverpool.qa.pages;

import com.liverpool.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {

    // Localizadores principales del Home y encabezado
    private By btnAceptarCookies = By.xpath(
            "//button[contains(translate(., 'ACEPTAR', 'aceptar'), 'aceptar') or @id='onetrust-accept-btn-handler']"
    );
    private By btnCategorias = By.xpath(
            "//span[normalize-space()='Categorías'] | //button[contains(., 'Categorías')] | //*[@data-testid='blt26617d4f2e17657d-header-categories-button']"
    );
    private By primerProductoListado = By.xpath(
            "(//a[.//img[contains(@data-testid, '-image-slider-image-')]] | " +
                    "//img[contains(@data-testid, '-image-slider-image-0')])[1]"
    );
    private By iconoBolsaHeader = By.xpath(
            "//*[@data-testid='blt26617d4f2e17657d-header-shopping-cart-header-cart-quantity']/ancestor::a | " +
                    "//*[@data-testid='blt26617d4f2e17657d-header-shopping-cart-header-cart-quantity']/parent::* | " +
                    "//a[contains(@href,'/cart')]"
    );

    public HomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Cierra el banner de cookies si aparece en pantalla; si no existe, continúa sin fallar.
     */
    public void cerrarBannerCookiesSiExiste() {
        try {
            WebElement btnCookies = waitSeconds(3).until(ExpectedConditions.elementToBeClickable(btnAceptarCookies));
            btnCookies.click();
        } catch (Exception ignored) {
            // El banner no se mostró en esta sesión
        }
    }

    /**
     * Despliega el menú principal y navega hacia la categoría y subcategoría indicadas.
     * Lanza IllegalArgumentException si alguna no existe en el menú desplegado (soporte CN-01).
     */
    public void navegarPorCategoria(String categoria, String subcategoria) {
        WebElement elMenuCategorias = waitForClickability(btnCategorias);
        scrollToCenter(elMenuCategorias);
        try {
            elMenuCategorias.click();
        } catch (Exception e) {
            jsClick(elMenuCategorias);
        }

        By locatorCategoria = By.xpath("//*[normalize-space(text())='" + categoria + "']");
        try {
            waitSeconds(4).until(ExpectedConditions.visibilityOfElementLocated(locatorCategoria));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "CN-01 Error de navegación: La categoría '" + categoria + "' no existe en el menú principal."
            );
        }

        hover(locatorCategoria);

        By locatorSubcategoria = By.xpath("//*[normalize-space(text())='" + subcategoria + "']");
        try {
            WebElement elSubcategoria = waitSeconds(4).until(ExpectedConditions.elementToBeClickable(locatorSubcategoria));
            scrollToCenter(elSubcategoria);
            try {
                elSubcategoria.click();
            } catch (Exception ex) {
                jsClick(elSubcategoria);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "CN-01 Error de navegación: La subcategoría '" + subcategoria + "' no existe dentro de '" + categoria + "'."
            );
        }
    }

    /**
     * Selecciona el primer producto disponible en la cuadrícula de resultados de la subcategoría.
     */
    /**
     * Selecciona el primer producto disponible en la cuadrícula de resultados de la subcategoría.
     */
    public void seleccionarPrimerProducto() {
        String urlSubcategoria = driver.getCurrentUrl();
        waitSeconds(6).until(d -> !d.getCurrentUrl().equals("https://www.liverpool.com.mx/tienda/home"));

        WebElement producto = waitForClickability(primerProductoListado);
        scrollToCenter(producto);
        try {
            producto.click();
        } catch (Exception e) {
            jsClick(producto);
        }

        // Respaldo por si el clic recayó en la <img> y React delega el evento al contenedor padre
        try {
            waitSeconds(3).until(d -> !d.getCurrentUrl().equals(urlSubcategoria));
        } catch (Exception ignored) {
            WebElement reintento = waitForVisibility(primerProductoListado);
            jsClick(reintento);
        }
    }
    /**
     * Navega hacia la vista de bolsa/carrito desde el header.
     */
    public void irABolsa() {
        WebElement bolsa = waitForClickability(iconoBolsaHeader);
        scrollToCenter(bolsa);
        try {
            bolsa.click();
        } catch (Exception e) {
            jsClick(bolsa);
        }
    }
}