package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.TransactionRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.opencsv.CSVWriter;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportDataUseCase {
    private final TransactionRepository repository;

    public ExportDataUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public void executeToCsv(String filePath) throws IOException {
        List<Transaction> transactions = repository.getAllTransactionsSync();

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write Header
            String[] header = { "ID", "Amount", "Description", "Date", "Category ID", "Is Income" };
            writer.writeNext(header);

            // Write Data
            for (Transaction transaction : transactions) {
                String[] data = {
                        String.valueOf(transaction.id),
                        String.valueOf(transaction.amount),
                        transaction.description != null ? transaction.description : "",
                        String.valueOf(transaction.timestamp),
                        String.valueOf(transaction.categoryId),
                        String.valueOf(transaction.isIncome)
                };
                writer.writeNext(data);
            }
        } catch (Exception e) {
            throw new IOException("Failed to export data to CSV", e);
        }
    }

    public void executeToPdf(String filePath) throws IOException {
        List<Transaction> transactions = repository.getAllTransactionsSync();

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(filePath))) {
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Economiza - Transaction Report").setBold().setFontSize(18));

            float[] columnWidths = { 1, 3, 5, 3, 2 };
            Table table = new Table(columnWidths);

            table.addCell("ID");
            table.addCell("Amount");
            table.addCell("Description");
            table.addCell("Date");
            table.addCell("Type");

            for (Transaction transaction : transactions) {
                table.addCell(String.valueOf(transaction.id));
                table.addCell(String.valueOf(transaction.amount));
                table.addCell(transaction.description != null ? transaction.description : "");
                table.addCell(String.valueOf(transaction.timestamp));
                table.addCell(transaction.isIncome ? "Income" : "Expense");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to export data to PDF", e);
        }
    }
}
