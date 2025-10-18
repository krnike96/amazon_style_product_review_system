package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // Required for List<ReviewVote> and List<ReviewReport>

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_rating_date", columnList = "rating, reviewDate")
})
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Lob
    private String comment;

    private String imagePath;

    @Column(nullable = false)
    private LocalDateTime reviewDate = LocalDateTime.now();

    private boolean verifiedPurchase = false;

    @Column(name = "helpful_votes", nullable = false)
    private int helpfulVotes = 0;

    // ==========================================================
    // CRITICAL FIX 1: Relationship to ReviewVote with CASCADE
    // This fixes the FK constraint error for the 'review_votes' table.
    // ==========================================================
    @OneToMany(mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ReviewVote> votes;

    // ==========================================================
    // CRITICAL FIX 2: Relationship to ReviewReport with CASCADE
    // This fixes the FK constraint error for the 'review_reports' table.
    // ==========================================================
    @OneToMany(mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ReviewReport> reports; // Assuming you have a ReviewReport entity

    // ==========================================================

    public int getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(int helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }
}