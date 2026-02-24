package com.microshop.product.service.impl;

import com.microshop.product.dto.ProductRequest;
import com.microshop.product.dto.ProductResponse;
import com.microshop.product.entity.Product;
import com.microshop.product.mapper.ProductMapper;
import com.microshop.product.repository.ProductRepository;
import com.microshop.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapper.mapToProduct(request);
        Product savedProduct = repository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        return mapper.mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return repository.findAll().stream()
                .map(mapper::mapToProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return repository.findById(id)
                .map(mapper::mapToProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = repository.findById(id).orElseThrow(
                () -> new RuntimeException("Product not found with id: " + id)
        );

        mapper.updateProductFromRequest(request, product);
        Product updatedProduct = repository.save(product);

        log.info("Product {} was updated", id);
        return mapper.mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        repository.deleteById(id);
        log.info("Product {} was deleted", id);
    }
}