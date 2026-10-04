package com.example.graduationprocessbe.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "thesis_rounds")
public class ThesisRound extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false)
    private Boolean active = true;
}
