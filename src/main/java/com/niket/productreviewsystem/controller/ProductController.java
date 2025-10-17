package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.model.ReviewFormDTO; // New DTO import
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.service.ReviewService; // New Service import
import jakarta.validation.Valid; // New validation import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // New validation import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products") // Map all requests in this controller to /products
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewService reviewService; // Review Service for business logic

    // Handles GET request for product list (now just "/")
    @GetMapping({"", "/"})
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "product-list";
    }

    // New: Handles GET request for a single product page
    @GetMapping("/{productId}")
    public String showProductDetails(@PathVariable Long productId, Model model) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);
        // Pass a new, empty DTO for the form
        if (!model.containsAttribute("reviewFormDTO")) {
            ReviewFormDTO dto = new ReviewFormDTO();
            dto.setProductId(productId);
            model.addAttribute("reviewFormDTO", dto);
        }

        // TODO: In later phases, fetch and display actual reviews here

        return "product-detail";
    }

    // New: Handles POST request for submitting a review
    @PostMapping("/{productId}/submit-review")
    public String submitReview(@PathVariable Long productId,
                               @Valid @ModelAttribute("reviewFormDTO") ReviewFormDTO reviewFormDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            // If validation errors exist (including NoUrl constraint),
            // redirect back to the product page with errors
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewFormDTO", result);
            redirectAttributes.addFlashAttribute("reviewFormDTO", reviewFormDTO);
            return "redirect:/products/" + productId + "#review-form"; // Go back to the product page, focus on form
        }

        try {
            // The service layer handles file storage, user mapping, and saving to DB
            reviewService.saveReview(reviewFormDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/products/" + productId;
    }
}