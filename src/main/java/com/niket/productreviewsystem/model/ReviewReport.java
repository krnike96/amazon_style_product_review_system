package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_reports")
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The review being reported
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // The user who filed the report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    // The reason for the report (e.g., Spam, Hate Speech, Profanity)
    @Column(name = "reason", length = 255)
    private String reason;

    // user-provided detail when 'Other' is selected. Max 500 characters (approx. 100 words)
    @Column(name = "detail_reason", length = 500)
    private String detailReason;

    // Timestamp for when the report was filed
    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    // Status to track if the report has been processed by an admin
    @Column(name = "is_processed", nullable = false)
    private boolean isProcessed = false;


    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

    public String getDetailReason() {
        return detailReason;
    }

    public void setDetailReason(String detailReason) {
        this.detailReason = detailReason;
    }
}