package com.twillice.itmoislab1.view;

import com.twillice.itmoislab1.model.SpaceMarine;
import com.twillice.itmoislab1.service.ActionsService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Named @ViewScoped
@Getter @Setter
public class ActionsView implements Serializable {
    @Inject
    private ActionsService actionsService;

    private Long maxHealth;

    private Long health;
    private List<SpaceMarine> healthMarines;
    private List<Boolean> uniqueLoyalValues;

    public void calculateTotalHealth() {
        health = actionsService.calculateTotalHealth();
    }

    public void calculateMarinesWithHealthLessThan() {
        healthMarines = actionsService.getMarinesWithHealthLessThan(maxHealth);
    }

    public void calculateUniqueLoyalValues() {
        uniqueLoyalValues = actionsService.getUniqueLoyalValues();
    }
}
