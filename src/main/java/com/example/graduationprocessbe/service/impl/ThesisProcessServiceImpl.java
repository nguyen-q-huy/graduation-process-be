package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateThesisRequest;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.entity.Thesis;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.mapper.ThesisMapper;
import com.example.graduationprocessbe.repository.ThesisRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.service.AuditLogService;
import com.example.graduationprocessbe.service.CurrentUserService;
import com.example.graduationprocessbe.service.ThesisProcessService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ThesisProcessServiceImpl implements ThesisProcessService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final ThesisRepository thesisRepository;
    private final UserRepository userRepository;
    private final ThesisMapper thesisMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public ThesisResponse createThesis(CreateThesisRequest request) {
        User actor=currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if (!"STUDENT".equals(actor.getUserType()) || !actor.getId().equals(request.getStudentId()))
            throw new AccessDeniedException("Chỉ sinh viên được đăng ký đề tài của mình");
        User student = findUser(request.getStudentId());
        User lecturer = findUser(request.getLecturerId());
        if (!"LECTURER".equals(lecturer.getUserType())) throw new IllegalArgumentException("GVHD phải là giảng viên");
        List<Map<String,Object>> rounds=jdbc.queryForList("SELECT registration_opens_at,registration_closes_at FROM thesis_rounds WHERE id=? AND active=true",request.getPhaseId());
        if (rounds.isEmpty()) throw new IllegalArgumentException("Đợt ĐATN không hoạt động");
        Map<String,Object> round=rounds.getFirst();
        OffsetDateTime now=OffsetDateTime.now();
        if (round.get("registration_opens_at")==null || round.get("registration_closes_at")==null)
            throw new IllegalArgumentException("Đợt chưa có đủ mốc đăng ký");
        OffsetDateTime opens=asOffset(round.get("registration_opens_at"));
        OffsetDateTime closes=asOffset(round.get("registration_closes_at"));
        if (now.isBefore(opens) || now.isAfter(closes))
            throw new IllegalArgumentException("Ngoài thời gian đăng ký đề tài");
        Integer registered=jdbc.queryForObject("SELECT count(*) FROM round_lecturers WHERE round_id=? AND lecturer_id=? AND active=true",Integer.class,request.getPhaseId(),lecturer.getId());
        if (registered==null || registered==0) throw new IllegalArgumentException("Giảng viên chưa tham gia đợt này");
        Integer existing=jdbc.queryForObject("SELECT count(*) FROM theses WHERE phase_id=? AND student_id=?",Integer.class,request.getPhaseId(),student.getId());
        if (existing!=null && existing>0) throw new IllegalArgumentException("Sinh viên đã đăng ký trong đợt này");
        Integer memberExisting=jdbc.queryForObject("SELECT count(*) FROM members WHERE thesis_round_id=? AND user_id=?",Integer.class,request.getPhaseId(),student.getId());
        if (memberExisting!=null && memberExisting>0) throw new IllegalArgumentException("Sinh viên đã tham gia nhóm khác trong đợt này");
        User partner=null;
        if (request.getPartnerStudentId()!=null && !request.getPartnerStudentId().isBlank()) {
            partner=findUser(request.getPartnerStudentId());
            if (partner.getId().equals(student.getId()) || !"STUDENT".equals(partner.getUserType()) || !"ACTIVE".equals(partner.getStatus()))
                throw new IllegalArgumentException("Sinh viên cùng nhóm không hợp lệ");
            Integer partnerExisting=jdbc.queryForObject("SELECT count(*) FROM members WHERE thesis_round_id=? AND user_id=?",Integer.class,request.getPhaseId(),partner.getId());
            if (partnerExisting!=null && partnerExisting>0) throw new IllegalArgumentException("Sinh viên cùng nhóm đã có đề tài trong đợt");
        }

        Thesis thesis = new Thesis();
        thesis.setTitle(request.getTitle());
        thesis.setStudent(student);
        thesis.setLecturer(lecturer);
        thesis.setPhaseId(request.getPhaseId());
        thesis.setCurrentStatus("PENDING_SUPERVISOR");
        thesis = thesisRepository.saveAndFlush(thesis);
        jdbc.update("INSERT INTO members(thesis_id,user_id,thesis_round_id) VALUES(?,?,?)",thesis.getId(),student.getId(),request.getPhaseId());
        if (partner!=null) jdbc.update("INSERT INTO members(thesis_id,user_id,thesis_round_id) VALUES(?,?,?)",thesis.getId(),partner.getId(),request.getPhaseId());
        return thesisMapper.toResponse(thesis);
    }

    @Override
    public ThesisResponse submitProposal(String thesisId, String content) {
        // Initial proposal approval and signatures are handled outside the system.
        getThesis(thesisId);
        throw new IllegalArgumentException("Đề cương được trao đổi ngoài hệ thống. Đăng ký cần giảng viên xác nhận hướng dẫn.");
    }

    @Override
    @Transactional
    public ThesisResponse confirmGuidance(String thesisId, boolean approved, String comment) {
        jdbc.queryForObject("SELECT id FROM theses WHERE id=? FOR UPDATE", String.class, thesisId);
        Thesis thesis = findThesis(thesisId);
        User actor = currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if (!"LECTURER".equals(actor.getUserType()) || !thesis.getLecturer().getId().equals(actor.getId()))
            throw new AccessDeniedException("Chỉ giảng viên được chọn mới được xác nhận hướng dẫn");
        if (!pendingGuidance(thesis)) throw new IllegalArgumentException("Đăng ký đã được xử lý");
        String note = comment == null ? "" : comment.trim();
        if (note.length() > 2000) throw new IllegalArgumentException("Nhận xét tối đa 2000 ký tự");
        if (!approved && note.isBlank()) throw new IllegalArgumentException("Cần ghi lý do từ chối hướng dẫn");
        thesis.setGuidanceApproved(approved);
        thesis.setGuidanceRespondedAt(java.time.LocalDateTime.now());
        thesis.setGuidanceComment(note);
        if (approved) startConfirmedWorkflow(thesis, actor);
        else thesis.setCurrentStatus("GUIDANCE_REJECTED");
        jdbc.update("INSERT INTO thesis_feedback(id,thesis_id,step_key,reviewer_id,approved,comment) VALUES(?,?,?,?,?,?)",
                UUID.randomUUID().toString(), thesisId, "confirmGuidance", actor.getId(), approved,
                note.isBlank() ? "Đồng ý nhận hướng dẫn" : note);
        return thesisMapper.toResponse(thesisRepository.saveAndFlush(thesis));
    }

    @Override
    @Transactional
    public ThesisResponse resubmitRegistration(String thesisId, String title, String lecturerId) {
        jdbc.queryForObject("SELECT id FROM theses WHERE id=? FOR UPDATE", String.class, thesisId);
        Thesis thesis = findThesis(thesisId);
        User actor = currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if (!"STUDENT".equals(actor.getUserType()) || !actor.getId().equals(thesis.getStudent().getId()))
            throw new AccessDeniedException("Chỉ sinh viên đăng ký được gửi lại đăng ký của nhóm");
        if (!"GUIDANCE_REJECTED".equals(thesis.getCurrentStatus()) || thesis.getProcessInstanceId() != null)
            throw new IllegalArgumentException("Chỉ đăng ký bị từ chối mới được gửi lại");
        if (title == null || title.isBlank() || title.trim().length() > 255)
            throw new IllegalArgumentException("Tên đề tài không hợp lệ");
        var rounds = jdbc.queryForList("SELECT registration_opens_at,registration_closes_at FROM thesis_rounds WHERE id=? AND active=true", thesis.getPhaseId());
        OffsetDateTime now = OffsetDateTime.now();
        if (rounds.isEmpty() || rounds.getFirst().get("registration_opens_at") == null || rounds.getFirst().get("registration_closes_at") == null
                || now.isBefore(asOffset(rounds.getFirst().get("registration_opens_at"))) || now.isAfter(asOffset(rounds.getFirst().get("registration_closes_at"))))
            throw new IllegalArgumentException("Ngoài thời gian đăng ký đề tài");
        User lecturer = findUser(lecturerId);
        Integer available = jdbc.queryForObject("SELECT count(*) FROM round_lecturers WHERE round_id=? AND lecturer_id=? AND active=true", Integer.class, thesis.getPhaseId(), lecturerId);
        if (!"LECTURER".equals(lecturer.getUserType()) || !"ACTIVE".equals(lecturer.getStatus()) || available == null || available == 0)
            throw new IllegalArgumentException("Giảng viên chưa tham gia đợt này hoặc đã ngừng hoạt động");
        thesis.setTitle(title.trim());
        thesis.setLecturer(lecturer);
        thesis.setCurrentStatus("PENDING_SUPERVISOR");
        thesis.setGuidanceApproved(null);
        thesis.setGuidanceRespondedAt(null);
        thesis.setGuidanceComment(null);
        return thesisMapper.toResponse(thesisRepository.saveAndFlush(thesis));
    }

    private boolean pendingGuidance(Thesis thesis) {
        return thesis.getProcessInstanceId() == null
                && ("PENDING_SUPERVISOR".equals(thesis.getCurrentStatus()) || "REGISTERED".equals(thesis.getCurrentStatus()));
    }

    private void startConfirmedWorkflow(Thesis thesis, User actor) {
        var definitions = jdbc.queryForList("SELECT workflow_definition_id FROM thesis_rounds WHERE id=?", thesis.getPhaseId());
        if (definitions.isEmpty() || definitions.getFirst().get("workflow_definition_id") == null)
            throw new IllegalArgumentException("Đợt chưa được gán quy trình đã công bố");
        String definition = (String) definitions.getFirst().get("workflow_definition_id");
        // Published templates keep their version. Skip initial proposal submission/review,
        // which have already happened outside the system, without completing a fake approval.
        List<String> nextSteps = jdbc.queryForList("SELECT ws.step_key FROM workflow_steps ws JOIN workflow_templates wt ON wt.id=ws.template_id WHERE wt.process_definition_id=? ORDER BY ws.sort_order OFFSET 2 LIMIT 1", String.class, definition);
        if (nextSteps.isEmpty()) throw new IllegalArgumentException("Quy trình cần có bước thực hiện sau xác nhận hướng dẫn");
        List<String> students = jdbc.queryForList("SELECT user_id FROM members WHERE thesis_id=? ORDER BY user_id", String.class, thesis.getId());
        Map<String,Object> variables = new HashMap<>();
        variables.put("thesisId", thesis.getId()); variables.put("studentId", thesis.getStudent().getId());
        variables.put("studentIds", String.join(",", students)); variables.put("studentEmail", thesis.getStudent().getEmail());
        variables.put("lecturerId", thesis.getLecturer().getId()); variables.put("guidanceConfirmed", true);
        ProcessInstance instance = runtimeService.startProcessInstanceById(definition, thesis.getId(), variables);
        thesis.setProcessInstanceId(instance.getId());
        var initialTasks = taskService.createTaskQuery().processInstanceId(instance.getId()).list();
        runtimeService.createChangeActivityStateBuilder().processInstanceId(instance.getId())
                .moveActivityIdsToSingleActivityId(initialTasks.stream().map(Task::getTaskDefinitionKey).distinct().toList(), nextSteps.getFirst()).changeState();
        refreshStatus(thesis);
        auditLogService.record(instance.getId(), actor, "CONFIRM_GUIDANCE", "confirmGuidance", Map.of("thesisId", thesis.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ThesisResponse getThesis(String thesisId) {
        Thesis thesis=findThesis(thesisId);
        authorizeThesis(thesis);
        return thesisMapper.toResponse(thesis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getThesisTasks(String thesisId) {
        Thesis thesis = findThesis(thesisId);
        authorizeThesis(thesis);
        if (thesis.getProcessInstanceId() == null) {
            return List.of();
        }
        return taskService.createTaskQuery()
                .processInstanceId(thesis.getProcessInstanceId())
                .active()
                .orderByTaskCreateTime().asc()
                .list()
                .stream().map(this::toTaskResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findTasks(String assignee, String candidateGroup) {
        User actor=currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if (!"ADMIN".equals(actor.getUserType())) {
            if (assignee!=null && !assignee.equals(actor.getId())) throw new AccessDeniedException("Không được xem tác vụ của người khác");
            if (candidateGroup!=null && !hasRole(actor.getId(),candidateGroup)) throw new AccessDeniedException("Không thuộc nhóm xử lý");
            if (assignee==null && candidateGroup==null) return java.util.stream.Stream.concat(
                taskService.createTaskQuery().taskAssignee(actor.getId()).active().list().stream(),
                java.util.stream.Stream.concat(
                    taskService.createTaskQuery().taskCandidateGroupIn(roles(actor.getId())).active().list().stream(),
                    taskService.createTaskQuery().taskCandidateUser(actor.getId()).active().list().stream()))
                .distinct().filter(task -> canAccessTask(task,actor)).map(this::toTaskResponse).toList();
        }
        TaskQuery query = taskService.createTaskQuery().active();
        if (assignee != null) {
            query.taskAssignee(assignee);
        }
        if (candidateGroup != null) {
            query.taskCandidateGroup(candidateGroup);
        }
        return query.orderByTaskCreateTime().asc().list()
                .stream().filter(task -> canAccessTask(task,actor)).map(this::toTaskResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(String taskId) {
        Task task=findTask(taskId);
        authorizeTask(task,currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập")));
        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse claimTask(String taskId, String userId) {
        Task task=findTask(taskId);
        User actor=currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if (!actor.getId().equals(userId)) throw new AccessDeniedException("Chỉ được nhận việc cho bản thân");
        authorizeTask(task,actor);
        findUser(userId);
        taskService.claim(taskId, userId);
        return toTaskResponse(findTask(taskId));
    }

    @Override
    @Transactional
    public ThesisResponse completeTask(String taskId, Map<String, Object> variables) {
        Task task = findTask(taskId);
        User actor=currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        authorizeTask(task,actor);
        Thesis thesis = thesisRepository.findByProcessInstanceId(task.getProcessInstanceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thesis not found for process instance: " + task.getProcessInstanceId()));
        Map<String, Object> vars = variables == null ? Map.of() : variables;
        List<Map<String,Object>> step=jdbc.queryForList("SELECT ws.kind FROM workflow_steps ws JOIN workflow_templates wt ON wt.id=ws.template_id WHERE wt.process_definition_id=? AND ws.step_key=?",task.getProcessDefinitionId(),task.getTaskDefinitionKey());
        if (!step.isEmpty()) {
            String kind=(String)step.getFirst().get("kind");
            if (kind.equals("SUBMIT")) {
                // submitProposal tasks are revision requests after faculty feedback;
                // the initial proposal window only governs first registration.
                List<Map<String,Object>> windows="submitProposal".equals(task.getTaskDefinitionKey()) ? List.of()
                    : jdbc.queryForList("SELECT opens_at,closes_at FROM round_step_windows WHERE round_id=? AND step_key=?",thesis.getPhaseId(),task.getTaskDefinitionKey());
                if (!windows.isEmpty()) {
                    OffsetDateTime current=OffsetDateTime.now();
                    Map<String,Object> window=windows.getFirst();
                    if (current.isBefore(asOffset(window.get("opens_at"))) || current.isAfter(asOffset(window.get("closes_at"))))
                        throw new IllegalArgumentException("Biểu mẫu bước này chưa mở hoặc đã hết hạn");
                }
                Object content=vars.get("content");
                if (!(content instanceof String text) || text.isBlank()) throw new IllegalArgumentException("Cần nội dung hoặc liên kết hồ sơ nộp");
                jdbc.update("INSERT INTO thesis_submissions(id,thesis_id,step_key,submitted_by,content,attachment_url) VALUES(?,?,?,?,?,?)",
                    UUID.randomUUID().toString(),thesis.getId(),task.getTaskDefinitionKey(),actor.getId(),content,vars.get("attachmentUrl"));
            } else if (kind.equals("REVIEW")) {
                if (!(vars.get("approved") instanceof Boolean approved)) throw new IllegalArgumentException("Cần chọn duyệt hoặc yêu cầu sửa");
                String comment=String.valueOf(vars.getOrDefault("comment",""));
                if (!approved && comment.isBlank()) throw new IllegalArgumentException("Cần ghi rõ nội dung yêu cầu sửa");
                jdbc.update("INSERT INTO thesis_feedback(id,thesis_id,step_key,reviewer_id,approved,comment) VALUES(?,?,?,?,?,?)",
                    UUID.randomUUID().toString(),thesis.getId(),task.getTaskDefinitionKey(),actor.getId(),approved,comment);
            }
        }

        taskService.complete(taskId, vars);

        refreshStatus(thesis);
        thesis = thesisRepository.save(thesis);

        auditLogService.record(task.getProcessInstanceId(), currentUserService.getCurrentUser().orElse(null),
                "COMPLETE_TASK", task.getTaskDefinitionKey(), vars);
        return thesisMapper.toResponse(thesis);
    }

    /** currentStatus = taskDefinitionKey của task đang chờ, hoặc COMPLETED khi process kết thúc. */
    private void refreshStatus(Thesis thesis) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(thesis.getProcessInstanceId())
                .active()
                .list();
        thesis.setCurrentStatus(tasks.isEmpty() ? STATUS_COMPLETED : tasks.get(0).getTaskDefinitionKey());
    }

    private Task findTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return task;
    }
    private List<String> roles(String userId) {
        return jdbc.queryForList("SELECT DISTINCT r.role_code FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=?",String.class,userId);
    }
    private boolean hasRole(String userId,String role) { return roles(userId).contains(role); }
    private boolean hasRoleForRound(String userId,String role,String roundId) {
        Integer count=jdbc.queryForObject("SELECT count(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.role_code=? AND (ur.thesis_round_id IS NULL OR ur.thesis_round_id=?)",Integer.class,userId,role,roundId);
        return count!=null && count>0;
    }
    private boolean canAccessTask(Task task,User actor) {
        if ("ADMIN".equals(actor.getUserType())) return true;
        if (task.getAssignee()!=null) return task.getAssignee().equals(actor.getId());
        String roundId=jdbc.queryForObject("SELECT phase_id FROM theses WHERE process_instance_id=?",String.class,task.getProcessInstanceId());
        return taskService.getIdentityLinksForTask(task.getId()).stream().anyMatch(link ->
                link.getUserId()!=null && link.getUserId().equals(actor.getId())
                || link.getGroupId()!=null && hasRoleForRound(actor.getId(),link.getGroupId(),roundId));
    }
    private OffsetDateTime asOffset(Object value) {
        if (value instanceof OffsetDateTime date) return date;
        return ((java.sql.Timestamp)value).toInstant().atOffset(java.time.ZoneOffset.UTC);
    }
    private void authorizeThesis(Thesis thesis) {
        User actor=currentUserService.getCurrentUser().orElseThrow(() -> new AccessDeniedException("Cần đăng nhập"));
        if ("ADMIN".equals(actor.getUserType()) || thesis.getStudent().getId().equals(actor.getId())
                || thesis.getLecturer().getId().equals(actor.getId()) || hasRoleForRound(actor.getId(),"FACULTY_STAFF",thesis.getPhaseId())) return;
        Integer member=jdbc.queryForObject("SELECT count(*) FROM members WHERE thesis_id=? AND user_id=?",Integer.class,thesis.getId(),actor.getId());
        if (member!=null && member>0) return;
        throw new AccessDeniedException("Không được xem hồ sơ này");
    }
    private void authorizeTask(Task task,User actor) {
        if (!canAccessTask(task,actor)) throw new AccessDeniedException("Không thuộc nhóm được xử lý tác vụ");
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Thesis findThesis(String id) {
        return thesisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thesis not found: " + id));
    }

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse response = new TaskResponse(
                task.getId(),
                task.getName(),
                task.getTaskDefinitionKey(),
                task.getAssignee(),
                task.getProcessInstanceId(),
                task.getCreateTime() == null ? null
                        : task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        List<String> kinds=jdbc.queryForList("SELECT ws.kind FROM workflow_steps ws JOIN workflow_templates wt ON wt.id=ws.template_id WHERE wt.process_definition_id=? AND ws.step_key=?",String.class,task.getProcessDefinitionId(),task.getTaskDefinitionKey());
        if (!kinds.isEmpty()) response.setKind(kinds.getFirst());
        if (task.getDueDate()!=null) response.setDueDate(task.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return response;
    }
}
