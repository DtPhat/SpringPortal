package com.swd.uniportal.domain.account;

import com.swd.uniportal.domain.common.BaseEntity;
import com.swd.uniportal.domain.converter.RoleConverter;
import com.swd.uniportal.domain.converter.StatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class Account extends BaseEntity implements UserDetails {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName = "Anonymous";

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "avatar_link")
    private String avatarLink;

    @Convert(converter = RoleConverter.class)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Convert(converter = StatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @SuppressWarnings("java:S1948")
    @OneToOne(mappedBy = "account")
    private Login login;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.asSecurityRole()));
    }

    @Override
    public String getPassword() {
        return getLogin().getPassword();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return (getStatus() == Status.ACTIVE);
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
