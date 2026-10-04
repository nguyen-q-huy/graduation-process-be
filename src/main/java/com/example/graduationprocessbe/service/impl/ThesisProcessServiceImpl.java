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

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThesisProcessServiceImpl implements ThesisProcessService {

    private static final String PROCESS_KEY = "graduationProcess";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final ThesisRepository thesisRepository;
    private final UserRepository userRepository;
    private final ThesisMapper thesisMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public ThesisResponse createThesis(CreateThesisRequest request) {
        User student = findUser(request.getStudentId());
        User lecturer = findUser(request.getLecturerId());

        Thesis thesis = new Thesis();
        thesis.setTitle(request.getTitle());
        thesis.setDescription(request.getDescription());
        thesis.setStudent(student);
        thesis.setLecturer(lecturer);
        thesis.setPhaseId(request.getPhaseId());
        thesis = thesisRepository.save(thesis);

        Map<String, Object> variables = new HashMap<>();
        variables.put("thesisId", thesis.getId());
        variables.put("studentId", student.getId());
        variables.put("studentEmail", student.getEmail());
        variables.put("lecturerId", lecturer.getId());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_KEY, thesis.getId(), variables);
        thesis.setProcessInstanceId(instance.getId());
        refreshStatus(thesis);
        thesis = thesisRepository.save(thesis);

        auditLogService.record(instance.getId(), currentUserService.getCurrentUser().orElse(null),
                "START_PROCESS", thesis.getCurrentStatus(), Map.of("thesisId", thesis.getId()));
        return thesisMapper.toResponse(thesis);
    }

    @Override
    @Transactional(readOnly = true)
    public ThesisResponse getThesis(String thesisId) {
        return thesisMapper.toResponse(findThesis(thesisId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getThesisTasks(String thesisId) {
        Thesis thesis = findThesis(thesisId);
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
        TaskQuery query = taskService.createTaskQuery().active();
        if (assignee != null) {
            query.taskAssignee(assignee);
        }
        if (candidateGroup != null) {
            query.taskCandidateGroup(candidateGroup);
        }
        return query.orderByTaskCreateTime().asc().list()
                .stream().map(this::toTaskResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(String taskId) {
        return toTaskResponse(findTask(taskId));
    }

    @Override
    @Transactional
    public TaskResponse claimTask(String taskId, String userId) {
        findTask(taskId);
        findUser(userId);
        taskService.claim(taskId, userId);
        return toTaskResponse(findTask(taskId));
    }

    @Override
    @Transactional
    public ThesisResponse completeTask(String taskId, Map<String, Object> variables) {
        Task task = findTask(taskId);
        Thesis thesis = thesisRepository.findByProcessInstanceId(task.getProcessInstanceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thesis not found for process instance: " + task.getProcessInstanceId()));
        Map<String, Object> vars = variables == null ? Map.of() : variables;

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

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Thesis findThesis(String id) {
        return thesisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thesis not found: " + id));
    }

    private TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getTaskDefinitionKey(),
                task.getAssignee(),
                task.getProcessInstanceId(),
                task.getCreateTime() == null ? null
                        : task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }
}
