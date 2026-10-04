package com.example.graduationprocessbe.config;

import com.example.graduationprocessbe.entity.Thesis;
import com.example.graduationprocessbe.entity.User;
import com.example.graduationprocessbe.repository.ThesisRepository;
import com.example.graduationprocessbe.repository.UserRepository;
import com.example.graduationprocessbe.service.AuditLogService;
import com.example.graduationprocessbe.service.CoreWorkflowService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/** Optional, repeat-safe local demonstration data; never runs unless app.seed-demo=true. */
@Component
@RequiredArgsConstructor
public class DemoDataSeeder {
    private final JdbcTemplate jdbc;
    private final CoreWorkflowService core;
    private final UserRepository users;
    private final ThesisRepository theses;
    private final RuntimeService runtime;
    private final TaskService tasks;
    private final AuditLogService audit;

    @Transactional
    public void seed() {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM app_seed_versions WHERE version='demo-core-v1')",Boolean.class))) {
            seedWindows(); seedGroupWorkflowVersion(); return;
        }
        OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);
        int start=now.getMonthValue()>=8?now.getYear():now.getYear()-1;
        String yearCode=start+"-"+(start+1);
        jdbc.update("INSERT INTO departments(id,dept_code,dept_name,created_date,last_modified_date) VALUES(?,?,?,now(),now()) ON CONFLICT(dept_code) DO NOTHING",
                UUID.randomUUID().toString(),"KH-KTTT","Khoa Khoa học và Kỹ thuật Thông tin");
        jdbc.update("INSERT INTO academic_years(id,code,start_year,end_year) VALUES(?,?,?,?) ON CONFLICT(code) DO NOTHING",
                UUID.randomUUID().toString(),yearCode,start,start+1);
        String yearId=jdbc.queryForObject("SELECT id FROM academic_years WHERE code=?",String.class,yearCode);
        int semester=now.getMonthValue()>=8 || now.getMonthValue()<=1 ? 1 : 2;
        jdbc.update("INSERT INTO semesters(id,academic_year_id,number) VALUES(?,?,?) ON CONFLICT(academic_year_id,number) DO NOTHING",
                UUID.randomUUID().toString(),yearId,semester);
        String semesterId=jdbc.queryForObject("SELECT id FROM semesters WHERE academic_year_id=? AND number=?",String.class,yearId,semester);
        String roundCode="DEMO-"+yearCode+"-HK"+semester;
        String roundId=UUID.randomUUID().toString();
        String definition=jdbc.queryForObject("SELECT process_definition_id FROM workflow_templates WHERE id='core-template-v1'",String.class);
        if (definition==null) definition=core.publish("core-template-v1");
        jdbc.update("INSERT INTO thesis_rounds(id,code,name,active,semester_id,registration_opens_at,registration_closes_at,workflow_definition_id,created_date,last_modified_date) VALUES(?,?,?,true,?,?,?, ?,now(),now())",
                roundId,roundCode,"Đợt ĐATN mẫu - HK"+semester+" "+yearCode,semesterId,now.minusDays(3),now.plusDays(14),definition);
        String lecturer=users.findByUsername("lecturer").orElseThrow().getId();
        String lecturer2=users.findByUsername("lecturer2").orElseThrow().getId();
        jdbc.update("UPDATE users SET phone='0900000001' WHERE id=? AND phone IS NULL",lecturer);
        jdbc.update("UPDATE users SET phone='0900000002' WHERE id=? AND phone IS NULL",lecturer2);
        jdbc.update("INSERT INTO round_lecturers(id,round_id,lecturer_id,orientation,active) VALUES(?,?,?,?,true)",
                UUID.randomUUID().toString(),roundId,lecturer,"Ứng dụng web, tự động hóa quy trình, cơ sở dữ liệu");
        jdbc.update("INSERT INTO round_lecturers(id,round_id,lecturer_id,orientation,active) VALUES(?,?,?,?,true)",
                UUID.randomUUID().toString(),roundId,lecturer2,"Khoa học dữ liệu, học máy và phân tích dữ liệu");
        String reviewer=users.findByUsername("khoa").orElseThrow().getId();
        sampleCase(roundId,definition,"student",lecturer,"Hệ thống quản lý quy trình ĐATN",
                "Đề cương mẫu: khảo sát, thiết kế và hiện thực hệ thống quản lý ĐATN.",false,reviewer);
        sampleCase(roundId,definition,"student2",lecturer2,"Phân tích dữ liệu học tập",
                "Đề cương mẫu: mô hình phân tích tiến độ học tập và cảnh báo sớm.",true,reviewer);
        // student3 remains unregistered so the full registration screen can be tried manually.
        jdbc.update("INSERT INTO app_seed_versions(version) VALUES('demo-core-v1')");
        seedWindows();
        seedGroupWorkflowVersion();
    }

    private void seedGroupWorkflowVersion() {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM app_seed_versions WHERE version='demo-core-v3-group-workflow')",Boolean.class))) return;
        String roundId=jdbc.queryForObject("SELECT id FROM thesis_rounds WHERE code LIKE 'DEMO-%' ORDER BY created_date DESC LIMIT 1",String.class);
        String draft=core.createDraft("Quy trình ĐATN mẫu cho nhóm tối đa 2 sinh viên","core-template-v1");
        core.publish(draft);
        core.assignTemplate(roundId,draft);
        jdbc.update("INSERT INTO app_seed_versions(version) VALUES('demo-core-v3-group-workflow')");
    }

    private void seedWindows() {
        if (Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM app_seed_versions WHERE version='demo-core-v2-windows')",Boolean.class))) return;
        String roundId=jdbc.queryForObject("SELECT id FROM thesis_rounds WHERE code LIKE 'DEMO-%' ORDER BY created_date DESC LIMIT 1",String.class);
        OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);
        core.setWindow(roundId,"submitProposal",now.minusDays(3),now.plusDays(21));
        core.setWindow(roundId,"midtermReport",now.minusDays(1),now.plusDays(30));
        core.setWindow(roundId,"finalReport",now.plusDays(31),now.plusDays(60));
        core.setWindow(roundId,"finalCorrection",now.plusDays(61),now.plusDays(90));
        jdbc.update("INSERT INTO app_seed_versions(version) VALUES('demo-core-v2-windows')");
    }

    private void sampleCase(String roundId,String definition,String studentName,String lecturerId,
                            String title,String proposal,boolean advance,String reviewerId) {
        User student=users.findByUsername(studentName).orElseThrow();
        User lecturer=users.findById(lecturerId).orElseThrow();
        Thesis thesis=new Thesis(); thesis.setTitle(title); thesis.setDescription(proposal);
        thesis.setStudent(student); thesis.setLecturer(lecturer); thesis.setPhaseId(roundId);
        thesis=theses.saveAndFlush(thesis);
        jdbc.update("INSERT INTO members(thesis_id,user_id,thesis_round_id) VALUES(?,?,?)",
                thesis.getId(),student.getId(),roundId);
        var instance=runtime.startProcessInstanceById(definition,thesis.getId(),Map.of(
                "thesisId",thesis.getId(),"studentId",student.getId(),"studentIds",student.getId(),"lecturerId",lecturerId,"studentEmail",student.getEmail()));
        thesis.setProcessInstanceId(instance.getId());
        thesis.setCurrentStatus("reviewProposal");
        jdbc.update("INSERT INTO thesis_submissions(id,thesis_id,step_key,submitted_by,content) VALUES(?,?,?,?,?)",
                UUID.randomUUID().toString(),thesis.getId(),"submitProposal",student.getId(),proposal);
        audit.record(instance.getId(),student,"START_PROCESS","reviewProposal",Map.of("thesisId",thesis.getId()));
        if (advance) {
            var review=tasks.createTaskQuery().processInstanceId(instance.getId()).singleResult();
            tasks.complete(review.getId(),Map.of("approved",false));
            jdbc.update("INSERT INTO thesis_feedback(id,thesis_id,step_key,reviewer_id,approved,comment) VALUES(?,?,?,?,false,?)",
                    UUID.randomUUID().toString(),thesis.getId(),"reviewProposal",reviewerId,"Bổ sung phạm vi dữ liệu và phương pháp đánh giá.");
            var resubmit=tasks.createTaskQuery().processInstanceId(instance.getId()).singleResult();
            tasks.complete(resubmit.getId());
            jdbc.update("INSERT INTO thesis_submissions(id,thesis_id,step_key,submitted_by,content) VALUES(?,?,?,?,?)",
                    UUID.randomUUID().toString(),thesis.getId(),"submitProposal",student.getId(),proposal+" Đã bổ sung phạm vi và phương pháp đánh giá.");
            review=tasks.createTaskQuery().processInstanceId(instance.getId()).singleResult();
            tasks.complete(review.getId(),Map.of("approved",true));
            jdbc.update("INSERT INTO thesis_feedback(id,thesis_id,step_key,reviewer_id,approved,comment) VALUES(?,?,?,?,true,?)",
                    UUID.randomUUID().toString(),thesis.getId(),"reviewProposal",reviewerId,"Đề cương đạt yêu cầu.");
            thesis.setCurrentStatus("midtermReport");
        }
        theses.save(thesis);
    }
}
