package com.swd.uniportal.domain.subject;

import com.swd.uniportal.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subject_group")
public class SubjectGroup extends BaseEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToMany
    @JoinTable(
            name = "subject_group_subject",
            joinColumns = @JoinColumn(name = "subject_group_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uc_subject_group_subject",
                    columnNames = { "subject_group_id", "subject_id" }
            )
    )
    private Set<Subject> subjects;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
