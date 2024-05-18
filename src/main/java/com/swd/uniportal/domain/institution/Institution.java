package com.swd.uniportal.domain.institution;

import com.swd.uniportal.domain.address.Address;
import com.swd.uniportal.domain.common.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
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
@Table(name = "institution")
public class Institution extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "website_1"))
    @AttributeOverride(name = "title", column = @Column(name = "website_title_1"))
    private Website website1;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "website_2"))
    @AttributeOverride(name = "title", column = @Column(name = "website_title_2"))
    private Website website2;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "website_3"))
    @AttributeOverride(name = "title", column = @Column(name = "website_title_3"))
    private Website website3;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_1"))
    @AttributeOverride(name = "title", column = @Column(name = "email_title_1"))
    private Email email1;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_2"))
    @AttributeOverride(name = "title", column = @Column(name = "email_title_2"))
    private Email email2;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_3"))
    @AttributeOverride(name = "title", column = @Column(name = "email_title_3"))
    private Email email3;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_1"))
    @AttributeOverride(name = "title", column = @Column(name = "phone_title_1"))
    private Phone phone1;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_2"))
    @AttributeOverride(name = "title", column = @Column(name = "phone_title_2"))
    private Phone phone2;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_3"))
    @AttributeOverride(name = "title", column = @Column(name = "phone_title_3"))
    private Phone phone3;

    @Column(name = "avatar_link")
    private String avatarLink;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Address> addresses = new ArrayList<>();

    public void addAddress(Address address) {
        addresses.add(address);
        address.setInstitution(this);
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