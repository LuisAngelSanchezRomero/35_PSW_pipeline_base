package vallegrande.edu.pe.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import vallegrande.edu.pe.model.Product;
import vallegrande.edu.pe.service.ProductService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void getProducts_debeRetornarListaDeProductos() throws Exception {
        when(productService.getProducts()).thenReturn(List.of(
                new Product(1, "Laptop", 2500.0),
                new Product(2, "Mouse", 80.0),
                new Product(3, "Teclado", 120.0),
                new Product(4, "Monitor", 850.0),
                new Product(5, "Audífonos", 150.0)
        ));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].price").value(2500.0));
    }

    @Test
    void getProducts_debeTenerCamposCorrectos() throws Exception {
        when(productService.getProducts()).thenReturn(List.of(
                new Product(1, "Laptop", 2500.0)
        ));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].price").exists());
    }
}
