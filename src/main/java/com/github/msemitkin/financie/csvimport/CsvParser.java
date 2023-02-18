package com.github.msemitkin.financie.csvimport;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Component
public class CsvParser {

    public <T> List<T> parseTransactions(byte[] bytes, Class<T> tClass) throws IOException {
        try (
            InputStream is = new ByteArrayInputStream(bytes);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(isr)
        ) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(bufferedReader)
                .withType(tClass)
                .withIgnoreEmptyLine(true)
                .build();
            return csvToBean.parse();
        }
    }

}
