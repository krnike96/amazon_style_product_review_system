package com.niket.productreviewsystem.repository;

import com.niket.productreviewsystem.model.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    // Custom query method: checks if a vote already exists for this user and review
    Optional<ReviewVote> findByUserIdAndReviewId(Long userId, Long reviewId);
}