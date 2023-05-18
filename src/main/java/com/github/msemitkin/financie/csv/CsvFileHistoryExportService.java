package com.github.msemitkin.financie.csv;

import com.github.msemitkin.financie.domain.Transaction;
import com.github.msemitkin.financie.domain.TransactionService;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
public class CsvFileHistoryExportService {
    private final TransactionService transactionService;
    private final CsvWriter csvWriter;

    public CsvFileHistoryExportService(
        TransactionService transactionService,
        CsvWriter csvWriter
    ) {
        this.transactionService = transactionService;
        this.csvWriter = csvWriter;
    }

    public byte[] getFileToExport(long userId, ZoneId timeZoneId) {
        List<Transaction> transactions = transactionService.getTransactions(userId)
            .stream()
            .map(tran -> new Transaction(
                tran.id(), tran.userId(), tran.amount(),
                tran.category(),
                tran.description(),
                tran.time().atZone(ZoneId.systemDefault()).withZoneSameInstant(timeZoneId).toLocalDateTime())
            ).toList();
        return csvWriter.writeCsvFile(transactions);
    }
}
