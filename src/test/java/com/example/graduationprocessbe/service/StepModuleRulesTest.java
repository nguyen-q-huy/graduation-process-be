package com.example.graduationprocessbe.service;

import com.example.graduationprocessbe.service.CoreWorkflowService.StepInput;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepModuleRulesTest {
    private final CoreWorkflowService service = new CoreWorkflowService(null, null, null, null);

    @SuppressWarnings("unchecked")
    private List<StepInput> apply(List<StepInput> steps) throws Exception {
        Method m = CoreWorkflowService.class.getDeclaredMethod("applyModules", List.class);
        m.setAccessible(true);
        try { return (List<StepInput>) m.invoke(service, steps); }
        catch (InvocationTargetException e) { throw (Exception) e.getCause(); }
    }

    private static StepInput step(String key, String kind, String role, String reject, String module) {
        return new StepInput(key, key, kind, role, null, null, reject, true, module, null);
    }

    @Test
    void stepsWithoutModulesAreReturnedUntouched() throws Exception {
        var steps = List.of(step("submitProposal", "SUBMIT", "STUDENT", null, null), step("reviewProposal", "REVIEW", "FACULTY_STAFF", "submitProposal", null));
        assertSame(steps, apply(steps));
    }

    @Test
    void feedbackReviewSendsBothOutcomesToTheNextStep() throws Exception {
        var out = apply(List.of(step("a", "SUBMIT", "STUDENT", null, "SUBMIT_FILE"), step("b", "REVIEW", "FACULTY_STAFF", "a", "REVIEW_FEEDBACK"),
                step("c", "SUBMIT", "STUDENT", null, "SUBMIT_FILE"), step("d", "REVIEW", "LECTURER", "c", "REVIEW_APPROVAL")));
        assertEquals("c", out.get(1).rejectKey());
        assertEquals("c", out.get(3).rejectKey());
    }

    @Test
    void phasesMustKeepApprovalBeforeExecution() throws Exception {
        Method m = CoreWorkflowService.class.getDeclaredMethod("validatePhases", List.class);
        m.setAccessible(true);
        var ok = List.of(new StepInput("a", "a", "SUBMIT", "STUDENT", null, null, null, true, null, "APPROVAL"), new StepInput("b", "b", "REVIEW", "LECTURER", null, null, "a", true, null, "EXECUTION"));
        m.invoke(service, ok);
        var bad = List.of(ok.get(1), ok.get(0));
        var e = assertThrows(InvocationTargetException.class, () -> m.invoke(service, bad));
        assertInstanceOf(IllegalArgumentException.class, e.getCause());
    }

    @Test
    void submitModuleNeedsAReviewStepRightAfter() {
        var steps = List.of(step("a", "SUBMIT", "STUDENT", null, "SUBMIT_FILE"), step("b", "TASK", "STUDENT", null, null));
        assertThrows(IllegalArgumentException.class, () -> apply(steps));
    }

    @Test
    void moduleMustMatchStepKind() {
        var steps = List.of(step("a", "TASK", "STUDENT", null, "SUBMIT_FILE"), step("b", "REVIEW", "LECTURER", "a", "REVIEW_APPROVAL"));
        assertThrows(IllegalArgumentException.class, () -> apply(steps));
    }
}
