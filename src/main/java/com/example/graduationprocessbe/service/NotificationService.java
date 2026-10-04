package com.example.graduationprocessbe.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

/**
 * Bean được gọi từ BPMN qua ${notificationService.sendFeedback(execution, '...')}.
 * Hiện mới log; thay bằng JavaMailSender khi cấu hình SMTP.
 */
@Slf4j
@Service("notificationService")
public class NotificationService {

    public void sendFeedback(DelegateExecution execution, String stage) {
        Object email = execution.getVariable("studentEmail");
        Object feedback = execution.getVariable("feedback");
        log.info("[EMAIL] stage={} to={} feedback={}", stage, email, feedback);
    }
}
