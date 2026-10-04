package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateDepartmentRequest;
import com.example.graduationprocessbe.dto.request.UpdateDepartmentRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;

public interface DepartmentService {

    DepartmentResponse create(CreateDepartmentRequest request);

    DepartmentResponse getById(String id);

    PageResponse<DepartmentResponse> search(String keyword, int page, int size, String sortBy, String direction);

    DepartmentResponse update(String id, UpdateDepartmentRequest request);

    void delete(String id);
}
