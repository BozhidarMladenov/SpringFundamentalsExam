package com.softuni.gearshare.service;

import com.softuni.gearshare.exception.EntityNotFoundException;
import com.softuni.gearshare.exception.UnauthorizedActionException;
import com.softuni.gearshare.model.dto.EquipmentFormRequest;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.UserRole;
import com.softuni.gearshare.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public List<Equipment> findAllAvailable() {
        return equipmentRepository.findAllByAvailableTrue();
    }

    public List<Equipment> findAllOwnedBy(User owner) {
        return equipmentRepository.findAllByOwner(owner);
    }

    public Equipment findById(UUID id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));
    }

    @Transactional
    public Equipment create(EquipmentFormRequest request, User owner) {
        requireOwnerRole(owner);

        Equipment equipment = new Equipment();
        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setCategory(request.getCategory());
        equipment.setDailyPrice(request.getDailyPrice());
        equipment.setLocation(request.getLocation());
        equipment.setAvailable(true);
        equipment.setOwner(owner);
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public Equipment update(UUID id, EquipmentFormRequest request, User currentUser) {
        Equipment equipment = findById(id);
        requireOwnership(equipment, currentUser);

        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setCategory(request.getCategory());
        equipment.setDailyPrice(request.getDailyPrice());
        equipment.setLocation(request.getLocation());
        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void delete(UUID id, User currentUser) {
        Equipment equipment = findById(id);
        requireOwnership(equipment, currentUser);
        equipmentRepository.delete(equipment);
    }

    @Transactional
    public Equipment toggleAvailability(UUID id, User currentUser) {
        Equipment equipment = findById(id);
        requireOwnership(equipment, currentUser);
        equipment.setAvailable(!equipment.isAvailable());
        return equipmentRepository.save(equipment);
    }

    private void requireOwnerRole(User user) {
        if (user.getRole() != UserRole.OWNER) {
            throw new UnauthorizedActionException("Only owners can list equipment for rent.");
        }
    }

    private void requireOwnership(Equipment equipment, User user) {
        if (!equipment.getOwner().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You may only manage your own equipment listings.");
        }
    }
}
