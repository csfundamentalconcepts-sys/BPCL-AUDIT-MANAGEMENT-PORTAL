package com.bpcl.audit_portal.controller;

import com.bpcl.audit_portal.dto.IssueDto;
import com.bpcl.audit_portal.service.VaptIssueExtractionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vapt")
public class  PdfController{

    private final VaptIssueExtractionService extractionService;

    public PdfController(VaptIssueExtractionService extractionService){
        this.extractionService = extractionService;
    }

    @GetMapping("/issues")
    public List<IssueDto> extractIssues() {
        return extractionService.extractIssues("C:/Users/satyamprakashsi/Downloads/audit-portal/audit-portal/src/main/java/com/bpcl/audit_portal/controller/VAPT.pdf", "Bpcl#111225");

    }
}