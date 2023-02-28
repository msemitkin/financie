package com.github.msemitkin.financie.telegram.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class FormatterUtilTest {

    static Stream<Arguments> formatNumberParameters() {
        return Stream.of(
            Arguments.of(1, "1"),
            Arguments.of(1.0, "1"),
            Arguments.of(1.1, "1.10"),
            Arguments.of(1.10, "1.10"),
            Arguments.of(1.11, "1.11"),
            Arguments.of(1.111, "1.11"),
            Arguments.of(1.115, "1.12")
        );
    }

    @ParameterizedTest
    @MethodSource("formatNumberParameters")
    void formatNumber(double givenNumber, String expectedFormat) {
        String actualFormat = FormatterUtil.formatNumber(givenNumber);

        Assertions.assertEquals(expectedFormat, actualFormat);
    }
}