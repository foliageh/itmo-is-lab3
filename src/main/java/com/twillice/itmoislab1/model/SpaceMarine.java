package com.twillice.itmoislab1.model;

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
public class SpaceMarine extends BaseModel {
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

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Entity
    static class ChangeHistory extends EntityChangeHistory<SpaceMarine> {
    }
}