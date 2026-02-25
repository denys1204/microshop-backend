package com.microshop.product.spec;

import com.microshop.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductSpecifications {
    public Specification<Product> getSpecification(String name, BigDecimal price, String description, String sku) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (price != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), price));
            }
            if (description != null && !description.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            }
            if (sku != null && !sku.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sku"), sku));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
