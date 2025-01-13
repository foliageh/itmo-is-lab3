package com.twillice.itmoislab1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter @Setter
public class Chapter extends BaseEntity {
    @Column
    private String parentLegion;  // may be empty

    @Column
    @Min(value = 1, message = "Marines count must be either > 0 and <= 1000, or empty")
    @Max(value = 1000, message = "Marines count must be either > 0 and <= 1000, or empty")
    private Integer marinesCount;

    @Column
    private String world;  // may be empty

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
        Chapter chapter = (Chapter) o;
        return getId() != null && Objects.equals(getId(), chapter.getId());
    }

    public final boolean equalsByFields(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Chapter chapter = (Chapter) o;
        return getName() != null && Objects.equals(getName(), chapter.getName())
                && Objects.equals(getParentLegion(), chapter.getParentLegion())
                && Objects.equals(getMarinesCount(), chapter.getMarinesCount())
                && Objects.equals(getWorld(), chapter.getWorld())
                && Objects.equals(getEditAllowed(), chapter.getEditAllowed());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @JsonIgnore
    public Chapter getCloneByFields() {
        var chapter = new Chapter();
        chapter.setName(getName());
        chapter.setParentLegion(getParentLegion());
        chapter.setMarinesCount(getMarinesCount());
        chapter.setWorld(getWorld());
        chapter.setEditAllowed(getEditAllowed());
        return chapter;
    }

    @Entity
    static class ChangeHistory extends EntityChangeHistory<Chapter> {
    }
}