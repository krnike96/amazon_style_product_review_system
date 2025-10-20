package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.model.ReviewFormDTO;
import com.niket.productreviewsystem.repository.ProductRepository;
import com.niket.productreviewsystem.service.ReviewService;
import com.niket.productreviewsystem.service.ReviewReportService; // Import was in user code, keeping it
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // NEW IMPORT
import org.springframework.data.domain.Pageable; // NEW IMPORT
import org.springframework.data.web.PageableDefault; // NEW IMPORT
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewReportService reportService;

    @GetMapping({"", "/"})
    public String listProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "product-list";
    }

    // --- CRITICAL FIX APPLIED HERE ---
    @GetMapping("/{productId}")
    public String showProductDetails(@PathVariable Long productId,
                                     @PageableDefault(size = 5) Pageable pageable, // ADDED: Handles 'page' and 'size' parameters
                                     @RequestParam(required = false, defaultValue = "newest") String sort,
                                     Model model) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // NOTE FOR REVIEWSERVICE:
        // You MUST implement this new method signature in ReviewService.java:
        // public Page<Review> getPaginatedReviewsByProductId(Long productId, String sort, Pageable pageable)
        Page<Review> reviewsPage = reviewService.getPaginatedReviewsByProductId(productId, sort, pageable);

        double averageRating = reviewService.getAverageRating(productId);

        // 2. Add data to model
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewsPage); // <<< FIX: Pass the Page object to Thymeleaf
        model.addAttribute("avgRating", averageRating); // Renamed attribute for consistency with the corrected HTML
        model.addAttribute("currentSort", sort);

        // 3. Prepare DTO for form submission
        if (!model.containsAttribute("reviewFormDTO")) {
            ReviewFormDTO dto = new ReviewFormDTO();
            dto.setProductId(productId);
            model.addAttribute("reviewFormDTO", dto);
        }

        return "product-detail";
    }

    @PostMapping("/{productId}/submit-review")
    public String submitReview(@PathVariable Long productId,
                               @Valid @ModelAttribute("reviewFormDTO") ReviewFormDTO reviewFormDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.reviewFormDTO", result);
            redirectAttributes.addFlashAttribute("reviewFormDTO", reviewFormDTO);
            return "redirect:/products/" + productId + "#review-form";
        }

        try {
            reviewService.saveReview(reviewFormDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back to the first page (page=0) to see the new review with default sort.
        return "redirect:/products/" + productId;
    }

    // --- Updated for correct voting logic and parameter retention ---
    @PostMapping("/reviews/{reviewId}/vote")
    public String voteReview(@PathVariable Long reviewId,
                             @RequestParam String type, // Expects 'UP' or 'DOWN' from the Thymeleaf form
                             @RequestParam Long productId,
                             @RequestParam(required = false, defaultValue = "newest") String sort,
                             @RequestParam(required = false, defaultValue = "0") int page, // Retain current page
                             @RequestParam(required = false, defaultValue = "5") int size, // Retain page size
                             RedirectAttributes redirectAttributes) {
        try {
            // NOTE FOR REVIEWSERVICE:
            // You must implement this method to handle both 'UP' and 'DOWN' votes
            reviewService.addVote(reviewId, type);
            redirectAttributes.addFlashAttribute("successMessage", "Vote recorded. Thank you!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back, preserving current page, size, and sort for a smooth experience
        return "redirect:/products/" + productId + "?sort=" + sort + "&page=" + page + "&size=" + size;
    }

    @PostMapping("/reviews/report/{reviewId}")
    public String reportReview(@PathVariable Long reviewId,
                               @RequestParam String reason,
                               @RequestParam(required = false) String detailReason, // Retained field name from correct HTML
                               @RequestParam Long productId,
                               RedirectAttributes redirectAttributes) {
        try {
            reportService.submitReport(reviewId, reason, detailReason);
            redirectAttributes.addFlashAttribute("successMessage", "Review reported successfully. An admin will review it shortly.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }
}