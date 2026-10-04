package com.example.graduationprocessbe.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Getter @Setter
@Table(name="role_allowed_permissions",uniqueConstraints=@UniqueConstraint(name="ux_role_allowed_pair",columnNames={"role_id","permission_id"}))
public class RoleAllowedPermission extends BaseEntity {
    @Column(name="role_id",nullable=false,length=36) private String roleId;
    @Column(name="permission_id",nullable=false,length=36) private String permissionId;
}
