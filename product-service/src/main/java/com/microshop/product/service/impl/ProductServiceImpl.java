package com.microshop.product.service.impl;

import com.microshop.product.dto.ProductRequest;
import com.microshop.product.dto.ProductResponse;
import com.microshop.product.entity.Product;
import com.microshop.product.exception.DuplicateSkuException;
import com.microshop.product.exception.ProductNotFoundException;
import com.microshop.product.mapper.ProductMapper;
import com.microshop.product.repository.ProductRepository;
import com.microshop.product.service.ProductService;
import com.microshop.product.spec.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductSpecifications specifications;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        Product product = mapper.mapToProduct(request);
        Product savedProduct = repository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        return mapper.mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String name, BigDecimal price, String description, String sku, Pageable pageable) {
        Specification<Product> specification = specifications.getSpecification(name, price, description, sku);
        return repository.findAll(specification, pageable).map(mapper::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        return repository.findById(id)
                .map(mapper::mapToProductResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = repository.findById(id).orElseThrow(
                () -> new ProductNotFoundException(id)
        );

        if (repository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateSkuException(request.sku());
        }

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