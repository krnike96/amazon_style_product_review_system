package com.niket.productreviewsystem.repository;

import com.niket.productreviewsystem.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}