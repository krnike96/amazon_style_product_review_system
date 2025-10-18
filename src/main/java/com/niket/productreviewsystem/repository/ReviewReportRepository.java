package com.niket.productreviewsystem.repository;

import com.niket.productreviewsystem.model.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

    // MODIFIED QUERY: Fetching Review, Product, AND Reporter (User) details.
    @Query("SELECT r FROM ReviewReport r JOIN FETCH r.review rev JOIN FETCH rev.product prod JOIN FETCH r.reporter rep WHERE r.isProcessed = false ORDER BY r.reportDate DESC")
    List<ReviewReport> findPendingReportsWithDetails();

    // Check if a specific user has already reported this review
    boolean existsByReporterIdAndReviewId(Long reporterId, Long reviewId);
}