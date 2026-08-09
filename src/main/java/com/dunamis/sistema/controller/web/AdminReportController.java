package com.dunamis.sistema.controller.web;

import com.dunamis.sistema.dto.request.ReportPeriodRequest;
import com.dunamis.sistema.service.PdfReportService;
import com.dunamis.sistema.service.ReportDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.YearMonth;

@Controller
@RequestMapping("/admin/relatorios")
public class AdminReportController {

    private final ReportDataService reportDataService;
    private final PdfReportService pdfReportService;

    public AdminReportController(ReportDataService reportDataService, PdfReportService pdfReportService) {
        this.reportDataService = reportDataService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping
    public String index(Model model) {
        preparePage(model, defaultPeriodRequest());
        return "admin/relatorios/index";
    }

    @PostMapping("/escola-biblica/pdf")
    public Object bibleSchoolPdf(
            @Valid @ModelAttribute("period") ReportPeriodRequest period,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            preparePage(model, period);
            return "admin/relatorios/index";
        }

        var data = reportDataService.buildBibleSchoolReport(period.getMonth(), period.getYear());
        StreamingResponseBody stream = outputStream -> pdfReportService.generateBibleSchoolReport(data, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName("escola-biblica", period) + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream);
    }

    @PostMapping("/igreja/pdf")
    public Object churchPdf(
            @Valid @ModelAttribute("period") ReportPeriodRequest period,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            preparePage(model, period);
            return "admin/relatorios/index";
        }

        var data = reportDataService.buildChurchReport(period.getMonth(), period.getYear());
        StreamingResponseBody stream = outputStream -> pdfReportService.generateChurchReport(data, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName("igreja", period) + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(stream);
    }

    private void preparePage(Model model, ReportPeriodRequest period) {
        model.addAttribute("activeMenu", "relatorios");
        model.addAttribute("period", period);
    }

    private ReportPeriodRequest defaultPeriodRequest() {
        YearMonth current = YearMonth.now();
        ReportPeriodRequest request = new ReportPeriodRequest();
        request.setMonth(current.getMonthValue());
        request.setYear(current.getYear());
        return request;
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String fileName) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private String fileName(String prefix, ReportPeriodRequest period) {
        return String.format("relatorio-%s-%02d-%d.pdf", prefix, period.getMonth(), period.getYear());
    }
}
