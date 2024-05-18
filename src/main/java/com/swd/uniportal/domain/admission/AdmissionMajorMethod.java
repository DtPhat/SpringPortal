package com.swd.uniportal.domain.admission;

import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.subject.SubjectGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
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
@Table(name = "admission_major_method")
public class AdmissionMajorMethod extends BaseEntity {

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_major_id")
    private AdmissionMajor admissionMajor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_method_id")
    private AdmissionMethod admissionMethod;

    @ManyToMany
    @JoinTable(name = "admission_major_method_subject_group",
            joinColumns = { @JoinColumn(name = "admission_major_method_id") },
            inverseJoinColumns = { @JoinColumn(name = "subject_group_id") })
    private Set<SubjectGroup> subjectGroups = new HashSet<>();

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
