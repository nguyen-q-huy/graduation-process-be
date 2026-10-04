package com.example.graduationprocessbe.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Cập nhật từng phần: field null thì giữ nguyên.
 * currentStatus không sửa được qua đây vì do Flowable quản lý.
 */
@Getter
@Setter
public class UpdateThesisRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private String lecturerId;

    private String phaseId;
}
