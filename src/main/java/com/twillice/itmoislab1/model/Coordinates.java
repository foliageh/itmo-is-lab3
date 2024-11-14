package com.twillice.itmoislab1.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Embeddable
public class Coordinates {
    @Column(nullable = false)
    @NotNull(message = "Coordinate X must not be empty")
    private Float x;

    @Column(nullable = false)
    @NotNull(message = "Coordinate Y must not be empty")
    @Max(value = 913, message = "Coordinate Y must be <= 913")
    private Double y;
}
