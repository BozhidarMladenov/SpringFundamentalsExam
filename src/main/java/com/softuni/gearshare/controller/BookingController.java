package com.softuni.gearshare.controller;

import com.softuni.gearshare.config.CurrentUserProvider;
import com.softuni.gearshare.model.dto.BookingFormRequest;
import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.BookingStatus;
import com.softuni.gearshare.service.BookingService;
import com.softuni.gearshare.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/mine")
    public String myBookings(Model model) {
        User currentUser = currentUserProvider.getCurrentUser();
        model.addAttribute("bookings", bookingService.findAllForRenter(currentUser));
        return "booking/mine";
    }

    @GetMapping("/requests")
    public String incomingRequests(Model model) {
        User currentUser = currentUserProvider.getCurrentUser();
        model.addAttribute("bookings", bookingService.findAllForOwner(currentUser));
        return "booking/requests";
    }

    @GetMapping("/new/{equipmentId}")
    public String createForm(@PathVariable UUID equipmentId, Model model) {
        if (!model.containsAttribute("bookingFormRequest")) {
            model.addAttribute("bookingFormRequest", new BookingFormRequest());
        }
        model.addAttribute("equipment", equipmentService.findById(equipmentId));
        return "booking/create";
    }

    @PostMapping("/new/{equipmentId}")
    public String create(@PathVariable UUID equipmentId,
                          @Valid @ModelAttribute("bookingFormRequest") BookingFormRequest request,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("equipment", equipmentService.findById(equipmentId));
            return "booking/create";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        Booking booking = bookingService.create(equipmentId, request, currentUser);
        return "redirect:/bookings/mine?created=" + booking.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        Booking booking = bookingService.findById(id);

        BookingFormRequest form = new BookingFormRequest();
        form.setStartDate(booking.getStartDate());
        form.setEndDate(booking.getEndDate());

        model.addAttribute("bookingFormRequest", form);
        model.addAttribute("booking", booking);
        return "booking/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                        @Valid @ModelAttribute("bookingFormRequest") BookingFormRequest request,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("booking", bookingService.findById(id));
            return "booking/edit";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        bookingService.updateDates(id, request, currentUser);
        return "redirect:/bookings/mine";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        bookingService.cancel(id, currentUser);
        return "redirect:/bookings/mine";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        bookingService.decide(id, BookingStatus.APPROVED, currentUser);
        return "redirect:/bookings/requests";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        bookingService.decide(id, BookingStatus.REJECTED, currentUser);
        return "redirect:/bookings/requests";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        bookingService.decide(id, BookingStatus.COMPLETED, currentUser);
        return "redirect:/bookings/requests";
    }
}
