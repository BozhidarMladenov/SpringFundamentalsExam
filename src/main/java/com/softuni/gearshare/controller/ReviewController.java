package com.softuni.gearshare.controller;

import com.softuni.gearshare.config.CurrentUserProvider;
import com.softuni.gearshare.model.dto.ReviewFormRequest;
import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.Review;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.service.BookingService;
import com.softuni.gearshare.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/new/{bookingId}")
    public String createForm(@PathVariable UUID bookingId, Model model) {
        if (!model.containsAttribute("reviewFormRequest")) {
            model.addAttribute("reviewFormRequest", new ReviewFormRequest());
        }
        model.addAttribute("booking", bookingService.findById(bookingId));
        return "review/create";
    }

    @PostMapping("/new/{bookingId}")
    public String create(@PathVariable UUID bookingId,
                          @Valid @ModelAttribute("reviewFormRequest") ReviewFormRequest request,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("booking", bookingService.findById(bookingId));
            return "review/create";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        reviewService.create(bookingId, request, currentUser);
        return "redirect:/bookings/mine";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        Review review = reviewService.findById(id);

        ReviewFormRequest form = new ReviewFormRequest();
        form.setRating(review.getRating());
        form.setComment(review.getComment());

        model.addAttribute("reviewFormRequest", form);
        model.addAttribute("review", review);
        return "review/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                        @Valid @ModelAttribute("reviewFormRequest") ReviewFormRequest request,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("review", reviewService.findById(id));
            return "review/edit";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        Review updated = reviewService.update(id, request, currentUser);
        Booking booking = updated.getBooking();
        return "redirect:/equipment/" + booking.getEquipment().getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        Review review = reviewService.findById(id);
        UUID equipmentId = review.getBooking().getEquipment().getId();
        User currentUser = currentUserProvider.getCurrentUser();
        reviewService.delete(id, currentUser);
        return "redirect:/equipment/" + equipmentId;
    }
}
