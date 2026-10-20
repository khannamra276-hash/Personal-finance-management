package com.financeapp.service;

import com.financeapp.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CsvExportService {

    public void exportTransactionsToCsv(List<Transaction> transactions, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("ID,Date,Type,Category,Amount,Description");
            for (Transaction t : transactions) {
                writer.printf("%d,%s,%s,\"%s\",%s,\"%s\"%n",
                        t.getId(),
                        t.getTransactionDate(),
                        t.getType(),
                        escapeCsv(t.getCategoryName()),
                        t.getAmount().toPlainString(),
                        escapeCsv(t.getDescription())
                );
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}

