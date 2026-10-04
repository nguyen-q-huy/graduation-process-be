package com.example.graduationprocessbe.service.impl;

import com.example.graduationprocessbe.dto.request.CreateThesisRequest;
import com.example.graduationprocessbe.dto.response.TaskResponse;
import com.example.graduationprocessbe.dto.response.ThesisResponse;
import com.example.graduationprocessbe.entity.Thesis;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.exception.ResourceNotFoundException;
import com.example.graduationprocessbe.mapper.UserMapper;
import com.example.graduationprocessbe.repository.ThesisRepository;
import com.example.graduationprocessbe.repository.UserRepository;
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
    private final UserMapper userMapper;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

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

        return toResponse(thesisRepository.save(thesis));
    }

    @Override
    @Transactional(readOnly = true)
    public ThesisResponse getThesis(String thesisId) {
        return toResponse(findThesis(thesisId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getThesisTasks(String thesisId) {
        Thesis thesis = findThesis(thesisId);
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
    @Transactional
    public ThesisResponse completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        Thesis thesis = thesisRepository.findByProcessInstanceId(task.getProcessInstanceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Thesis not found for process instance: " + task.getProcessInstanceId()));

        taskService.complete(taskId, variables == null ? Map.of() : variables);

        refreshStatus(thesis);
        return toResponse(thesisRepository.save(thesis));
    }

    /** currentStatus = taskDefinitionKey của task đang chờ, hoặc COMPLETED khi process kết thúc. */
    private void refreshStatus(Thesis thesis) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(thesis.getProcessInstanceId())
                .active()
                .list();
        thesis.setCurrentStatus(tasks.isEmpty() ? STATUS_COMPLETED : tasks.get(0).getTaskDefinitionKey());
    }

    private User findUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Thesis findThesis(String id) {
        return thesisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thesis not found: " + id));
    }

    private ThesisResponse toResponse(Thesis thesis) {
        ThesisResponse response = new ThesisResponse();
        response.setId(thesis.getId());
        response.setTitle(thesis.getTitle());
        response.setDescription(thesis.getDescription());
        response.setStudent(userMapper.toResponse(thesis.getStudent()));
        response.setLecturer(userMapper.toResponse(thesis.getLecturer()));
        response.setPhaseId(thesis.getPhaseId());
        response.setProcessInstanceId(thesis.getProcessInstanceId());
        response.setCurrentStatus(thesis.getCurrentStatus());
        response.setCreatedDate(thesis.getCreatedDate());
        response.setLastModifiedDate(thesis.getLastModifiedDate());
        return response;
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
