package com.swd.uniportal.domain.admission;

import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.major.Major;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "admission_major")
public class AdmissionMajor extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "quota", nullable = false)
    private Integer quota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_plan_id", nullable = false)
    private AdmissionPlan admissionPlan;

    @OneToMany(mappedBy = "admissionMajor", orphanRemoval = true)
    private Set<AdmissionMajorMethod> admissionMajorMethods = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admission_training_program_id")
    private AdmissionTrainingProgram admissionTrainingProgram;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
