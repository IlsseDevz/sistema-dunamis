package com.dunamis.sistema.service;

import com.dunamis.sistema.dto.BibleSchoolReportData;
import com.dunamis.sistema.dto.ChurchReportData;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfReportService {

    private static final Color HEADER_BG = new Color(45, 55, 72);
    private static final Color HEADER_TEXT = Color.WHITE;
    private static final Color ROW_ALT = new Color(248, 249, 250);
    private static final DateTimeFormatter GENERATED_AT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "PT"));

    public byte[] generateBibleSchoolReport(BibleSchoolReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(document, output);
            document.open();

            addTitle(document, "Relatório Mensal — Escola Bíblica");
            addSubtitle(document, data.getChurchName());
            addSubtitle(document, data.getBibleSchoolName());
            addPeriod(document, data.getPeriodLabel());
            document.add(spacer());

            PdfPTable table = newTable(2);
            addRow(table, "Total de alunos activos", String.valueOf(data.getTotalStudents()));
            addRow(table, "Alunos com presença no período", String.valueOf(data.getStudentsPresent()));
            addRow(table, "Alunos sem presença no período", String.valueOf(data.getStudentsAbsent()));
            addRow(table, "Registos de presença", String.valueOf(data.getTotalAttendanceRecords()));
            addRow(table, "Presenças registadas", String.valueOf(data.getPresentRecords()));
            addRow(table, "Ausências registadas", String.valueOf(data.getAbsentRecords()));
            addRow(table, "Percentagem de presença", formatPercentage(data.getAttendancePercentage()));
            addRow(table, "Média das notas do período", formatGrade(data.getAverageGrade()));
            document.add(table);

            addFooter(document);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível gerar o relatório PDF da Escola Bíblica.", ex);
        }
    }

    public byte[] generateChurchReport(ChurchReportData data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 48, 48, 56, 48);
            PdfWriter.getInstance(document, output);
            document.open();

            addTitle(document, "Relatório Mensal — Igreja");
            addSubtitle(document, data.getChurchName());
            addPeriod(document, data.getPeriodLabel());
            document.add(spacer());

            PdfPTable stats = newTable(2);
            addRow(stats, "Total de membros cadastrados", String.valueOf(data.getTotalRegistered()));
            addRow(stats, "Membros activos", String.valueOf(data.getActiveMembers()));
            addRow(stats, "Novos convertidos", String.valueOf(data.getNewConverts()));
            addRow(stats, "Visitantes", String.valueOf(data.getVisitors()));
            addRow(stats, "Batizados", String.valueOf(data.getBaptized()));
            addRow(stats, "Não batizados", String.valueOf(data.getNotBaptized()));
            addRow(stats, "Novos cadastros no período", String.valueOf(data.getNewRegistrationsInPeriod()));
            document.add(stats);

            document.add(spacer());
            document.add(sectionHeading("Distribuição por função na igreja"));

            PdfPTable functions = newTable(2);
            addHeader(functions, "Função", "Quantidade");
            boolean alternate = false;
            for (ChurchReportData.FunctionCount item : data.getFunctionDistribution()) {
                addDataRow(functions, item.getFunctionLabel(), String.valueOf(item.getCount()), alternate);
                alternate = !alternate;
            }
            if (data.getFunctionDistribution().isEmpty()) {
                addDataRow(functions, "Sem dados", "—", false);
            }
            document.add(functions);

            addFooter(document);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível gerar o relatório PDF da igreja.", ex);
        }
    }

    private void addTitle(Document document, String text) throws DocumentException {
        Font font = new Font(Font.HELVETICA, 18, Font.BOLD, HEADER_BG);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(8f);
        document.add(paragraph);
    }

    private void addSubtitle(Document document, String text) throws DocumentException {
        Font font = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.DARK_GRAY);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(4f);
        document.add(paragraph);
    }

    private void addPeriod(Document document, String period) throws DocumentException {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
        Paragraph paragraph = new Paragraph("Período: " + period, font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(12f);
        document.add(paragraph);
    }

    private Paragraph sectionHeading(String text) {
        Font font = new Font(Font.HELVETICA, 13, Font.BOLD, HEADER_BG);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingAfter(8f);
        return paragraph;
    }

    private Paragraph spacer() {
        return new Paragraph(" ", new Font(Font.HELVETICA, 8));
    }

    private PdfPTable newTable(int columns) throws DocumentException {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(4f);
        table.setSpacingAfter(8f);
        table.setWidths(new float[]{3f, 2f});
        return table;
    }

    private void addHeader(PdfPTable table, String left, String right) {
        table.addCell(headerCell(left));
        table.addCell(headerCell(right));
    }

    private void addRow(PdfPTable table, String label, String value) {
        table.addCell(labelCell(label, false));
        table.addCell(valueCell(value, false));
    }

    private void addDataRow(PdfPTable table, String label, String value, boolean alternate) {
        table.addCell(labelCell(label, alternate));
        table.addCell(valueCell(value, alternate));
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD, HEADER_TEXT)));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(8f);
        cell.setBorderColor(HEADER_BG);
        return cell;
    }

    private PdfPCell labelCell(String text, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK)));
        cell.setPadding(8f);
        if (alternate) {
            cell.setBackgroundColor(ROW_ALT);
        }
        return cell;
    }

    private PdfPCell valueCell(String text, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(8f);
        if (alternate) {
            cell.setBackgroundColor(ROW_ALT);
        }
        return cell;
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(spacer());
        Font font = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
        Paragraph footer = new Paragraph(
                "Gerado em " + LocalDateTime.now().format(GENERATED_AT) + " — Sistema Dunamis",
                font
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }

    private String formatPercentage(double value) {
        return String.format(Locale.forLanguageTag("pt-PT"), "%.1f%%", value);
    }

    private String formatGrade(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        return value.toPlainString();
    }
}
