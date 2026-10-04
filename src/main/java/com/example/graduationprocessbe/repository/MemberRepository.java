package com.example.graduationprocessbe.repository;

import com.example.graduationprocessbe.entity.Member;
import com.example.graduationprocessbe.entity.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, MemberId> {
    List<Member> findByThesisId(String thesisId);
    List<Member> findByUserId(String userId);

    void deleteByThesisId(String thesisId);

    void deleteByUserId(String userId);
}
