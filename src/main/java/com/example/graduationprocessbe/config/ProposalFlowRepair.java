package com.example.graduationprocessbe.config;

import com.example.graduationprocessbe.service.ThesisProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * On startup, moves proposals that Faculty sent back but that were routed to the first submission step
 * on to the revision step, so they follow the same path as the ones sent back from now on. Safe to run repeatedly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProposalFlowRepair {

    private final ThesisProcessService thesisProcessService;

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        try {
            int moved = thesisProcessService.redirectReturnedProposalsToRevision();
            if (moved > 0) log.info("Moved {} proposal(s) returned by Faculty to the revision step", moved);
            int approved = thesisProcessService.redirectApprovedProposalsToLecturer();
            if (approved > 0) log.info("Moved {} proposal(s) approved by Faculty on to the lecturer's confirmation", approved);
        } catch (RuntimeException ex) {
            // never block startup because of this repair
            log.warn("Could not move returned proposals to the revision step", ex);
        }
    }
}
