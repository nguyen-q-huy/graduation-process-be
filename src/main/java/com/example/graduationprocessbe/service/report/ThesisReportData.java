package com.example.graduationprocessbe.service.report;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the report template can show for one thesis.
 *
 * @param students members of the group, the registering student first
 */
public record ThesisReportData(
        String thesisId,
        String title,
        String titleEn,
        String supervisor,
        List<Student> students,
        String content,
        LocalDate createdDate,
        String status) {

    public record Student(String fullName, String code) {
    }

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Placeholder name -> text. {@code fullName} and {@code employeeCode} describe the main student and exist so a
     * template written with those generic names also works.
     */
    public Map<String, String> toPlaceholders() {
        Student main = students.isEmpty() ? new Student("", "") : students.getFirst();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("title", title);
        values.put("titleEn", titleEn);
        values.put("supervisor", supervisor);
        values.put("studentName", main.fullName());
        values.put("studentCode", main.code());
        values.put("fullName", main.fullName());
        values.put("employeeCode", main.code());
        values.put("studentNames", String.join(", ", students.stream().map(Student::fullName).toList()));
        values.put("studentCodes", String.join(", ", students.stream().map(Student::code).toList()));
        values.put("students", String.join("\n", students.stream().map(s -> s.fullName() + " - " + s.code()).toList()));
        values.put("student1", students.size() > 0 ? label(students.get(0)) : "");
        values.put("student2", students.size() > 1 ? label(students.get(1)) : "");
        values.put("content", content);
        values.put("createdDate", createdDate == null ? "" : DATE.format(createdDate));
        values.put("today", DATE.format(LocalDate.now()));
        values.put("status", status);
        return values;
    }

    private static String label(Student student) {
        return student.fullName() + " - " + student.code();
    }
}
