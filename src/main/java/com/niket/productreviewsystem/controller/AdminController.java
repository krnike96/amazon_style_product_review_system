package com.niket.productreviewsystem.controller;

import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.model.Product;
import com.niket.productreviewsystem.service.AdminService;
import com.niket.productreviewsystem.service.ProductAdminService;
import com.niket.productreviewsystem.model.ReviewReport;
import com.niket.productreviewsystem.service.ReviewReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductAdminService productAdminService;

    @Autowired
    private ReviewReportService reportService;

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

    // 1. List all products
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productAdminService.findAll();
        model.addAttribute("products", products);
        return "admin/product-list";
    }

    // 2. Display product creation/edit form
    @GetMapping("/products/form")
    public String productForm(@RequestParam(required = false) Long id, Model model) {
        Product product = new Product();
        if (id != null) {
            product = productAdminService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        }
        model.addAttribute("product", product);
        return "admin/product-form";
    }

    // 3. Handle product save/update
    @PostMapping("/products/save")
    public String saveProduct(Product product,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            // If the ID exists and the file is empty, retain the old image path
            if (product.getId() != null && file.isEmpty()) {
                Product existingProduct = productAdminService.findById(product.getId()).orElse(null);
                if (existingProduct != null) {
                    product.setImagePath(existingProduct.getImagePath());
                }
            }

            productAdminService.saveProduct(product, file);
            redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // 4. Delete a product
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productAdminService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product ID " + id + " deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // 1. Display the pending reports dashboard (NEW)
    @GetMapping("/reports")
    public String reportDashboard(Model model) {
        List<ReviewReport> pendingReports = reportService.getPendingReports();
        model.addAttribute("reports", pendingReports);
        return "admin/report-dashboard"; // Look for template here
    }

    // 2. Mark a report as processed (NEW)
    // NOTE: Actual review deletion (rejection) is handled by the existing /reviews/reject/{reviewId} endpoint.
    @PostMapping("/reports/process/{reportId}")
    public String processReport(@PathVariable Long reportId,
                                @RequestParam(required = false, defaultValue = "false") Boolean deleteReview, // Optional parameter
                                RedirectAttributes redirectAttributes) {
        try {
            ReviewReport report = reportService.markReportAsProcessed(reportId);

            // If the admin decides to delete the review that was reported:
            if (deleteReview) {
                // Use the existing deletion service (AdminService)
                adminService.moderateReview(report.getReview().getId(), false); // false = reject/delete
                redirectAttributes.addFlashAttribute("successMessage", "Report ID " + reportId + " processed and Review ID " + report.getReview().getId() + " was deleted.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Report ID " + reportId + " processed (Review retained).");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/reports";
    }
}