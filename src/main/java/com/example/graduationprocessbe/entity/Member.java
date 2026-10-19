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

    @Column(name = "thesis_round_id", nullable = false)
    private String thesisRoundId;

    /** A locked member can neither see nor act on the thesis; only the group leader (theses.student_id) toggles it. */
    @Column(name = "locked", nullable = false, columnDefinition = "boolean default false")
    private boolean locked;

    public Member(String userId,String thesisId) { this.userId=userId; this.thesisId=thesisId; }
}
