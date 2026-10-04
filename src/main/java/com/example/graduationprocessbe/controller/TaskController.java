package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CompleteTaskRequest;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.service.ThesisProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.graduationprocessbe.util.ApiResponses.ok;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ThesisProcessService thesisProcessService;

    /** GET /api/tasks?assignee=<userId>&candidateGroup=FACULTY */
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<List<TaskResponse>>> findTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String candidateGroup) {
        return ok(thesisProcessService.findTasks(assignee, candidateGroup));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponseWrapper<TaskResponse>> getTask(@PathVariable String taskId) {
        return ok(thesisProcessService.getTask(taskId));
    }

    /** POST /api/tasks/{taskId}/claim?userId=<userId> */
    @PostMapping("/{taskId}/claim")
    public ResponseEntity<ApiResponseWrapper<TaskResponse>> claimTask(
            @PathVariable String taskId, @RequestParam String userId) {
        return ok(thesisProcessService.claimTask(taskId, userId));
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) CompleteTaskRequest request) {
        return ok(thesisProcessService.completeTask(taskId, request == null ? null : request.getVariables()));
    }
}
