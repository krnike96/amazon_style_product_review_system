package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.model.ReviewFormDTO;
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import com.niket.productreviewsystem.service.ReviewReportService;

import java.util.List;

@Controller
@RequestMapping("/products") // Map all requests in this controller to /products
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewService reviewService; // Review Service for business logic

    @Autowired
    private ReviewReportService reportService;

    // Handles GET request for product list (now just "/")
    @GetMapping({"", "/"})
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "product-list";
    }

    // New: Handles GET request for a single product page
    @GetMapping("/{productId}")
    public String showProductDetails(@PathVariable Long productId,
                                     @RequestParam(required = false, defaultValue = "newest") String sort, // NEW PARAM
                                     Model model) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 1. Fetch data from service using the sort parameter
        List<Review> reviews = reviewService.getReviewsByProductId(productId, sort); // MODIFIED CALL
        double averageRating = reviewService.getAverageRating(productId); // (Uses old method without sort)
        long reviewCount = reviews.size();

        // 2. Add data to model
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("currentSort", sort); // Pass the current sort order to the UI

        // 3. Prepare DTO for form submission
        if (!model.containsAttribute("reviewFormDTO")) {
            ReviewFormDTO dto = new ReviewFormDTO();
            dto.setProductId(productId);
            model.addAttribute("reviewFormDTO", dto);
        }

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

    @PostMapping("/reviews/vote/{reviewId}")
    public String voteHelpful(@PathVariable Long reviewId,
                              @RequestParam Long productId, // Need product ID to redirect back
                              @RequestParam(required = false, defaultValue = "newest") String sort, // Retain sort order
                              RedirectAttributes redirectAttributes) {
        try {
            reviewService.addHelpfulVote(reviewId);
            redirectAttributes.addFlashAttribute("successMessage", "Vote recorded. Thank you!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // Redirect back to the product detail page, retaining the sort order
        return "redirect:/products/" + productId + "?sort=" + sort;
    }

    @PostMapping("/reviews/report/{reviewId}")
    public String reportReview(@PathVariable Long reviewId,
                               @RequestParam String reason,
                               @RequestParam(required = false) String otherReason, // NEW OPTIONAL PARAMETER
                               @RequestParam Long productId,
                               RedirectAttributes redirectAttributes) {
        try {
            // PASS THE NEW PARAMETER
            reportService.submitReport(reviewId, reason, otherReason);
            redirectAttributes.addFlashAttribute("successMessage", "Review reported successfully. An admin will review it shortly.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }
}