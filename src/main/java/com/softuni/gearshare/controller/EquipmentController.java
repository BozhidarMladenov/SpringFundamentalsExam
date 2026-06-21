package com.softuni.gearshare.controller;

import com.softuni.gearshare.config.CurrentUserProvider;
import com.softuni.gearshare.model.dto.EquipmentFormRequest;
import com.softuni.gearshare.model.entity.Equipment;
import com.softuni.gearshare.model.entity.User;
import com.softuni.gearshare.model.enums.EquipmentCategory;
import com.softuni.gearshare.service.EquipmentService;
import com.softuni.gearshare.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("equipmentList", equipmentService.findAllAvailable());
        return "equipment/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        Equipment equipment = equipmentService.findById(id);
        model.addAttribute("equipment", equipment);
        model.addAttribute("reviews", reviewService.findAllForEquipment(equipment));
        return "equipment/details";
    }

    @GetMapping("/mine")
    public String myListings(Model model) {
        User currentUser = currentUserProvider.getCurrentUser();
        model.addAttribute("equipmentList", equipmentService.findAllOwnedBy(currentUser));
        return "equipment/mine";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("equipmentFormRequest")) {
            model.addAttribute("equipmentFormRequest", new EquipmentFormRequest());
        }
        model.addAttribute("categories", EquipmentCategory.values());
        return "equipment/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("equipmentFormRequest") EquipmentFormRequest request,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", EquipmentCategory.values());
            return "equipment/create";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        Equipment created = equipmentService.create(request, currentUser);
        return "redirect:/equipment/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        Equipment equipment = equipmentService.findById(id);

        EquipmentFormRequest form = new EquipmentFormRequest();
        form.setName(equipment.getName());
        form.setDescription(equipment.getDescription());
        form.setCategory(equipment.getCategory());
        form.setDailyPrice(equipment.getDailyPrice());
        form.setLocation(equipment.getLocation());

        model.addAttribute("equipmentFormRequest", form);
        model.addAttribute("categories", EquipmentCategory.values());
        model.addAttribute("equipmentId", id);
        return "equipment/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                        @Valid @ModelAttribute("equipmentFormRequest") EquipmentFormRequest request,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", EquipmentCategory.values());
            model.addAttribute("equipmentId", id);
            return "equipment/edit";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        equipmentService.update(id, request, currentUser);
        return "redirect:/equipment/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        equipmentService.delete(id, currentUser);
        return "redirect:/equipment/mine";
    }

    @PostMapping("/{id}/toggle-availability")
    public String toggleAvailability(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        equipmentService.toggleAvailability(id, currentUser);
        return "redirect:/equipment/mine";
    }
}
