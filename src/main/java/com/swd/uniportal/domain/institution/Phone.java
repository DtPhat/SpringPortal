package com.swd.uniportal.domain.institution;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
@Embeddable
public class Phone {

    @Column(name = "phone", length = 20)
    private String value;

    @Column(name = "phone_title")
    private String title;
}
