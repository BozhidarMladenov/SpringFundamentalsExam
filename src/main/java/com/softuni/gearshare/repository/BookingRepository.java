package com.softuni.gearshare.repository;

import com.softuni.gearshare.model.entity.Booking;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findAllByRenter(User renter);

    List<Booking> findAllByEquipmentOwner(User owner);

    List<Booking> findAllByEquipmentAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Equipment equipment, List<BookingStatus> statuses, LocalDate end, LocalDate start);
}
