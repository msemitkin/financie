package com.github.msemitkin.financie.csvimport;

import com.github.msemitkin.financie.domain.SaveTransactionCommand;
import com.github.msemitkin.financie.domain.TransactionService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

@Service
public class CsvFileHistoryImportService {
    private final CsvParser csvParser;
    private final TransactionService transactionService;

    public CsvFileHistoryImportService(
        CsvParser csvParser,
        TransactionService transactionService
    ) {
        this.csvParser = csvParser;
        this.transactionService = transactionService;
    }

    public void importTransactions(long userId, byte[] payload, ZoneId zoneId) {
        try {
            List<TransactionEntity> transactions = getTransactions(payload);
            List<SaveTransactionCommand> commands = transactions.stream()
                .map(tran -> toCommand(userId, tran, zoneId))
                .toList();
            transactionService.saveTransactions(commands);
        } catch (RuntimeException e) {
            throw new ImportTransactionsException(e);
        }
    }

    private List<TransactionEntity> getTransactions(byte[] payload) {
        try {
            return csvParser.parseTransactions(payload, TransactionEntity.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SaveTransactionCommand toCommand(long userId, TransactionEntity tran, ZoneId zoneId) {
        return new SaveTransactionCommand(
            userId,
            tran.getAmount(),
            tran.getCategory(),
            tran.getDescription(),
            tran.getTransactionTime().atZone(zoneId).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        );
    }
}
