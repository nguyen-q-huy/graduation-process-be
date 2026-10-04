package com.example.graduationprocessbe.entity;

import java.io.Serializable;
import java.util.Objects;

public class MemberId implements Serializable {
    private String userId;
    private String thesisId;

    public MemberId() {}

    public MemberId(String userId, String thesisId) {
        this.userId = userId;
        this.thesisId = thesisId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, thesisId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MemberId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(thesisId, that.thesisId);
    }
}
