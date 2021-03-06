package org.openmrs.module.patientdashboardapp.page.controller;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.hospitalcore.PatientQueueService;
import org.openmrs.module.hospitalcore.model.OpdPatientQueue;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.util.ConceptAnswerComparator;
import org.openmrs.module.hospitalcore.util.PatientUtils;
import org.openmrs.module.referenceapplication.ReferenceApplicationWebConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainPageController {

    public void get(UiSessionContext sessionContext,
                    PageModel model,
                    PageRequest pageRequest,
                    UiUtils ui,
                    @RequestParam("patientId") Integer patientId,
                    @RequestParam("opdId") Integer opdId,
                    @RequestParam(value = "queueId", required = false) Integer queueId,
                    @RequestParam(value = "opdLogId", required = false) Integer opdLogId,
                    @RequestParam(value = "visitStatus", required = false) String visitStatus) {
        pageRequest.getSession().setAttribute(ReferenceApplicationWebConstants.SESSION_ATTRIBUTE_REDIRECT_URL,ui.thisUrl());
        sessionContext.requireAuthentication();
        Patient patient = Context.getPatientService().getPatient(patientId);
        HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
        Map<String, String> attributes = PatientUtils.getAttributes(patient);
        Concept category = Context.getConceptService().getConceptByName("Patient Category");
        List<ConceptAnswer> categoryList = (category != null ? new ArrayList<ConceptAnswer>(category.getAnswers()) : null);
        if (CollectionUtils.isNotEmpty(categoryList)) {
            Collections.sort(categoryList, new ConceptAnswerComparator());
        }

        model.addAttribute("patientId", patientId);
        model.addAttribute("opdId", opdId);
        model.addAttribute("queueId", queueId);
        model.addAttribute("opdLogId", opdLogId);
        model.addAttribute("patientIdentifier",patient.getPatientIdentifier());
        model.addAttribute("category",patient.getAttribute(14));
        model.addAttribute("address",patient.getPersonAddress());
        model.addAttribute("visitStatus",visitStatus);
        model.addAttribute("previousVisit",hcs.getLastVisitTime(patient));

        if (queueId != null) {
            OpdPatientQueue opdPatientQueue = Context.getService(PatientQueueService.class).getOpdPatientQueueById(queueId);
            if (opdPatientQueue != null) {
                opdPatientQueue.setStatus("Dr. "+Context.getAuthenticatedUser().getGivenName());
                Context.getService(PatientQueueService.class).saveOpdPatientQueue(opdPatientQueue);
                model.addAttribute("patientStatus", opdPatientQueue.getVisitStatus());
            }
            else{
                model.addAttribute("patientStatus", "Unknown");
            }
        }
        else {
            model.addAttribute("patientStatus", "Unknown");
        }
    }
}