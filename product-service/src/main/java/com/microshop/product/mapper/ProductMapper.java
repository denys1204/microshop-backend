package com.microshop.product.mapper;

import com.microshop.product.dto.ProductRequest;
import com.microshop.product.dto.ProductResponse;
import com.microshop.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    Product mapToProduct(ProductRequest productRequest);

    ProductResponse mapToProductResponse(Product product);

    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequest request, @MappingTarget Product product);
}