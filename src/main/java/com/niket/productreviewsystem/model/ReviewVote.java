package com.niket.productreviewsystem.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "review_votes", uniqueConstraints = {
        // Crucial: Ensures a single user can only vote once per review
        @UniqueConstraint(columnNames = {"user_id", "review_id"})
})
public class ReviewVote implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who cast the vote
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The review that was voted on
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}