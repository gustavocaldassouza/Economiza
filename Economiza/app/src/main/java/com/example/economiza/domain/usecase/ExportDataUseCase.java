package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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
    private final TransactionRepository repository;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ExportDataUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public void executeToCsv(File outFile) throws IOException {
        List<Transaction> transactions = repository.getAllTransactionsSync();

        try (CSVWriter writer = new CSVWriter(new FileWriter(outFile))) {
            writer.writeNext(new String[] { "ID", "Type", "Amount (R$)", "Description", "Date", "Category ID" });
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

    public void executeToPdf(File outFile) throws IOException {
        List<Transaction> transactions = repository.getAllTransactionsSync();

        try (PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(outFile))) {
            PdfDocument pdf = new PdfDocument(pdfWriter);
            Document document = new Document(pdf);

            document.add(new Paragraph("Economiza – Transaction Report")
                    .setBold().setFontSize(18));
            document.add(new Paragraph("Generated on " + sdf.format(new Date()))
                    .setFontSize(10));
            document.add(new Paragraph(" "));

            float[] widths = { 1, 3, 5, 3, 2 };
            Table table = new Table(widths);
            String[] headers = { "ID", "Amount", "Description", "Date", "Type" };
            for (String h : headers)
                table.addCell(h);

            for (Transaction t : transactions) {
                table.addCell(String.valueOf(t.id));
                table.addCell(String.format(Locale.getDefault(), "R$%.2f", t.amount / 100.0));
                table.addCell(t.description != null ? t.description : "");
                table.addCell(sdf.format(new Date(t.timestamp)));
                table.addCell(t.isIncome ? "Income" : "Expense");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("PDF export failed: " + e.getMessage(), e);
        }
    }
}
