package com.swd.uniportal.domain.admission;

import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.institution.Institution;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "admission_plan")
public class AdmissionPlan extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "year", nullable = false)
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @OneToMany(mappedBy = "admissionPlan", cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    private Set<AdmissionMajor> admissionMajors;

    @OneToMany(mappedBy = "admissionPlan", cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, orphanRemoval = true)
    private Set<AdmissionTrainingProgram> admissionTrainingPrograms;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
