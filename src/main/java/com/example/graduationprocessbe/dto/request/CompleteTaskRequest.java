package com.example.graduationprocessbe.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CompleteTaskRequest {

    /**
     * Biến process truyền theo task, ví dụ:
     * facultyReviewProposal: {"proposalApproved": true, "feedback": "..."}
     * plagiarismCheck:       {"plagiarismPassed": false, "feedback": "..."}
     */
    private Map<String, Object> variables;
}
