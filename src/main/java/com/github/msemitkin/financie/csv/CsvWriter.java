package com.github.msemitkin.financie.csv;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.telegram.util.FormatterUtil;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class CsvWriter {

    public byte[] writeCsvFile(List<Transaction> transactions) {
        List<String[]> transactionRows = transactions.stream()
            .map(this::toTransactionEntity).toList();
        try (
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            CSVWriter csvWriter = new CSVWriter(outputStreamWriter);
        ) {
            csvWriter.writeNext(new String[]{"amount", "category", "time", "description"});
            csvWriter.writeAll(transactionRows);
            csvWriter.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String[] toTransactionEntity(Transaction transaction) {
        return new String[]{
            String.valueOf(transaction.amount()),
            transaction.category(),
            FormatterUtil.formatDateTime(transaction.time()),
            transaction.description()
        };
    }
}
