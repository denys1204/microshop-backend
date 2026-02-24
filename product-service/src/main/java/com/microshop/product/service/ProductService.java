package com.microshop.product.service;

import com.microshop.product.dto.ProductRequest;
import com.microshop.product.dto.ProductResponse;
import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequest productRequest);
    List<ProductResponse> getAllProducts();
    ProductResponse getProduct(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
}