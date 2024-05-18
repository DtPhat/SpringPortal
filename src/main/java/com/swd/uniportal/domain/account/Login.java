package com.swd.uniportal.domain.account;

import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.converter.LoginMethodConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "login")
public class Login extends BaseEntity {

    @Column(name = "password", unique = true)
    private String password;

    @Convert(converter = LoginMethodConverter.class)
    @Column(name = "method", nullable = false, length = 20)
    private LoginMethod method;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
