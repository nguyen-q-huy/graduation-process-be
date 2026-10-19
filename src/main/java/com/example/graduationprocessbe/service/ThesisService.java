package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.dto.PageResponse;
import com.example.graduationprocessbe.dto.request.UpdateThesisRequest;
import com.example.graduationprocessbe.dto.response.ActivityHistoryResponse;
import com.example.graduationprocessbe.dto.response.MemberResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;

import java.util.List;

/** CRUD, tìm kiếm, thành viên và lịch sử của đề tài. Việc start/complete task nằm ở ThesisProcessService. */
public interface ThesisService {

    PageResponse<ThesisResponse> search(String keyword, String studentId, String lecturerId, String status,
                                        String phaseId, int page, int size, String sortBy, String direction);

    ThesisResponse update(String id, UpdateThesisRequest request);

    /** Xoá đề tài, dừng process đang chạy (nếu có) và xoá thành viên. Audit log được giữ lại. */
    void delete(String id);

    List<MemberResponse> getMembers(String thesisId);

    List<MemberResponse> addMember(String thesisId, String userId);

    List<MemberResponse> removeMember(String thesisId, String userId);

    /** The group leader locks (or unlocks) another member of the group. */
    List<MemberResponse> setMemberLocked(String thesisId, String userId, boolean locked);

    /** The advising lecturer, Faculty staff or an admin hand the group leadership to another member of the group. */
    List<MemberResponse> changeLeader(String thesisId, String newLeaderId);

    /** Các bước process đã và đang đi qua, theo thứ tự thời gian. */
    List<ActivityHistoryResponse> getHistory(String thesisId);
}
