package com.microshop.product.spec;

import com.microshop.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ProductSpecifications {
    public Specification<Product> getSpecification(String name, BigDecimal price, String description, String sku) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                String escapedName = escapeLikePattern(name.toLowerCase(Locale.ROOT));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + escapedName + "%", '\\'));
            }
            if (price != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), price));
            }
            if (description != null && !description.isBlank()) {
                String escapedDesc = escapeLikePattern(description.toLowerCase(Locale.ROOT));
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + escapedDesc + "%", '\\'));
            }
            if (sku != null && !sku.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("sku"), sku));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String escapeLikePattern(String input) {
        if (input == null) return null;
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
