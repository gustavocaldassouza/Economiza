package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportDataUseCase {

        // ── Brand colours ───────────────────────────────────────────────────────
        private static final DeviceRgb TEAL = new DeviceRgb(0x00, 0xB0, 0xA0);
        private static final DeviceRgb TEAL_DARK = new DeviceRgb(0x00, 0x7C, 0x72);
        private static final DeviceRgb GREEN = new DeviceRgb(0x00, 0xC8, 0x7A);
        private static final DeviceRgb GREEN_BG = new DeviceRgb(0xE6, 0xF9, 0xF2);
        private static final DeviceRgb RED = new DeviceRgb(0xE5, 0x3E, 0x3E);
        private static final DeviceRgb RED_BG = new DeviceRgb(0xFD, 0xEB, 0xEB);
        private static final DeviceRgb BLUE_BG = new DeviceRgb(0xE8, 0xF4, 0xFF);
        private static final DeviceRgb BLUE_ACCENT = new DeviceRgb(0x3D, 0x8B, 0xFF);
        private static final DeviceRgb ROW_ALT = new DeviceRgb(0xF5, 0xF7, 0xFA);
        private static final DeviceRgb ROW_WHITE = new DeviceRgb(0xFF, 0xFF, 0xFF);
        private static final DeviceRgb TEXT_DARK = new DeviceRgb(0x1A, 0x1A, 0x2E);
        private static final DeviceRgb TEXT_MUTED = new DeviceRgb(0x6B, 0x7A, 0x90);
        private static final DeviceRgb BORDER_COLOR = new DeviceRgb(0xE2, 0xE8, 0xF0);
        // ────────────────────────────────────────────────────────────────────────

        private final TransactionRepository repository;
        private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        private final SimpleDateFormat sdfLong = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());

        public ExportDataUseCase(TransactionRepository repository) {
                this.repository = repository;
        }

        // ── CSV ─────────────────────────────────────────────────────────────────

        public void executeToCsv(File outFile) throws IOException {
                List<Transaction> transactions = repository.getAllTransactionsSync();
                try (CSVWriter writer = new CSVWriter(new FileWriter(outFile))) {
                        writer.writeNext(new String[] { "ID", "Type", "Amount", "Description", "Date", "Category ID" });
                        for (Transaction t : transactions) {
                                writer.writeNext(new String[] {
                                                String.valueOf(t.id),
                                                t.isIncome ? "Income" : "Expense",
                                                String.format(Locale.getDefault(), "%.2f", t.amount / 100.0),
                                                t.description != null ? t.description : "",
                                                sdf.format(new Date(t.timestamp)),
                                                String.valueOf(t.categoryId)
                                });
                        }
                } catch (Exception e) {
                        throw new IOException("CSV export failed: " + e.getMessage(), e);
                }
        }

        // ── PDF ─────────────────────────────────────────────────────────────────

        public void executeToPdf(File outFile) throws IOException {
                List<Transaction> transactions = repository.getAllTransactionsSync();

                // Compute totals
                long incomeTotal = 0, expenseTotal = 0;
                for (Transaction t : transactions) {
                        if (t.isIncome)
                                incomeTotal += t.amount;
                        else
                                expenseTotal += t.amount;
                }
                long netBalance = incomeTotal - expenseTotal;

                try (PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(outFile))) {
                        PdfDocument pdf = new PdfDocument(pdfWriter);
                        Document document = new Document(pdf);
                        document.setMargins(0, 36, 48, 36); // extra bottom margin for footer

                        // Register footer event handler
                        pdf.addEventHandler(PdfDocumentEvent.END_PAGE,
                                        new FooterHandler(sdfLong.format(new Date())));

                        // ── 1. Header band ───────────────────────────────────────────
                        addHeader(document, pdf, transactions.size());
                        document.setTopMargin(24);

                        // ── 2. Summary cards ─────────────────────────────────────────
                        addSummarySection(document, incomeTotal, expenseTotal, netBalance);

                        // ── 3. Section label ─────────────────────────────────────────
                        document.add(new Paragraph("Transaction History")
                                        .setFontSize(11)
                                        .setBold()
                                        .setFontColor(TEXT_DARK)
                                        .setMarginTop(20)
                                        .setMarginBottom(8));

                        // ── 4. Data table ────────────────────────────────────────────
                        if (transactions.isEmpty()) {
                                document.add(new Paragraph("No transactions found.")
                                                .setFontSize(10)
                                                .setFontColor(TEXT_MUTED)
                                                .setTextAlignment(TextAlignment.CENTER)
                                                .setMarginTop(24));
                        } else {
                                addTransactionTable(document, transactions);
                        }

                        document.close();
                } catch (Exception e) {
                        throw new IOException("PDF export failed: " + e.getMessage(), e);
                }
        }

        // ── Header band ──────────────────────────────────────────────────────────

        private void addHeader(Document document, PdfDocument pdf, int txCount) {

                // Header content via layout table
                Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 60, 40 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setBorder(Border.NO_BORDER);

                // Left cell: app name + subtitle
                Cell left = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .setBackgroundColor(TEAL_DARK)
                                .setPaddingLeft(36)
                                .setPaddingTop(22)
                                .setPaddingBottom(18);
                left.add(new Paragraph("ECONOMIZA")
                                .setFontSize(26)
                                .setBold()
                                .setFontColor(ColorConstants.WHITE)
                                .setMargin(0));
                left.add(new Paragraph("Transaction Report")
                                .setFontSize(10)
                                .setFontColor(new DeviceRgb(0xCC, 0xFF, 0xF6))
                                .setMarginTop(2).setMarginBottom(0));
                headerTable.addCell(left);

                // Right cell: count + date
                Cell right = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .setBackgroundColor(TEAL)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setPaddingRight(24)
                                .setPaddingTop(22)
                                .setPaddingBottom(18);
                right.add(new Paragraph(String.valueOf(txCount))
                                .setFontSize(28)
                                .setBold()
                                .setFontColor(ColorConstants.WHITE)
                                .setMargin(0));
                right.add(new Paragraph("transactions")
                                .setFontSize(9)
                                .setFontColor(new DeviceRgb(0xCC, 0xFF, 0xF6))
                                .setMarginTop(2));
                right.add(new Paragraph(sdf.format(new Date()))
                                .setFontSize(8)
                                .setFontColor(new DeviceRgb(0xCC, 0xFF, 0xF6))
                                .setMarginTop(4));
                headerTable.addCell(right);

                document.add(headerTable);
        }

        // ── Summary cards ────────────────────────────────────────────────────────

        private void addSummarySection(Document document, long income, long expense, long net) {
                Table cards = new Table(UnitValue.createPercentArray(new float[] { 1, 1, 1 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginTop(8)
                                .setBorder(Border.NO_BORDER);

                cards.addCell(summaryCard("Total Income", formatMoney(income), GREEN, GREEN_BG, "▲ "));
                cards.addCell(summaryCard("Total Expenses", formatMoney(expense), RED, RED_BG, "▼ "));
                DeviceRgb netColor = net >= 0 ? BLUE_ACCENT : RED;
                DeviceRgb netBg = net >= 0 ? BLUE_BG : RED_BG;
                cards.addCell(summaryCard("Net Balance", formatMoney(Math.abs(net)), netColor, netBg,
                                net >= 0 ? "= " : "- "));

                document.add(cards);
        }

        private Cell summaryCard(String label, String value,
                        DeviceRgb textColor, DeviceRgb bgColor, String icon) {
                Cell card = new Cell()
                                .setBackgroundColor(bgColor)
                                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                                .setPadding(12)
                                .setMargin(3);
                card.add(new Paragraph(icon + label)
                                .setFontSize(8)
                                .setFontColor(TEXT_MUTED)
                                .setMargin(0));
                card.add(new Paragraph(value)
                                .setFontSize(13)
                                .setBold()
                                .setFontColor(textColor)
                                .setMarginTop(4).setMarginBottom(0));
                return card;
        }

        // ── Transaction table ────────────────────────────────────────────────────

        private void addTransactionTable(Document document, List<Transaction> transactions) {
                float[] colWidths = { 0.5f, 1.5f, 3.5f, 1f, 1.5f, 1f };
                Table table = new Table(UnitValue.createPercentArray(colWidths))
                                .setWidth(UnitValue.createPercentValue(100));

                // Header row
                for (String h : new String[] { "#", "Date", "Description", "Cat.", "Amount", "Type" }) {
                        table.addHeaderCell(
                                        new Cell().add(new Paragraph(h).setFontSize(8).setBold()
                                                        .setFontColor(ColorConstants.WHITE))
                                                        .setBackgroundColor(TEAL_DARK)
                                                        .setBorder(Border.NO_BORDER)
                                                        .setPadding(7));
                }

                // Data rows
                for (int i = 0; i < transactions.size(); i++) {
                        Transaction t = transactions.get(i);
                        DeviceRgb rowBg = (i % 2 == 0) ? ROW_WHITE : ROW_ALT;

                        table.addCell(dataCell(String.valueOf(i + 1), rowBg, TextAlignment.CENTER));
                        table.addCell(dataCell(sdf.format(new Date(t.timestamp)), rowBg, TextAlignment.LEFT));
                        table.addCell(dataCell(t.description != null ? t.description : "—", rowBg, TextAlignment.LEFT));
                        table.addCell(dataCell(String.valueOf(t.categoryId), rowBg, TextAlignment.CENTER));

                        // Amount — colour coded
                        String amtText = (t.isIncome ? "+" : "-") + formatMoney(t.amount);
                        table.addCell(new Cell()
                                        .add(new Paragraph(amtText).setFontSize(7).setBold()
                                                        .setFontColor(t.isIncome ? GREEN : RED))
                                        .setBackgroundColor(rowBg)
                                        .setBorder(Border.NO_BORDER)
                                        .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.3f))
                                        .setPadding(6)
                                        .setTextAlignment(TextAlignment.RIGHT));

                        // Type badge
                        table.addCell(new Cell()
                                        .add(new Paragraph(t.isIncome ? "INC" : "EXP")
                                                        .setFontSize(6).setBold()
                                                        .setFontColor(t.isIncome ? GREEN : RED))
                                        .setBackgroundColor(t.isIncome ? GREEN_BG : RED_BG)
                                        .setBorder(Border.NO_BORDER)
                                        .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.3f))
                                        .setPadding(6)
                                        .setTextAlignment(TextAlignment.CENTER));
                }

                document.add(table);
        }

        private Cell dataCell(String text, DeviceRgb bg, TextAlignment align) {
                return new Cell()
                                .add(new Paragraph(text).setFontSize(7).setFontColor(TEXT_DARK))
                                .setBackgroundColor(bg)
                                .setBorder(Border.NO_BORDER)
                                .setBorderBottom(new SolidBorder(BORDER_COLOR, 0.3f))
                                .setPadding(6)
                                .setTextAlignment(align);
        }

        // ── Helpers ──────────────────────────────────────────────────────────────

        private String formatMoney(long cents) {
                return String.format(Locale.getDefault(), "$%.2f", cents / 100.0);
        }

        // ── Footer handler (iText 7 API) ─────────────────────────────────────────

        private static class FooterHandler implements IEventHandler {
                private final String timestamp;

                FooterHandler(String timestamp) {
                        this.timestamp = timestamp;
                }

                @Override
                public void handleEvent(Event event) {
                        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                        PdfDocument pdf = docEvent.getDocument();
                        PdfPage page = docEvent.getPage();
                        int pageNum = pdf.getPageNumber(page);
                        int totalPages = pdf.getNumberOfPages();
                        Rectangle pageSize = page.getPageSize();

                        // Draw separator line
                        PdfCanvas pdfCanvas = new PdfCanvas(page);
                        pdfCanvas.setStrokeColor(TEAL_DARK)
                                        .setLineWidth(0.5f)
                                        .moveTo(36, 36)
                                        .lineTo(pageSize.getWidth() - 36, 36)
                                        .stroke()
                                        .release();

                        // Left footer: generation info
                        float pageW = pageSize.getWidth();
                        try (Canvas leftCanvas = new Canvas(new PdfCanvas(page),
                                        new Rectangle(36, 10, pageW / 2f - 36, 20))) {
                                leftCanvas.add(new Paragraph("Generated by Economiza  •  " + timestamp)
                                                .setFontSize(7)
                                                .setFontColor(TEXT_MUTED)
                                                .setMargin(0));
                        }

                        // Right footer: page number
                        float rightW = 80f;
                        try (Canvas rightCanvas = new Canvas(new PdfCanvas(page),
                                        new Rectangle(pageW - 36 - rightW, 10, rightW, 20))) {
                                rightCanvas.add(new Paragraph("Page " + pageNum + " of " + totalPages)
                                                .setFontSize(7)
                                                .setFontColor(TEXT_MUTED)
                                                .setTextAlignment(TextAlignment.RIGHT)
                                                .setMargin(0));
                        }
                }
        }
}
