package com.github.msemitkin.financie.telegram.callback.command;

import java.time.OffsetDateTime;

public record GetMonthlyReportCommand(OffsetDateTime utcYearMonth) {
}
