package com.swd.uniportal.domain.student;

import com.swd.uniportal.domain.account.Account;
import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.institution.HighSchool;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
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
@Table(name = "student")
public class Student extends BaseEntity {

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "high_school_id")
    private HighSchool highSchool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @OneToMany(mappedBy = "student", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<StudentRecord> studentRecords;

    public void addStudentRecord(StudentRecord sr) {
        studentRecords.add(sr);
        sr.setStudent(this);
    }

    public void removeStudentRecord(StudentRecord sr) {
        studentRecords.remove(sr);
        sr.setStudent(null);
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
