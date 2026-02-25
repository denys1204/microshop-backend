package com.microshop.product.service;

import com.microshop.product.dto.ProductRequest;
import com.microshop.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest productRequest);
    Page<ProductResponse> getProducts(String name, BigDecimal price, String description, String sku, Pageable pageable);
    ProductResponse getProduct(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}