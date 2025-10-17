package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // Handles the GET request for the product list page
    @GetMapping({"/", "/products"}) // Map both root URL and /products to this page
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        // Add the list of products to the Model so Thymeleaf can display them
        model.addAttribute("products", products);

        // This tells Spring to look for the template file: src/main/resources/templates/product-list.html
        return "product-list";
    }
}