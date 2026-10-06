package vallegrande.edu.pe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vallegrande.edu.pe.model.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    // --- getProducts ---

    @Test
    void getProducts_debeRetornarListaNoVacia() {
        List<Product> products = productService.getProducts();
        assertNotNull(products);
        assertFalse(products.isEmpty());
    }

    @Test
    void getProducts_debeTenerCincoProductos() {
        List<Product> products = productService.getProducts();
        assertEquals(5, products.size());
    }

    @Test
    void getProducts_primerProductoEsLaptop() {
        List<Product> products = productService.getProducts();
        assertEquals("Laptop", products.get(0).getName());
        assertEquals(2500.00, products.get(0).getPrice());
    }

    // --- processProduct ---

    @Test
    void processProduct_conNombreValido_debeRetornarMensaje() {
        String result = productService.processProduct("Laptop");
        assertEquals("Producto procesado: Laptop", result);
    }

    @Test
    void processProduct_conNombreVacio_debeRetornarMensajeVacio() {
        String result = productService.processProduct("");
        assertEquals("Producto vacío", result);
    }

    @Test
    void processProduct_conNull_debeRetornarMensajeNoValido() {
        String result = productService.processProduct(null);
        assertEquals("Producto no válido", result);
    }

    // --- validateProduct ---

    @Test
    void validateProduct_conProductoValido_debeRetornarTrue() {
        Product product = new Product(1, "Mouse", 80.00);
        assertTrue(productService.validateProduct(product));
    }

    @Test
    void validateProduct_conProductoNull_debeRetornarFalse() {
        assertFalse(productService.validateProduct(null));
    }

    @Test
    void validateProduct_conNombreNull_debeRetornarFalse() {
        Product product = new Product(1, null, 80.00);
        assertFalse(productService.validateProduct(product));
    }

    @Test
    void validateProduct_conNombreVacio_debeRetornarFalse() {
        Product product = new Product(1, "", 80.00);
        assertFalse(productService.validateProduct(product));
    }

    @Test
    void validateProduct_conPrecioNull_debeRetornarFalse() {
        Product product = new Product(1, "Monitor", null);
        assertFalse(productService.validateProduct(product));
    }

    @Test
    void validateProduct_conPrecioCero_debeRetornarFalse() {
        Product product = new Product(1, "Monitor", 0.0);
        assertFalse(productService.validateProduct(product));
    }

    @Test
    void validateProduct_conPrecioNegativo_debeRetornarFalse() {
        Product product = new Product(1, "Monitor", -10.0);
        assertFalse(productService.validateProduct(product));
    }
}
