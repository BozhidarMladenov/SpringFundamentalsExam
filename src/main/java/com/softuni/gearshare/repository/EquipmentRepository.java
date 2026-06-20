package com.softuni.gearshare.repository;

import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    List<Equipment> findAllByAvailableTrue();

    List<Equipment> findAllByOwner(User owner);
}
