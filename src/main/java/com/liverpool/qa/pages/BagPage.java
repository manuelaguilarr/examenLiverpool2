package com.liverpool.qa.pages;

import com.liverpool.qa.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BagPage extends BasePage {

    private static final String URL_BOLSA = "https://www.liverpool.com.mx/tienda/cart";

    // Localizador directo del botón "Comprar" en el resumen de pago de la bolsa
    private By btnComprar = By.xpath("//button[@data-testid='checkout-payment-summary-button']");

    public BagPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Verifica que el botón "Comprar" del resumen de bolsa esté visible y habilitado.
     */
    public boolean esBotonComprarVisible() {
        try {
            if (!driver.getCurrentUrl().contains("cart")) {
                driver.get(URL_BOLSA);
            }
            WebElement btn = waitForVisibility(btnComprar);
            return btn.isDisplayed() && btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Centra el botón "Comprar" del resumen y dispara el inicio del checkout.
     */
    public void irAComprarAhora() {
        WebElement btn = waitForClickability(btnComprar);
        try {
            click(btnComprar);
        } catch (Exception e) {
            jsClick(btn);
        }
    }
}