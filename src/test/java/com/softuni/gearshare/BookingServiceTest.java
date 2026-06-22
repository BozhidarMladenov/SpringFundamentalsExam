package com.softuni.gearshare;

import com.softuni.gearshare.exception.InvalidBookingException;
import com.softuni.gearshare.model.dto.BookingFormRequest;
import com.softuni.gearshare.model.dto.EquipmentFormRequest;
import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.BookingStatus;
import com.softuni.gearshare.model.enums.EquipmentCategory;
import com.softuni.gearshare.model.enums.UserRole;
import com.softuni.gearshare.repository.UserRepository;
import com.softuni.gearshare.service.BookingService;
import com.softuni.gearshare.service.EquipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User owner;
    private User renter;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setUsername("owner1");
        owner.setEmail("owner1@example.com");
        owner.setFullName("Owner One");
        owner.setPasswordHash(passwordEncoder.encode("password123"));
        owner.setRole(UserRole.OWNER);
        owner = userRepository.save(owner);

        renter = new User();
        renter.setUsername("renter1");
        renter.setEmail("renter1@example.com");
        renter.setFullName("Renter One");
        renter.setPasswordHash(passwordEncoder.encode("password123"));
        renter.setRole(UserRole.RENTER);
        renter = userRepository.save(renter);

        EquipmentFormRequest form = new EquipmentFormRequest();
        form.setName("Test Camera");
        form.setDescription("A camera used for integration testing purposes.");
        form.setCategory(EquipmentCategory.CAMERA);
        form.setDailyPrice(new BigDecimal("20.00"));
        form.setLocation("Sofia");
        equipment = equipmentService.create(form, owner);
    }

    @Test
    void createBooking_calculatesTotalPriceCorrectly() {
        BookingFormRequest request = new BookingFormRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(4));

        Booking booking = bookingService.create(equipment.getId(), request, renter);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getTotalPrice()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void createBooking_rejectsOverlappingDates() {
        BookingFormRequest first = new BookingFormRequest();
        first.setStartDate(LocalDate.now().plusDays(1));
        first.setEndDate(LocalDate.now().plusDays(5));
        bookingService.create(equipment.getId(), first, renter);

        BookingFormRequest overlapping = new BookingFormRequest();
        overlapping.setStartDate(LocalDate.now().plusDays(3));
        overlapping.setEndDate(LocalDate.now().plusDays(6));

        assertThatThrownBy(() -> bookingService.create(equipment.getId(), overlapping, renter))
                .isInstanceOf(InvalidBookingException.class);
    }

    @Test
    void decide_ownerCanApprovePendingBooking() {
        BookingFormRequest request = new BookingFormRequest();
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        Booking booking = bookingService.create(equipment.getId(), request, renter);

        Booking approved = bookingService.decide(booking.getId(), BookingStatus.APPROVED, owner);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }
}
