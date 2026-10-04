package com.example.graduationprocessbe.controller;

import com.example.graduationprocessbe.dto.ApiResponseWrapper;
import com.example.graduationprocessbe.dto.request.CreateThesisRequest;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.service.ThesisProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/theses")
@RequiredArgsConstructor
public class ThesisController {

    private final ThesisProcessService thesisProcessService;

    @PostMapping
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> createThesis(
            @RequestBody @Valid CreateThesisRequest request) {
        return ok(thesisProcessService.createThesis(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ThesisResponse>> getThesis(@PathVariable String id) {
        return ok(thesisProcessService.getThesis(id));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<ApiResponseWrapper<List<TaskResponse>>> getThesisTasks(@PathVariable String id) {
        return ok(thesisProcessService.getThesisTasks(id));
    }

    private <T> ResponseEntity<ApiResponseWrapper<T>> ok(T data) {
        return ResponseEntity
                .status(ResponseDetails.API_SUCCESSFULLY.getHttpStatus())
                .body(new ApiResponseWrapper<>(ResponseDetails.API_SUCCESSFULLY, data));
    }
}
