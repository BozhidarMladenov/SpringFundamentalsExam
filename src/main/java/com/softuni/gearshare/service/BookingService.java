package com.softuni.gearshare.service;

import com.softuni.gearshare.exception.EntityNotFoundException;
import com.softuni.gearshare.exception.InvalidBookingException;
import com.softuni.gearshare.exception.UnauthorizedActionException;
import com.softuni.gearshare.model.dto.BookingFormRequest;
import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.BookingStatus;
import com.softuni.gearshare.model.enums.UserRole;
import com.softuni.gearshare.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> BLOCKING_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.APPROVED);

    private final BookingRepository bookingRepository;
    private final EquipmentService equipmentService;

    public List<Booking> findAllForRenter(User renter) {
        return bookingRepository.findAllByRenter(renter);
    }

    public List<Booking> findAllForOwner(User owner) {
        return bookingRepository.findAllByEquipmentOwner(owner);
    }

    public Booking findById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }

    @Transactional
    public Booking create(UUID equipmentId, BookingFormRequest request, User renter) {
        if (renter.getRole() != UserRole.RENTER) {
            throw new UnauthorizedActionException("Only renters can create bookings.");
        }

        Equipment equipment = equipmentService.findById(equipmentId);

        if (!equipment.isAvailable()) {
            throw new InvalidBookingException("This equipment is currently unavailable for booking.");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new InvalidBookingException("End date must be after the start date.");
        }

        boolean overlaps = !bookingRepository
                .findAllByEquipmentAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        equipment, BLOCKING_STATUSES, request.getEndDate(), request.getStartDate())
                .isEmpty();
        if (overlaps) {
            throw new InvalidBookingException("This equipment is already booked for part of the selected period.");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        BigDecimal total = equipment.getDailyPrice().multiply(BigDecimal.valueOf(days));

        Booking booking = new Booking();
        booking.setEquipment(equipment);
        booking.setRenter(renter);
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalPrice(total);
        booking.setStatus(BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateDates(UUID id, BookingFormRequest request, User currentUser) {
        Booking booking = findById(id);
        requireRenterOwnership(booking, currentUser);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be edited.");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new InvalidBookingException("End date must be after the start date.");
        }

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        booking.setStartDate(request.getStartDate());
        booking.setEndDate(request.getEndDate());
        booking.setTotalPrice(booking.getEquipment().getDailyPrice().multiply(BigDecimal.valueOf(days)));
        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancel(UUID id, User currentUser) {
        Booking booking = findById(id);
        requireRenterOwnership(booking, currentUser);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("A completed booking cannot be cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional
    public Booking decide(UUID id, BookingStatus decision, User currentUser) {
        Booking booking = findById(id);

        if (!booking.getEquipment().getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the equipment owner can decide on this booking.");
        }
        if (booking.getStatus() != BookingStatus.PENDING && decision != BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Only pending bookings can be approved or rejected.");
        }
        if (decision == BookingStatus.COMPLETED && booking.getStatus() != BookingStatus.APPROVED) {
            throw new InvalidBookingException("Only approved bookings can be marked as completed.");
        }

        booking.setStatus(decision);
        return bookingRepository.save(booking);
    }

    private void requireRenterOwnership(Booking booking, User user) {
        if (!booking.getRenter().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You may only manage your own bookings.");
        }
    }
}
