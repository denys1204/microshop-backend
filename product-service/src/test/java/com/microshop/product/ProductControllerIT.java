package com.microshop.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microshop.product.dto.ProductRequest;
import com.microshop.product.entity.Product;
import com.microshop.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIT extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "PlayStation 5",
                "Next-gen gaming console",
                new BigDecimal("499.99"),
                "SONY-PS5-001"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("PlayStation 5"))
                .andExpect(jsonPath("$.sku").value("SONY-PS5-001"));

        List<Product> products = repository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().getSku()).isEqualTo("SONY-PS5-001");
    }

    @Test
    void shouldReturn400BadRequestWhenValidationFails() throws Exception {
        ProductRequest invalidRequest = new ProductRequest(
                "",
                "Next-gen gaming console",
                new BigDecimal("-100.00"),
                "SONY-PS5-001"
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.validationErrors[?(@.field == 'price')]").exists());

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldReturn409ConflictWhenSkuAlreadyExists() throws Exception {
        String sharedSku = "UNIQUE-SKU-999";
        ProductRequest firstRequest = new ProductRequest(
                "First Console",
                "Original",
                new BigDecimal("300.00"),
                sharedSku
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        ProductRequest duplicateRequest = new ProductRequest(
                "Second Console",
                "Different description",
                new BigDecimal("400.00"),
                sharedSku
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Product with SKU '" + sharedSku + "' already exists"));

        List<Product> products = repository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.getFirst().getName()).isEqualTo("First Console");
    }

    @Test
    void shouldReturn404NotFoundWhenProductDoesNotExist() throws Exception {
        Long nonExistentId = 999L;

        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: " + nonExistentId));
    }

    @Test
    void shouldGetProductByIdSuccessfully() throws Exception {
        ProductRequest request = new ProductRequest(
                "Nintendo Switch",
                "Portable console",
                new BigDecimal("299.99"),
                "NIN-SW-001"
        );

        String responseJson = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer savedId = com.jayway.jsonpath.JsonPath.read(responseJson, "$.id");

        mockMvc.perform(get("/api/v1/products/{id}", savedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId))
                .andExpect(jsonPath("$.name").value("Nintendo Switch"))
                .andExpect(jsonPath("$.sku").value("NIN-SW-001"));
    }

    @Test
    void shouldGetAllProductsWithPaginationAndFiltering() throws Exception {
        ProductRequest p1 = new ProductRequest("iPhone 15", "Apple smartphone", new BigDecimal("999.00"), "APL-IPH15");
        ProductRequest p2 = new ProductRequest("Samsung Galaxy S24", "Samsung smartphone", new BigDecimal("899.00"), "SAM-S24");
        ProductRequest p3 = new ProductRequest("iPhone 15 Pro", "Premium Apple smartphone", new BigDecimal("1199.00"), "APL-IPH15P");

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(p3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products")
                        .param("name", "iphone")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                .andExpect(jsonPath("$.content[1].name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        ProductRequest createReq = new ProductRequest("Old Mouse", "Old model", new BigDecimal("25.00"), "MOUSE-001");
        String createRes = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer savedId = com.jayway.jsonpath.JsonPath.read(createRes, "$.id");

        ProductRequest updateReq = new ProductRequest("New Mouse Pro", "Wireless model", new BigDecimal("45.00"), "MOUSE-001-PRO");

        mockMvc.perform(put("/api/v1/products/{id}", savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Mouse Pro"))
                .andExpect(jsonPath("$.price").value(45.00))
                .andExpect(jsonPath("$.sku").value("MOUSE-001-PRO"));

        assertThat(repository.findById(savedId.longValue()).orElseThrow().getName()).isEqualTo("New Mouse Pro");
    }

    @Test
    void shouldReturn409ConflictWhenUpdatingToExistingSku() throws Exception {
        ProductRequest req1 = new ProductRequest("Keyboard 1", "Desc 1", new BigDecimal("50.00"), "KEY-001");
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        ProductRequest req2 = new ProductRequest("Keyboard 2", "Desc 2", new BigDecimal("60.00"), "KEY-002");
        String res2 = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer idToUpdate = com.jayway.jsonpath.JsonPath.read(res2, "$.id");

        ProductRequest maliciousUpdateReq = new ProductRequest("Hacked Keyboard", "Desc", new BigDecimal("60.00"), "KEY-001");

        mockMvc.perform(put("/api/v1/products/{id}", idToUpdate)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousUpdateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Product with SKU 'KEY-001' already exists"));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        ProductRequest req = new ProductRequest("To Delete", "Will be removed", new BigDecimal("10.00"), "DEL-001");
        String res = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Integer idToDelete = com.jayway.jsonpath.JsonPath.read(res, "$.id");

        mockMvc.perform(delete("/api/v1/products/{id}", idToDelete))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(idToDelete.longValue())).isEmpty();
    }
}