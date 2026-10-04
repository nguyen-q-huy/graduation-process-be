package com.example.graduationprocessbe.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityHistoryResponse {
    private String activityId;
    private String activityName;
    private String activityType;
    private String assignee;
    private LocalDateTime startTime;
    /** null nghĩa là bước đang chạy. */
    private LocalDateTime endTime;
}
