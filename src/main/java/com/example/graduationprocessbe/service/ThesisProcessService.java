package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.request.CreateThesisRequest;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;

import java.util.List;
import java.util.Map;

public interface ThesisProcessService {

    /** Tạo Thesis và start process graduationProcess. */
    ThesisResponse createThesis(CreateThesisRequest request);

    ThesisResponse getThesis(String thesisId);

    /** Các task đang chờ của một Thesis. */
    List<TaskResponse> getThesisTasks(String thesisId);

    /** Tìm task theo assignee và/hoặc candidateGroup; để null để bỏ qua điều kiện. */
    List<TaskResponse> findTasks(String assignee, String candidateGroup);

    /** Hoàn tất task, truyền biến vào process, cập nhật currentStatus của Thesis. */
    ThesisResponse completeTask(String taskId, Map<String, Object> variables);
}
