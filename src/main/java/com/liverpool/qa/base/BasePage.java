package com.liverpool.qa.base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        // Espera explícita por defecto para acciones estándar del flujo
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // =========================================================================
    // ESPERAS EXPLÍCITAS
    // =========================================================================

    // Espera corta puntual (banners, variantes opcionales) sin saturar el timeout global
    protected WebDriverWait waitSeconds(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickability(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // =========================================================================
    // NAVEGACIÓN EN VIEWPORT E INTERACCIONES
    // =========================================================================

    // Centra el elemento en pantalla para evitar intercepciones de headers o overlays
    protected void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    protected void click(By locator) {
        WebElement element = waitForClickability(locator);
        scrollToCenter(element);
        element.click();
    }

    protected void jsClick(WebElement element) {
        scrollToCenter(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    // Respaldo cuando un componente React intercepta el clic nativo de WebDriver
    protected void jsClick(By locator) {
        jsClick(waitForClickability(locator));
    }

    protected void hover(By locator) {
        WebElement element = waitForVisibility(locator);
        scrollToCenter(element);
        new Actions(driver).moveToElement(element).perform();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        scrollToCenter(element);
        element.clear();
        element.sendKeys(text);
    }

    protected void pressEnter(By locator) {
        waitForVisibility(locator).sendKeys(Keys.ENTER);
    }
}