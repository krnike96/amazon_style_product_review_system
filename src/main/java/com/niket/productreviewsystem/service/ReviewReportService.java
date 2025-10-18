package com.niket.productreviewsystem.service;

import com.niket.productreviewsystem.model.Review;
import com.niket.productreviewsystem.model.ReviewReport;
import com.niket.productreviewsystem.model.User;
import com.niket.productreviewsystem.repository.ReviewReportRepository;
import com.niket.productreviewsystem.repository.ReviewRepository;
import com.niket.productreviewsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewReportService {

    @Autowired
    private ReviewReportRepository reportRepository;

    @Autowired
    private ReviewRepository reviewRepository; // Need to fetch the review entity

    @Autowired
    private UserRepository userRepository; // Need to get the reporting user

    private void validateReportDetail(String detail) {
        if (detail == null || detail.trim().isEmpty()) {
            return;
        }

        // 1. URL Check (Prevent links)
        // Simple regex check for common URL patterns (http, www, common domains)
        String urlPattern = ".*(http|www|\\.com|\\.net|\\.org|\\.co).*";
        if (detail.toLowerCase().matches(urlPattern)) {
            throw new RuntimeException("Report detail cannot contain links or URLs.");
        }

        // 2. Word/Length Check (Max 100 words, using max 500 characters as proxy)
        // A simple character count check is much more efficient than word counting in Java
        if (detail.length() > 500) {
            throw new RuntimeException("Report detail exceeds the 500 character limit (approx. 100 words).");
        }
    }

    /**
     * Handles the submission of a new report by a user.
     */
    @Transactional
    public void submitReport(Long reviewId, String reason, String otherReason) { // NEW PARAMETER
        // 1. Get current authenticated user (existing logic)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Reporter user not found. Please log in."));

        // 2. Get the review (existing logic)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found for reporting."));

        // 3. Prevent duplicate reports (existing logic)
        if (reportRepository.existsByReporterIdAndReviewId(reporter.getId(), reviewId)) {
            throw new RuntimeException("You have already reported this review.");
        }

        // 4. Validation (NEW)
        String finalReason = reason;
        if ("Other".equalsIgnoreCase(reason) && otherReason != null && !otherReason.trim().isEmpty()) {
            validateReportDetail(otherReason);
            // Combine the reason and the detail for saving
            finalReason = "Other: " + otherReason.trim();
        } else if ("Other".equalsIgnoreCase(reason) && (otherReason == null || otherReason.trim().isEmpty())) {
            // Handle case where 'Other' is selected but the text is blank
            throw new RuntimeException("Please explain your report reason.");
        }

        // 5. Create and save the new report
        ReviewReport report = new ReviewReport();
        report.setReview(review);
        report.setReporter(reporter);
        report.setReason(finalReason); // Save the combined or original reason
        report.setDetailReason(otherReason != null ? otherReason.trim() : null); // Save the detail separately
        report.setReportDate(LocalDateTime.now());
        report.setProcessed(false);

        reportRepository.save(report);
    }

    /**
     * Gets a list of all reports that have not yet been processed by an admin.
     */
    public List<ReviewReport> getPendingReports() {
        // Use the dedicated repository method with JOIN FETCH
        return reportRepository.findPendingReportsWithDetails();
    }

    /**
     * Marks a report as processed (after admin action is taken).
     */
    @Transactional
    public ReviewReport markReportAsProcessed(Long reportId) { // CHANGE RETURN TYPE to ReviewReport
        ReviewReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found."));

        report.setProcessed(true);
        // The save method is not strictly necessary here because @Transactional handles persistence,
        // but keeping it ensures immediate flush and clarity.
        return reportRepository.save(report); // RETURN THE SAVED REPORT
    }
}