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
public class Website {

    @Column(name = "website")
    private String value;

    @Column(name = "website_title")
    private String title;
}
