package com.example.graduationprocessbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menus")
public class Menu extends BaseEntity {

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(length = 100)
    private String icon;

    @Column(length = 200)
    private String path;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "permission_code", length = 100)
    private String permissionCode;

    @Column(nullable = false)
    private Boolean active = true;
}
