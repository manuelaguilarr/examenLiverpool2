package com.liverpool.qa.tests;

import com.liverpool.qa.pages.BagPage;
import com.liverpool.qa.pages.HomePage;
import com.liverpool.qa.pages.ProductDetailPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LiverpoolCheckoutTest {

    private static final String URL_HOME = "https://www.liverpool.com.mx/tienda/home";

    // Parámetros configurables para Casos Edge (CE)
    private static final int CANTIDAD_EXCEDIDA_CE01 = 105;
    private static final String MENSAJE_ESPERADO_CE01 =
            "Algunos artículos ya no están disponibles en la cantidad solicitada.";

    private WebDriver driver;

    // =========================================================================
    // CICLO DE VIDA DE PRUEBA (SETUP / TEARDOWN)
    // =========================================================================

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=C:\\selenium-profiles\\liverpool-qa");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get(URL_HOME);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    // CASOS DE FLUJO (CF)
    // =========================================================================

    @DataProvider(name = "rutasPositivasCF01")
    public Object[][] rutasPositivasCF01() {
        return new Object[][]{
                {"Zapatos", "Botas"},
                {"Zapatos", "Mocasines de hombre"}
        };
    }

    @Test(
            dataProvider = "rutasPositivasCF01",
            description = "CF-01: Validar flujo de navegación, selección de producto, agregado a bolsa y disponibilidad de compra"
    )
    public void testCF01FlujoCompraPorSubcategoria(String categoria, String subcategoria) {
        HomePage homePage = new HomePage(driver);
        ProductDetailPage pdp = new ProductDetailPage(driver);
        BagPage bagPage = new BagPage(driver);

        abrirProductoEnPDP(homePage, pdp, categoria, subcategoria, "CF-01");

        boolean agregado = pdp.agregarABolsaYConfirmar();
        Assert.assertTrue(
                agregado,
                "CF-01 Error: No se incrementó el contador de la bolsa al agregar producto de " + categoria + " - " + subcategoria
        );

        homePage.irABolsa();

        Assert.assertTrue(
                bagPage.esBotonComprarVisible(),
                "CF-01 Error: El botón 'Comprar' no está visible o habilitado en la bolsa. URL actual: " + driver.getCurrentUrl()
        );

        bagPage.irAComprarAhora();
    }

    // =========================================================================
    // CASOS NEGATIVOS (CN)
    // =========================================================================

    @DataProvider(name = "rutasNegativasCN01")
    public Object[][] rutasNegativasCN01() {
        return new Object[][]{
                {"Perfumes", "Caballero"},
                {"Electrónica", "Computadora Lenovo"},
                {"Electrónica", "Pantalla Sony"},
                {"Electrónica", "Xbox"}
        };
    }

    @Test(
            dataProvider = "rutasNegativasCN01",
            description = "CN-01: Validar falla controlada ante categorías o subcategorías inexistentes en el menú"
    )
    public void testCN01NavegacionInvalidaEnMenu(String categoria, String subcategoria) {
        HomePage homePage = new HomePage(driver);
        homePage.cerrarBannerCookiesSiExiste();

        IllegalArgumentException excepcion = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> homePage.navegarPorCategoria(categoria, subcategoria)
        );

        Assert.assertNotNull(excepcion.getMessage(), "CN-01 Error: El mensaje de excepción no debe ser nulo.");
        Assert.assertTrue(
                excepcion.getMessage().contains(categoria) || excepcion.getMessage().contains(subcategoria),
                "CN-01 Error: El mensaje no detalla el dato evaluado. Mensaje: " + excepcion.getMessage()
        );
    }

    // =========================================================================
    // CASOS EDGE (CE)
    // =========================================================================

    @Test(
            description = "CE-01: Validar mensaje de pop-up al intentar agregar más de 100 piezas de un artículo"
    )
    public void testCE01CantidadExcedidaArticulo() {
        HomePage homePage = new HomePage(driver);
        ProductDetailPage pdp = new ProductDetailPage(driver);

        abrirProductoEnPDP(homePage, pdp, "Zapatos", "Botas", "CE-01");

        String mensajeObtenido = pdp.intentarAgregarCantidadYCapturarMensajeCE01(CANTIDAD_EXCEDIDA_CE01);
        Assert.assertEquals(
                mensajeObtenido,
                MENSAJE_ESPERADO_CE01,
                "CE-01 Error: No se mostró el mensaje esperado al intentar agregar " +
                        CANTIDAD_EXCEDIDA_CE01 + " piezas."
        );
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    private void abrirProductoEnPDP(
            HomePage homePage,
            ProductDetailPage pdp,
            String categoria,
            String subcategoria,
            String prefijoCaso
    ) {
        homePage.cerrarBannerCookiesSiExiste();
        homePage.navegarPorCategoria(categoria, subcategoria);

        String urlListado = driver.getCurrentUrl();
        homePage.seleccionarPrimerProducto();
        Assert.assertNotEquals(
                driver.getCurrentUrl(),
                urlListado,
                prefijoCaso + " Error: No se navegó al PDP al seleccionar el primer producto en " + categoria + " - " + subcategoria
        );

        pdp.seleccionarTallaSiAplica();
    }
}