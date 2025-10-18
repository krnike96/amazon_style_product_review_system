package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"), // Performance optimization
        @Index(name = "idx_rating_date", columnList = "rating, reviewDate") // For sorting/analytics
})
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Many reviews to one product
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY) // Many reviews by one user
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // 1 to 5 stars

    @Lob
    private String comment; // The field that will be validated against URLs

    private String imagePath; // Local path for optional review image

    @Column(nullable = false)
    private LocalDateTime reviewDate = LocalDateTime.now();

    // Simulated requirement: to show "Verified Purchase" badge
    private boolean verifiedPurchase = false;

    @Column(name = "helpful_votes", nullable = false)
    private int helpfulVotes = 0;

    public int getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(int helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }
}