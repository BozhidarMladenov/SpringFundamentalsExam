package com.softuni.gearshare.repository;

import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByBookingEquipment(Equipment equipment);

    Optional<Review> findByBookingId(UUID bookingId);
}
