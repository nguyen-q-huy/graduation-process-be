package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.CreateDepartmentRequest;
import com.example.graduationprocessbe.dto.request.UpdateDepartmentRequest;
import com.example.graduationprocessbe.dto.response.DepartmentResponse;
import com.example.graduationprocessbe.entity.Department;
import com.example.graduationprocessbe.exception.ApplicationException;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.exception.ResponseDetails;
import com.example.graduationprocessbe.mapper.DepartmentMapper;
import com.example.graduationprocessbe.repository.DepartmentRepository;
import com.example.graduationprocessbe.service.DepartmentService;
import com.example.graduationprocessbe.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private static final Set<String> SORT_FIELDS = Set.of("deptCode", "deptName", "createdDate");

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByDeptCode(request.getDeptCode())) {
            throw new ApplicationException(ResponseDetails.DATA_EXISTED);
        }
        Department department = new Department();
        department.setDeptCode(request.getDeptCode());
        department.setDeptName(request.getDeptName());
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(String id) {
        return departmentMapper.toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> search(String keyword, int page, int size, String sortBy, String direction) {
        Specification<Department> spec = (root, query, cb) -> cb.conjunction();
        if (keyword != null && !keyword.isBlank()) {
            String pattern = PageUtil.likePattern(keyword);
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("deptCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("deptName")), pattern, '\\')));
        }
        Page<Department> result = departmentRepository.findAll(spec,
                PageUtil.of(page, size, sortBy, direction, SORT_FIELDS, "createdDate"));
        return PageResponse.from(result, departmentMapper::toResponse);
    }

    @Override
    @Transactional
    public DepartmentResponse update(String id, UpdateDepartmentRequest request) {
        Department department = find(id);
        if (request.getDeptCode() != null && !request.getDeptCode().equals(department.getDeptCode())) {
            if (departmentRepository.existsByDeptCode(request.getDeptCode())) {
                throw new ApplicationException(ResponseDetails.DATA_EXISTED);
            }
            department.setDeptCode(request.getDeptCode());
        }
        if (request.getDeptName() != null) {
            department.setDeptName(request.getDeptName());
        }
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    /** Khoa đang có user tham chiếu thì DB từ chối; GlobalExceptionHandler trả 409. */
    @Override
    @Transactional
    public void delete(String id) {
        departmentRepository.delete(find(id));
        departmentRepository.flush();
    }

    private Department find(String id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }
}
