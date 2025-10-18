package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
// Ensure this controller's methods can only be executed by users with ROLE_ADMIN
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 1. Display the moderation dashboard
    @GetMapping("/reviews")
    public String adminDashboard(Model model) {
        // Fetch all reviews, ready for moderation (e.g., pending approval)
        List<Review> allReviews = adminService.getAllReviews();
        model.addAttribute("reviews", allReviews);
        return "admin/dashboard"; // Look for template in src/main/resources/templates/admin/dashboard.html
    }

    // 2. Approve a review
    @PostMapping("/reviews/approve/{reviewId}")
    public String approveReview(@PathVariable Long reviewId, RedirectAttributes redirectAttributes) {
        try {
            adminService.moderateReview(reviewId, true);
            redirectAttributes.addFlashAttribute("successMessage", "Review ID " + reviewId + " approved.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    // 3. Reject/Delete a review
    @PostMapping("/reviews/reject/{reviewId}")
    public String rejectReview(@PathVariable Long reviewId, RedirectAttributes redirectAttributes) {
        try {
            adminService.moderateReview(reviewId, false);
            redirectAttributes.addFlashAttribute("successMessage", "Review ID " + reviewId + " rejected and deleted.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
}