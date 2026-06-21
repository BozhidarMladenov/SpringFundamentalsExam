package com.softuni.gearshare.model.dto;

import com.softuni.gearshare.model.enums.EquipmentCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EquipmentFormRequest {

    @NotBlank(message = "Name is required.")
    @Size(min = 3, max = 80, message = "Name must be between 3 and 80 characters.")
    private String name;

    @NotBlank(message = "Description is required.")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters.")
    private String description;

    @NotNull(message = "Please select a category.")
    private EquipmentCategory category;

    @NotNull(message = "Daily price is required.")
    @DecimalMin(value = "0.01", message = "Daily price must be greater than zero.")
    @DecimalMax(value = "100000.00", message = "Daily price is unrealistically high.")
    private BigDecimal dailyPrice;

    @NotBlank(message = "Location is required.")
    @Size(min = 2, max = 80, message = "Location must be between 2 and 80 characters.")
    private String location;
}
