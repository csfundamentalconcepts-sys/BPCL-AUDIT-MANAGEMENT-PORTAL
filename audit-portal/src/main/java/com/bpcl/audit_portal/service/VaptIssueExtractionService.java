////package com.bpcl.audit_portal.service;
////
////import com.bpcl.audit_portal.dto.IssueDto;
////import org.apache.pdfbox.Loader;
////import org.apache.pdfbox.pdmodel.PDDocument;
////import org.apache.pdfbox.text.PDFTextStripper;
////import org.springframework.stereotype.Service;
////
////import java.io.File;
////import java.io.IOException;
////import java.util.ArrayList;
////import java.util.List;
////import java.util.regex.Matcher;
////import java.util.regex.Pattern;
////
////@Service
////public class VaptIssueExtractionService {
////
////    private static final Pattern EXECUTIVE_SUMMARY_TOC_PATTERN =
////            Pattern.compile("Executive\\s+Summary\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
////    private static final Pattern DETAILED_OBSERVATION_TOC_PATTERN =
////            Pattern.compile("Detailed\\s+Observations\\s*\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
////    private static final Pattern ROW_START_PATTERN = Pattern.compile("^(\\d+)\\s+https?://.*");
////    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");
////    private static final Pattern CWE_CVE_PATTERN = Pattern.compile("(CWE\\s*:?\\s*[\\d,\\s]+|CVE\\s*:?\\s*[\\w\\-.,\\s]+)", Pattern.CASE_INSENSITIVE);
////    private static final Pattern SEVERITY_PATTERN = Pattern.compile("\\b(Critical|High|Medium|Low|Informational)\\b", Pattern.CASE_INSENSITIVE);
////    private static final Pattern OBSERVATION_TYPE_PATTERN = Pattern.compile("\\b(New|Repeat)\\b", Pattern.CASE_INSENSITIVE);
////    private static final Pattern STATUS_PATTERN = Pattern.compile("\\b(Open|Closed)\\b", Pattern.CASE_INSENSITIVE);
////    private static final Pattern REPORT_SECTION_PATTERN = Pattern.compile("(Ref\\s*[^\\n]*?)(?=\\b(New|Repeat|Open|Closed|Based\\s+on)\\b)", Pattern.CASE_INSENSITIVE);
////
////    public List<IssueDto> extractIssues(String pdfPath, String password) {
////        File file = new File(pdfPath);
////        if (!file.exists()) {
////            throw new IllegalArgumentException("PDF file not found: " + pdfPath);
////        }
////
////        try (PDDocument document = Loader.loadPDF(file, password)) {
////            PDFTextStripper stripper = new PDFTextStripper();
////            stripper.setSortByPosition(true);
////
////            int[] pageRange = resolveExecutiveSummaryRange(document, stripper);
////            String executiveText = extractPageTextRange(document, stripper, pageRange[0], pageRange[1]);
////            List<String> cleanedLines = normalizeLines(executiveText);
////            List<String> rowBlocks = splitRowBlocks(cleanedLines);
////
////            List<IssueDto> issues = new ArrayList<>();
////            for (String block : rowBlocks) {
////                issues.add(parseRow(block));
////            }
////            return issues;
////        } catch (IOException ex) {
////            throw new IllegalStateException("Unable to read PDF: " + ex.getMessage(), ex);
////        }
////    }
////
////    private int[] resolveExecutiveSummaryRange(PDDocument document, PDFTextStripper stripper) throws IOException {
////        String tocText = extractPageTextRange(document, stripper, 1, Math.min(8, document.getNumberOfPages()));
////        Matcher execMatcher = EXECUTIVE_SUMMARY_TOC_PATTERN.matcher(tocText);
////        Matcher detailMatcher = DETAILED_OBSERVATION_TOC_PATTERN.matcher(tocText);
////
////        int start = 1;
////        int end = document.getNumberOfPages();
////        if (execMatcher.find()) {
////            start = Integer.parseInt(execMatcher.group(1));
////        }
////        if (detailMatcher.find()) {
////            end = Integer.parseInt(detailMatcher.group(1)) - 1;
////        }
////        if (start < 1 || start > document.getNumberOfPages()) {
////            start = 1;
////        }
////        if (end < start || end > document.getNumberOfPages()) {
////            end = document.getNumberOfPages();
////        }
////        return new int[]{start, end};
////    }
////
////    private String extractPageTextRange(PDDocument document, PDFTextStripper stripper, int startPage, int endPage) throws IOException {
////        stripper.setStartPage(startPage);
////        stripper.setEndPage(endPage);
////        return stripper.getText(document);
////    }
////
////    private List<String> normalizeLines(String text) {
////        String[] lines = text.replace('\r', '\n').split("\n");
////        List<String> out = new ArrayList<>();
////        for (String rawLine : lines) {
////            String line = rawLine == null ? "" : rawLine.trim();
////            if (line.isBlank()) {
////                continue;
////            }
////            if (line.equalsIgnoreCase("<Confidential>")) {
////                continue;
////            }
////            if (line.matches("Page\\s+\\d+\\s+of\\s+\\d+")) {
////                continue;
////            }
////            if (line.toLowerCase().contains("cert-in audit report format")) {
////                continue;
////            }
////            if (line.equalsIgnoreCase("Executive Summary")) {
////                continue;
////            }
////            if (line.startsWith("S.") || line.startsWith("No") || line.startsWith("Affected Asset")
////                    || line.startsWith("Observation/") || line.startsWith("CVE/CWE")
////                    || line.startsWith("Severity") || line.startsWith("Recommendation")
////                    || line.startsWith("New") || line.startsWith("Status")
////                    || line.startsWith("L2 Assessment")) {
////                continue;
////            }
////            if (line.equalsIgnoreCase("References:") || line.equalsIgnoreCase("Detailed Observations")) {
////                break;
////            }
////            out.add(line);
////        }
////        return out;
////    }
////
////    private List<String> splitRowBlocks(List<String> lines) {
////        List<String> blocks = new ArrayList<>();
////        StringBuilder current = null;
////        for (String line : lines) {
////            if (ROW_START_PATTERN.matcher(line).matches()) {
////                if (current != null && !current.isEmpty()) {
////                    blocks.add(current.toString().trim());
////                }
////                current = new StringBuilder(line);
////            } else if (current != null) {
////                current.append('\n').append(line);
////            }
////        }
////        if (current != null && !current.isEmpty()) {
////            blocks.add(current.toString().trim());
////        }
////        return blocks;
////    }
////
////    private IssueDto parseRow(String block) {
////        Matcher m = Pattern.compile("^(\\d+)\\b").matcher(block);
////        Integer serialNo = m.find() ? Integer.parseInt(m.group(1)) : null;
////        String affectedAsset = extractFirstMatch(URL_PATTERN, block, 1);
////        String cveCwe = extractFirstMatch(CWE_CVE_PATTERN, block, 1);
////        String severity = extractFirstMatch(SEVERITY_PATTERN, block, 1);
////        String recommendationReference = extractRecommendation(block);
////        String reportSection = extractFirstMatch(REPORT_SECTION_PATTERN, block, 1);
////        String observationType = extractFirstMatch(OBSERVATION_TYPE_PATTERN, block, 1);
////        String status = extractFirstMatch(STATUS_PATTERN, block, 1);
////        String assessmentRemarks = extractRemarks(block, status);
////        String observationTitle = extractObservationTitle(block, affectedAsset, cveCwe);
////
////        return new IssueDto(
////                serialNo,
////                safe(affectedAsset),
////                safe(observationTitle),
////                safe(cveCwe),
////                safe(severity),
////                safe(recommendationReference),
////                safe(reportSection),
////                safe(observationType),
////                safe(status),
////                safe(assessmentRemarks)
////        );
////    }
////
////    private String extractRecommendation(String block) {
////        Matcher m = Pattern.compile("(Refer\\s+to\\s+the\\s+“?\"?Detailed\\s+Observations\"? section\\.?[^\\n]*)", Pattern.CASE_INSENSITIVE).matcher(block);
////        if (m.find()) {
////            return compact(m.group(1));
////        }
////        return "";
////    }
////
////    private String extractObservationTitle(String block, String url, String cveCwe) {
////        String work = block.replaceFirst("^\\d+\\s*", "");
////        if (url != null && !url.isBlank()) {
////            work = work.replace(url, "").trim();
////        }
////        if (cveCwe != null && !cveCwe.isBlank()) {
////            int idx = work.toLowerCase().indexOf(cveCwe.toLowerCase());
////            if (idx > 0) {
////                work = work.substring(0, idx).trim();
////            }
////        }
////        work = work.replaceAll("\\b(Critical|High|Medium|Low|Informational|New|Repeat|Open|Closed)\\b", "").trim();
////        return compact(work);
////    }
////
////    private String extractRemarks(String block, String status) {
////        if (status == null || status.isBlank()) {
////            return "";
////        }
////        int idx = block.toLowerCase().indexOf(status.toLowerCase());
////        if (idx < 0 || idx + status.length() >= block.length()) {
////            return "";
////        }
////        return compact(block.substring(idx + status.length()));
////    }
////
////    private String extractFirstMatch(Pattern pattern, String text, int group) {
////        Matcher matcher = pattern.matcher(text);
////        if (matcher.find()) {
////            return compact(matcher.group(group));
////        }
////        return "";
////    }
////
////    private String compact(String value) {
////        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
////    }
////
////    private String safe(String value) {
////        return value == null ? "" : value;
////    }
////}
//
//package com.bpcl.audit_portal.service;
//
//import com.bpcl.audit_portal.dto.IssueDto;
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class VaptIssueExtractionService {
//
//    private static final Pattern EXECUTIVE_SUMMARY_TOC_PATTERN =
//            Pattern.compile("Executive\\s+Summary\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
//    private static final Pattern DETAILED_OBSERVATION_TOC_PATTERN =
//            Pattern.compile("Detailed\\s+Observations\\s*\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
//    private static final Pattern ROW_START_PATTERN = Pattern.compile("^(\\d+)\\s+https?://.*");
//    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");
//    private static final Pattern CWE_CVE_PATTERN = Pattern.compile("(CWE\\s*:?\\s*[\\d,\\s]+|CVE\\s*:?\\s*[\\w\\-.,\\s]+)", Pattern.CASE_INSENSITIVE);
//    private static final Pattern SEVERITY_PATTERN = Pattern.compile("\\b(Critical|High|Medium|Low|Informational)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern OBSERVATION_TYPE_PATTERN = Pattern.compile("\\b(New|Repeat)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern STATUS_PATTERN = Pattern.compile("\\b(Open|Closed)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern REPORT_SECTION_PATTERN = Pattern.compile("(Ref\\s*[^\\n]*?)(?=\\b(New|Repeat|Open|Closed|Based\\s+on)\\b)", Pattern.CASE_INSENSITIVE);
//
//    public List<IssueDto> extractIssues(String pdfPath, String password) {
//        File file = new File(pdfPath);
//        if (!file.exists()) {
//            throw new IllegalArgumentException("PDF file not found: " + pdfPath);
//        }
//
//        try (PDDocument document = Loader.loadPDF(file, password)) {
//            PDFTextStripper stripper = new PDFTextStripper();
//            stripper.setSortByPosition(true);
//
//            int[] pageRange = resolveExecutiveSummaryRange(document, stripper);
//            String executiveText = extractPageTextRange(document, stripper, pageRange[0], pageRange[1]);
//            List<String> cleanedLines = normalizeLines(executiveText);
//            List<String> rowBlocks = splitRowBlocks(cleanedLines);
//
//            List<IssueDto> issues = new ArrayList<>();
//            for (String block : rowBlocks) {
//                issues.add(normalizeIssue(parseRow(block)));
//            }
//            return issues;
//        } catch (IOException ex) {
//            throw new IllegalStateException("Unable to read PDF: " + ex.getMessage(), ex);
//        }
//    }
//
//    private int[] resolveExecutiveSummaryRange(PDDocument document, PDFTextStripper stripper) throws IOException {
//        String tocText = extractPageTextRange(document, stripper, 1, Math.min(8, document.getNumberOfPages()));
//        Matcher execMatcher = EXECUTIVE_SUMMARY_TOC_PATTERN.matcher(tocText);
//        Matcher detailMatcher = DETAILED_OBSERVATION_TOC_PATTERN.matcher(tocText);
//
//        int start = 1;
//        int end = document.getNumberOfPages();
//        if (execMatcher.find()) {
//            start = Integer.parseInt(execMatcher.group(1));
//        }
//        if (detailMatcher.find()) {
//            end = Integer.parseInt(detailMatcher.group(1)) - 1;
//        }
//        if (start < 1 || start > document.getNumberOfPages()) {
//            start = 1;
//        }
//        if (end < start || end > document.getNumberOfPages()) {
//            end = document.getNumberOfPages();
//        }
//        return new int[]{start, end};
//    }
//
//    private String extractPageTextRange(PDDocument document, PDFTextStripper stripper, int startPage, int endPage) throws IOException {
//        stripper.setStartPage(startPage);
//        stripper.setEndPage(endPage);
//        return stripper.getText(document);
//    }
//
//    private List<String> normalizeLines(String text) {
//        String[] lines = text.replace('\r', '\n').split("\n");
//        List<String> out = new ArrayList<>();
//        for (String rawLine : lines) {
//            String line = rawLine == null ? "" : rawLine.trim();
//            if (line.isBlank()) {
//                continue;
//            }
//            if (line.equalsIgnoreCase("<Confidential>")) {
//                continue;
//            }
//            if (line.matches("Page\\s+\\d+\\s+of\\s+\\d+")) {
//                continue;
//            }
//            if (line.toLowerCase().contains("cert-in audit report format")) {
//                continue;
//            }
//            if (line.equalsIgnoreCase("Executive Summary")) {
//                continue;
//            }
//            if (line.startsWith("S.") || line.startsWith("No") || line.startsWith("Affected Asset")
//                    || line.startsWith("Observation/") || line.startsWith("CVE/CWE")
//                    || line.startsWith("Severity") || line.startsWith("Recommendation")
//                    || line.startsWith("New") || line.startsWith("Status")
//                    || line.startsWith("L2 Assessment")) {
//                continue;
//            }
//            if (line.equalsIgnoreCase("References:") || line.equalsIgnoreCase("Detailed Observations")) {
//                break;
//            }
//            out.add(line);
//        }
//        return out;
//    }
//
//    private List<String> splitRowBlocks(List<String> lines) {
//        List<String> blocks = new ArrayList<>();
//        StringBuilder current = null;
//        for (String line : lines) {
//            if (ROW_START_PATTERN.matcher(line).matches()) {
//                if (current != null && !current.isEmpty()) {
//                    blocks.add(current.toString().trim());
//                }
//                current = new StringBuilder(line);
//            } else if (current != null) {
//                current.append('\n').append(line);
//            }
//        }
//        if (current != null && !current.isEmpty()) {
//            blocks.add(current.toString().trim());
//        }
//        return blocks;
//    }
//
//    private IssueDto parseRow(String block) {
//        Matcher m = Pattern.compile("^(\\d+)\\b").matcher(block);
//        Integer serialNo = m.find() ? Integer.parseInt(m.group(1)) : null;
//        String affectedAsset = extractFirstMatch(URL_PATTERN, block, 1);
//        String cveCwe = extractFirstMatch(CWE_CVE_PATTERN, block, 1);
//        String severity = extractFirstMatch(SEVERITY_PATTERN, block, 1);
//        String recommendationReference = extractRecommendation(block);
//        String reportSection = extractFirstMatch(REPORT_SECTION_PATTERN, block, 1);
//        String observationType = extractFirstMatch(OBSERVATION_TYPE_PATTERN, block, 1);
//        String status = extractFirstMatch(STATUS_PATTERN, block, 1);
//        String assessmentRemarks = extractRemarks(block, status);
//        String observationTitle = extractObservationTitle(block, affectedAsset, cveCwe);
//
//        return new IssueDto(
//                serialNo,
//                safe(affectedAsset),
//                safe(observationTitle),
//                safe(cveCwe),
//                safe(severity),
//                safe(recommendationReference),
//                safe(reportSection),
//                safe(observationType),
//                safe(status),
//                safe(assessmentRemarks)
//        );
//    }
//
//    private String extractRecommendation(String block) {
//        Matcher m = Pattern.compile("(Refer\\s+to\\s+the\\s+“?\"?Detailed\\s+Observations\"? section\\.?[^\\n]*)", Pattern.CASE_INSENSITIVE).matcher(block);
//        if (m.find()) {
//            return compact(m.group(1));
//        }
//        return "";
//    }
//
//    private String extractObservationTitle(String block, String url, String cveCwe) {
//        String work = block.replaceFirst("^\\d+\\s*", "");
//        if (url != null && !url.isBlank()) {
//            work = work.replace(url, "").trim();
//        }
//        if (cveCwe != null && !cveCwe.isBlank()) {
//            int idx = work.toLowerCase().indexOf(cveCwe.toLowerCase());
//            if (idx > 0) {
//                work = work.substring(0, idx).trim();
//            }
//        }
//        work = work.replaceAll("\\b(Critical|High|Medium|Low|Informational|New|Repeat|Open|Closed)\\b", "").trim();
//        return compact(work);
//    }
//
//    private String extractRemarks(String block, String status) {
//        if (status == null || status.isBlank()) {
//            return "";
//        }
//        int idx = block.toLowerCase().indexOf(status.toLowerCase());
//        if (idx < 0 || idx + status.length() >= block.length()) {
//            return "";
//        }
//        return compact(block.substring(idx + status.length()));
//    }
//
//    private String extractFirstMatch(Pattern pattern, String text, int group) {
//        Matcher matcher = pattern.matcher(text);
//        if (matcher.find()) {
//            return compact(matcher.group(group));
//        }
//        return "";
//    }
//
//    private String compact(String value) {
//        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
//    }
//
//    private String safe(String value) {
//        return value == null ? "" : value;
//    }
//
//    private IssueDto normalizeIssue(IssueDto in) {
//        String affectedAsset = safe(in.affectedAsset());
//        String remarks = safe(in.assessmentRemarks());
//        String cve = safe(in.cveCwe());
//        String severity = safe(in.severity());
//        String reportSection = normalizeReportSection(safe(in.reportSection()));
//        String recommendation = safe(in.recommendationReference());
//
//// Fix split URL suffix like ".ne" + "t/" leaking into remarks.
//        if (affectedAsset.endsWith(".ne")) {
//            Matcher m = Pattern.compile("^\\s*(t(?:/[^\\s]*)?)\\b").matcher(remarks);
//            if (m.find()) {
//                affectedAsset = affectedAsset + m.group(1);
//                remarks = compact(remarks.substring(m.end()));
//            }
//        }
//
//// Recover split CWE values (e.g., "CWE:" + "79,1021,319" in remarks).
//        if ("CWE:".equalsIgnoreCase(cve) || "CWE".equalsIgnoreCase(cve)) {
//            Matcher m = Pattern.compile("^\\s*([0-9,]+)\\b").matcher(remarks);
//            if (m.find()) {
//                cve = "CWE:" + m.group(1);
//                remarks = compact(remarks.substring(m.end()));
//            }
//        }
//
//// Normalize severity casing.
//        if (severity.equalsIgnoreCase("informational") || severity.equalsIgnoreCase("information")) {
//            severity = "Informational";
//        }
//
//// Standardize recommendation if it got blank due to line breaks.
//        if (recommendation.isBlank() && !reportSection.isBlank()) {
//            recommendation = "Refer to the Detailed Observations section.";
//        }
//
//        return new IssueDto(
//                in.serialNo(),
//                affectedAsset,
//                safe(in.observationTitle()),
//                cve,
//                severity,
//                recommendation,
//                reportSection,
//                safe(in.observationType()),
//                safe(in.status()),
//                remarks
//        );
//    }
//
//    private String normalizeReportSection(String raw) {
//        if (raw.isBlank()) {
//            return "";
//        }
//        Matcher m = Pattern.compile("(Ref\\s*[0-9,\\s]+)", Pattern.CASE_INSENSITIVE).matcher(raw);
//        if (m.find()) {
//            return compact(m.group(1));
//        }
//        return compact(raw);
//    }
//}
package com.bpcl.audit_portal.service;

import com.bpcl.audit_portal.dto.IssueDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VaptIssueExtractionService {

    private static final Pattern EXECUTIVE_SUMMARY_TOC_PATTERN =
            Pattern.compile("Executive\\s+Summary\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DETAILED_OBSERVATION_TOC_PATTERN =
            Pattern.compile("Detailed\\s+Observations\\s*\\.{2,}\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROW_START_PATTERN = Pattern.compile("^(\\d+)\\s+https?://.*");
    private static final Pattern URL_PATTERN = Pattern.compile("(https?://\\S+)");
    private static final Pattern CWE_CVE_PATTERN = Pattern.compile("(CWE\\s*:?\\s*[\\d,\\s]+|CVE\\s*:?\\s*[\\w\\-.,\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SEVERITY_PATTERN = Pattern.compile("\\b(Critical|High|Medium|Low|Informational)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern OBSERVATION_TYPE_PATTERN = Pattern.compile("\\b(New|Repeat)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATUS_PATTERN = Pattern.compile("\\b(Open|Closed)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern REPORT_SECTION_PATTERN = Pattern.compile("(Ref\\s*[^\\n]*?)(?=\\b(New|Repeat|Open|Closed|Based\\s+on)\\b)", Pattern.CASE_INSENSITIVE);

    public List<IssueDto> extractIssues(String pdfPath, String password) {
        File file = new File(pdfPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("PDF file not found: " + pdfPath);
        }

        try (PDDocument document = Loader.loadPDF(file, password)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            int[] pageRange = resolveExecutiveSummaryRange(document, stripper);
            List<IssueDto> tableIssues = extractByTables(document, pageRange[0], pageRange[1]);
            if (!tableIssues.isEmpty()) {
                return tableIssues;
            }

// Fallback parser for non-table-friendly PDFs.
            String executiveText = extractPageTextRange(document, stripper, pageRange[0], pageRange[1]);
            List<String> cleanedLines = normalizeLines(executiveText);
            List<String> rowBlocks = splitRowBlocks(cleanedLines);
            List<IssueDto> fallbackIssues = new ArrayList<>();
            for (String block : rowBlocks) {
                fallbackIssues.add(normalizeIssue(parseRow(block)));
            }
            return fallbackIssues;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read PDF: " + ex.getMessage(), ex);
        }
    }
    private String normalizeSeverity(String severity) {
        String s = safe(severity).trim();
        if (s.equalsIgnoreCase("informational")
                || s.equalsIgnoreCase("information")
                || s.equalsIgnoreCase("informatio nal")
                || s.equalsIgnoreCase("informatio")) {
            return "Informational";
        }
        if (s.equalsIgnoreCase("critical")) return "Critical";
        if (s.equalsIgnoreCase("high")) return "High";
        if (s.equalsIgnoreCase("medium")) return "Medium";
        if (s.equalsIgnoreCase("low")) return "Low";
        return s;
    }

    private List<IssueDto> extractByTables(PDDocument document, int startPage, int endPage) throws IOException {
        List<RowBuilder> rows = new ArrayList<>();
        SpreadsheetExtractionAlgorithm spreadsheet = new SpreadsheetExtractionAlgorithm();
        BasicExtractionAlgorithm basic = new BasicExtractionAlgorithm();

        try (ObjectExtractor extractor = new ObjectExtractor(document)) {
            for (int pageNum = startPage; pageNum <= endPage; pageNum++) {
                Page page = extractor.extract(pageNum);
                List<Table> tables = spreadsheet.extract(page);
                if (tables.isEmpty()) {
                    tables = basic.extract(page);
                }

                for (Table table : tables) {
                    for (List<RectangularTextContainer> rawRow : table.getRows()) {
                        List<String> cells = new ArrayList<>();
                        for (RectangularTextContainer cell : rawRow) {
                            cells.add(compact(cell.getText()));
                        }
                        consumeTableRow(rows, cells);
                    }
                }
            }
        }

        List<IssueDto> out = new ArrayList<>();
        for (RowBuilder row : rows) {
            if (row.serialNo == null) {
                continue;
            }
            IssueDto issue = new IssueDto(
                    row.serialNo,
                    compact(row.affectedAsset),
                    compact(row.observationTitle),
                    compact(row.cveCwe),
                    normalizeSeverity(compact(row.severity)),
                    compact(row.recommendationReference),
                    normalizeReportSection(compact(row.reportSection)),
                    compact(row.observationType),
                    compact(row.status),
                    compact(row.assessmentRemarks)
            );
            out.add(normalizeIssue(issue));
        }
        return out;
    }

    private void consumeTableRow(List<RowBuilder> rows, List<String> cells) {
        if (cells.isEmpty()) {
            return;
        }
        String first = safe(getCell(cells, 0));
        if (first.equalsIgnoreCase("S.") || first.equalsIgnoreCase("No")) {
            return;
        }

        boolean startsNew = first.matches("\\d+");
        RowBuilder current;
        if (startsNew) {
            current = new RowBuilder();
            current.serialNo = Integer.parseInt(first);
            rows.add(current);
        } else if (!rows.isEmpty()) {
            current = rows.get(rows.size() - 1);
        } else {
            return;
        }

// Expected columns:
// 0 SNo, 1 Asset, 2 Observation, 3 CVE/CWE, 4 Severity,
// 5 Recommendation Reference, 6 New/Repeat, 7 Status, 8 L2 Remarks
        append(current::appendAsset, getCell(cells, 1));
        append(current::appendObservationTitle, getCell(cells, 2));
        append(current::appendCveCwe, getCell(cells, 3));
        append(current::appendSeverity, getCell(cells, 4));
        append(current::appendRecommendationReference, getCell(cells, 5));
        append(current::appendObservationType, getCell(cells, 6));
        append(current::appendStatus, getCell(cells, 7));
        append(current::appendAssessmentRemarks, getCell(cells, 8));

// Sometimes Ref values continue in next cells.
        for (int i = 9; i < cells.size(); i++) {
            String extra = getCell(cells, i);
            if (extra.toLowerCase().startsWith("ref")) {
                append(current::appendReportSection, extra);
            } else if (!extra.isBlank()) {
                append(current::appendAssessmentRemarks, extra);
            }
        }

        String reco = compact(current.recommendationReference);
        String detectedRef = normalizeReportSection(extractFirstMatch(REPORT_SECTION_PATTERN, reco + " " + current.reportSection, 1));
        if (!detectedRef.isBlank()) {
            current.reportSection = detectedRef;
        }
    }

    private String getCell(List<String> cells, int idx) {
        return idx < cells.size() ? safe(cells.get(idx)) : "";
    }

    private void append(java.util.function.Consumer<String> consumer, String value) {
        String v = compact(value);
        if (!v.isBlank()) {
            consumer.accept(v);
        }
    }

    private int[] resolveExecutiveSummaryRange(PDDocument document, PDFTextStripper stripper) throws IOException {
        String tocText = extractPageTextRange(document, stripper, 1, Math.min(8, document.getNumberOfPages()));
        Matcher execMatcher = EXECUTIVE_SUMMARY_TOC_PATTERN.matcher(tocText);
        Matcher detailMatcher = DETAILED_OBSERVATION_TOC_PATTERN.matcher(tocText);

        int start = 1;
        int end = document.getNumberOfPages();
        if (execMatcher.find()) {
            start = Integer.parseInt(execMatcher.group(1));
        }
        if (detailMatcher.find()) {
            end = Integer.parseInt(detailMatcher.group(1)) - 1;
        }
        if (start < 1 || start > document.getNumberOfPages()) {
            start = 1;
        }
        if (end < start || end > document.getNumberOfPages()) {
            end = document.getNumberOfPages();
        }
        return new int[]{start, end};
    }

    private String extractPageTextRange(PDDocument document, PDFTextStripper stripper, int startPage, int endPage) throws IOException {
        stripper.setStartPage(startPage);
        stripper.setEndPage(endPage);
        return stripper.getText(document);
    }

    private List<String> normalizeLines(String text) {
        String[] lines = text.replace('\r', '\n').split("\n");
        List<String> out = new ArrayList<>();
        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.isBlank()) {
                continue;
            }
            if (line.equalsIgnoreCase("<Confidential>")) {
                continue;
            }
            if (line.matches("Page\\s+\\d+\\s+of\\s+\\d+")) {
                continue;
            }
            if (line.toLowerCase().contains("cert-in audit report format")) {
                continue;
            }
            if (line.equalsIgnoreCase("Executive Summary")) {
                continue;
            }
            if (line.startsWith("S.") || line.startsWith("No") || line.startsWith("Affected Asset")
                    || line.startsWith("Observation/") || line.startsWith("CVE/CWE")
                    || line.startsWith("Severity") || line.startsWith("Recommendation")
                    || line.startsWith("New") || line.startsWith("Status")
                    || line.startsWith("L2 Assessment")) {
                continue;
            }
            if (line.equalsIgnoreCase("References:") || line.equalsIgnoreCase("Detailed Observations")) {
                break;
            }
            out.add(line);
        }
        return out;
    }

    private List<String> splitRowBlocks(List<String> lines) {
        List<String> blocks = new ArrayList<>();
        StringBuilder current = null;
        for (String line : lines) {
            if (ROW_START_PATTERN.matcher(line).matches()) {
                if (current != null && !current.isEmpty()) {
                    blocks.add(current.toString().trim());
                }
                current = new StringBuilder(line);
            } else if (current != null) {
                current.append('\n').append(line);
            }
        }
        if (current != null && !current.isEmpty()) {
            blocks.add(current.toString().trim());
        }
        return blocks;
    }

    private IssueDto parseRow(String block) {
        Matcher m = Pattern.compile("^(\\d+)\\b").matcher(block);
        Integer serialNo = m.find() ? Integer.parseInt(m.group(1)) : null;
        String affectedAsset = extractFirstMatch(URL_PATTERN, block, 1);
        String cveCwe = extractFirstMatch(CWE_CVE_PATTERN, block, 1);
        String severity = extractFirstMatch(SEVERITY_PATTERN, block, 1);
        String recommendationReference = extractRecommendation(block);
        String reportSection = extractFirstMatch(REPORT_SECTION_PATTERN, block, 1);
        String observationType = extractFirstMatch(OBSERVATION_TYPE_PATTERN, block, 1);
        String status = extractFirstMatch(STATUS_PATTERN, block, 1);
        String assessmentRemarks = extractRemarks(block, status);
        String observationTitle = extractObservationTitle(block, affectedAsset, cveCwe);

        return new IssueDto(
                serialNo,
                safe(affectedAsset),
                safe(observationTitle),
                safe(cveCwe),
                safe(severity),
                safe(recommendationReference),
                safe(reportSection),
                safe(observationType),
                safe(status),
                safe(assessmentRemarks)
        );
    }
private String extractRecommendation(String block) {
    Matcher m = Pattern.compile("(Refer\\s+to\\s+the\\s+“?\"?Detailed\\s+Observations\"? section\\.?[^\\n]*)", Pattern.CASE_INSENSITIVE).matcher(block);
    if (m.find()) {
        return compact(m.group(1));
    }
    return "";
}

private String extractObservationTitle(String block, String url, String cveCwe) {
    String work = block.replaceFirst("^\\d+\\s*", "");
    if (url != null && !url.isBlank()) {
        work = work.replace(url, "").trim();
    }
    if (cveCwe != null && !cveCwe.isBlank()) {
        int idx = work.toLowerCase().indexOf(cveCwe.toLowerCase());
        if (idx > 0) {
            work = work.substring(0, idx).trim();
        }
    }
    work = work.replaceAll("\\b(Critical|High|Medium|Low|Informational|New|Repeat|Open|Closed)\\b", "").trim();
    return compact(work);
}

private String extractRemarks(String block, String status) {
    if (status == null || status.isBlank()) {
        return "";
    }
    int idx = block.toLowerCase().indexOf(status.toLowerCase());
    if (idx < 0 || idx + status.length() >= block.length()) {
        return "";
    }
    return compact(block.substring(idx + status.length()));
}

private String extractFirstMatch(Pattern pattern, String text, int group) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
        return compact(matcher.group(group));
    }
    return "";
}

private String compact(String value) {
    return value == null ? "" : value.replaceAll("\\s+", " ").trim();
}

private String safe(String value) {
    return value == null ? "" : value;
}

private IssueDto normalizeIssue(IssueDto in) {
    String affectedAsset = safe(in.affectedAsset());
    String remarks = safe(in.assessmentRemarks());
    String cve = safe(in.cveCwe());
    String severity = safe(in.severity());
    String reportSection = normalizeReportSection(safe(in.reportSection()));
    String recommendation = safe(in.recommendationReference());

// Fix split URL suffix like ".ne" + "t/" leaking into remarks.
    if (affectedAsset.endsWith(".ne")) {
        Matcher m = Pattern.compile("^\\s*(t(?:/[^\\s]*)?)\\b").matcher(remarks);
        if (m.find()) {
            affectedAsset = affectedAsset + m.group(1);
            remarks = compact(remarks.substring(m.end()));
        }
    }

// Recover split CWE values (e.g., "CWE:" + "79,1021,319" in remarks).
    if ("CWE:".equalsIgnoreCase(cve) || "CWE".equalsIgnoreCase(cve)) {
        Matcher m = Pattern.compile("^\\s*([0-9,]+)\\b").matcher(remarks);
        if (m.find()) {
            cve = "CWE:" + m.group(1);
            remarks = compact(remarks.substring(m.end()));
        }
    }

// Normalize severity casing.
    if (severity.equalsIgnoreCase("informational") || severity.equalsIgnoreCase("information")) {
        severity = "Informational";
    }

// Standardize recommendation if it got blank due to line breaks.
    if (recommendation.isBlank() && !reportSection.isBlank()) {
        recommendation = "Refer to the Detailed Observations section.";
    }

    return new IssueDto(
            in.serialNo(),
            affectedAsset,
            safe(in.observationTitle()),
            cve,
            severity,
            recommendation,
            reportSection,
            safe(in.observationType()),
            safe(in.status()),
            remarks
    );
}

private String normalizeReportSection(String raw) {
    if (raw.isBlank()) {
        return "";
    }
    Matcher m = Pattern.compile("(Ref\\s*[0-9,\\s]+)", Pattern.CASE_INSENSITIVE).matcher(raw);
    if (m.find()) {
        return compact(m.group(1));
    }
    return compact(raw);
}

private static class RowBuilder {
    Integer serialNo;
    String affectedAsset = "";
    String observationTitle = "";
    String cveCwe = "";
    String severity = "";
    String recommendationReference = "";
    String reportSection = "";
    String observationType = "";
    String status = "";
    String assessmentRemarks = "";

    void appendAsset(String value) { affectedAsset = join(affectedAsset, value); }
    void appendObservationTitle(String value) { observationTitle = join(observationTitle, value); }
    void appendCveCwe(String value) { cveCwe = join(cveCwe, value); }
    void appendSeverity(String value) { severity = join(severity, value); }
    void appendRecommendationReference(String value) { recommendationReference = join(recommendationReference, value); }
    void appendReportSection(String value) { reportSection = join(reportSection, value); }
    void appendObservationType(String value) { observationType = join(observationType, value); }
    void appendStatus(String value) { status = join(status, value); }
    void appendAssessmentRemarks(String value) { assessmentRemarks = join(assessmentRemarks, value); }

    private String join(String a, String b) {
        if (a == null || a.isBlank()) return b == null ? "" : b;
        if (b == null || b.isBlank()) return a;
        return a + " " + b;
    }
}
}