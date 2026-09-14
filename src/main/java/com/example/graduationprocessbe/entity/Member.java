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
@Table(name = "members")
@IdClass(MemberId.class)
public class Member {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "thesis_id")
    private String thesisId;
}

class MemberId implements Serializable {
    private String userId;
    private String thesisId;

    public MemberId() {}

    public MemberId(String userId, String thesisId) {
        this.userId = userId;
        this.thesisId = thesisId;
    }

    @Override
    public int hashCode() {
        return userId.hashCode() ^ thesisId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MemberId)) return false;
        MemberId that = (MemberId) obj;
        return this.userId.equals(that.userId) && this.thesisId.equals(that.thesisId);
    }
}
