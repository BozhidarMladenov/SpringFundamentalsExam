package com.softuni.gearshare.service;

import com.softuni.gearshare.exception.DuplicateReviewException;
import com.softuni.gearshare.exception.EntityNotFoundException;
import com.softuni.gearshare.exception.InvalidBookingException;
import com.softuni.gearshare.exception.UnauthorizedActionException;
import com.softuni.gearshare.model.dto.ReviewFormRequest;
import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.Review;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.BookingStatus;
import com.softuni.gearshare.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;

    public List<Review> findAllForEquipment(Equipment equipment) {
        return reviewRepository.findAllByBookingEquipment(equipment);
    }

    public Review findById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));
    }

    @Transactional
    public Review create(UUID bookingId, ReviewFormRequest request, User currentUser) {
        Booking booking = bookingService.findById(bookingId);
        requireRenterOwnership(booking, currentUser);

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidBookingException("You can only review a completed booking.");
        }
        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new DuplicateReviewException("You have already reviewed this booking.");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    @Transactional
    public Review update(UUID id, ReviewFormRequest request, User currentUser) {
        Review review = findById(id);
        requireRenterOwnership(review.getBooking(), currentUser);

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    @Transactional
    public void delete(UUID id, User currentUser) {
        Review review = findById(id);
        requireRenterOwnership(review.getBooking(), currentUser);
        reviewRepository.delete(review);
    }

    private void requireRenterOwnership(Booking booking, User user) {
        if (!booking.getRenter().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You may only manage your own reviews.");
        }
    }
}
