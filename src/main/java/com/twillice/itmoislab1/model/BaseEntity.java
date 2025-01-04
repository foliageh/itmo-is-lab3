package com.twillice.itmoislab1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @JsonIgnore
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Name must not be empty")
    private String name;

    @Column(nullable = false)
    @CreationTimestamp
    @JsonIgnore
    private ZonedDateTime createdTime;

    @Column
    @UpdateTimestamp
    @JsonIgnore
    private ZonedDateTime updatedTime;

    @ManyToOne(optional = false)
    @JsonIgnore
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User updatedBy;

    @Column(nullable = false)
    @ColumnDefault("true")
    @JsonIgnore
    private Boolean editAllowed = true;

    public boolean isCreatedByUser(User user) {
        return createdBy.getUsername().equals(user.getUsername());
    }

    public boolean isEditableByUser(User user) {
        return isCreatedByUser(user) || user.hasRole(UserRole.ADMIN);
    }

    public boolean isDeletableByUser(User user) {
        return isCreatedByUser(user) || user.hasRole(UserRole.ADMIN) && editAllowed;
    }

    @Override
    public String toString() {
        return name;
    }
}