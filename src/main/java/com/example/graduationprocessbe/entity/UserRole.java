package com.example.graduationprocessbe.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleId.class)
public class UserRole {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "role_id")
    private String roleId;
}

class UserRoleId implements Serializable {
    private String userId;
    private String roleId;

    public UserRoleId() {}

    public UserRoleId(String userId, String roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    @Override
    public int hashCode() {
        return userId.hashCode() ^ roleId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserRoleId)) return false;
        UserRoleId that = (UserRoleId) obj;
        return this.userId.equals(that.userId) && this.roleId.equals(that.roleId);
    }
}
