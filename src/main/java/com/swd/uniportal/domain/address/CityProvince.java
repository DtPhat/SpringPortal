package com.swd.uniportal.domain.address;

import com.swd.uniportal.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "city_province")
public class CityProvince extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "cityProvince", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<District> districts;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
