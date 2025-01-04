package com.twillice.itmoislab1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter @Setter
public class SpaceMarine extends BaseEntity {
    @Embedded
    @Valid
    private Coordinates coordinates = new Coordinates();

    @ManyToOne(optional = false)
    @NotNull(message = "Chapter must not be empty")
    private Chapter chapter;

    @Column(nullable = false)
    @NotNull(message = "Health must not be empty")
    @Positive(message = "Health must be positive")
    private Long health;

    @Column(nullable = false)
    @NotNull(message = "Loyal must not be empty")
    private Boolean loyal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Category must not be empty")
    private AstartesCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Weapon must not be empty")
    private Weapon weapon;

    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChangeHistory> changeHistory = new ArrayList<>();

    public void setUpdateFields(User updatedBy, ZonedDateTime updatedTime) {
        setUpdatedBy(updatedBy);
        setUpdatedTime(updatedTime);

        var historyItem = new ChangeHistory();
        historyItem.setChangedBy(updatedBy);
        historyItem.setChangeTime(updatedTime);
        historyItem.setEntity(this);

        changeHistory.add(historyItem);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SpaceMarine that = (SpaceMarine) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    public final boolean equalsByFields(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SpaceMarine that = (SpaceMarine) o;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getLoyal(), that.getLoyal())
                && Objects.equals(getHealth(), that.getHealth())
                && Objects.equals(getEditAllowed(), that.getEditAllowed())
                && Objects.equals(getCategory(), that.getCategory())
                && Objects.equals(getWeapon(), that.getWeapon())
                && Objects.equals(getCoordinates(), that.getCoordinates())
                && Objects.equals(getChapter(), that.getChapter());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public SpaceMarine getCloneByFields() {
        var spaceMarine = new SpaceMarine();
        spaceMarine.setName(getName());
        spaceMarine.setLoyal(getLoyal());
        spaceMarine.setHealth(getHealth());
        spaceMarine.setEditAllowed(getEditAllowed());
        spaceMarine.setCategory(getCategory());
        spaceMarine.setWeapon(getWeapon());
        spaceMarine.setCoordinates(getCoordinates());
        spaceMarine.setChapter(getChapter());
        return spaceMarine;
    }

    @Entity
    static class ChangeHistory extends EntityChangeHistory<SpaceMarine> {
    }
}