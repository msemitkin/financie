package com.github.msemitkin.financie.csv;

import com.github.msemitkin.financie.domain.TransactionService;
import com.github.msemitkin.financie.domain.util.TransactionUtil;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
        return transactionService.getTransactions(userId)
            .stream()
            .map(tran -> TransactionUtil.atZoneSameInstant(tran, timeZoneId))
            .collect(Collectors.collectingAndThen(toList(), csvWriter::writeCsvFile));
    }
}
