package com.example.graduationprocessbe.service;

import lombok.RequiredArgsConstructor;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Component("coreTaskDeadlineListener")
@RequiredArgsConstructor
public class CoreTaskDeadlineListener implements TaskListener {
    private final JdbcTemplate jdbc;

    @Override
    public void notify(DelegateTask task) {
        List<Integer> days=jdbc.queryForList("SELECT ws.due_days FROM workflow_steps ws JOIN workflow_templates wt ON wt.id=ws.template_id WHERE wt.process_definition_id=? AND ws.step_key=? AND ws.due_days IS NOT NULL",
                Integer.class,task.getProcessDefinitionId(),task.getTaskDefinitionKey());
        if (!days.isEmpty()) task.setDueDate(Date.from(Instant.now().plus(days.getFirst(), ChronoUnit.DAYS)));
    }
}
