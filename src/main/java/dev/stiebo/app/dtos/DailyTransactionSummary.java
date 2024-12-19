package dev.stiebo.app.dtos;

import dev.stiebo.app.configuration.TransactionStatus;

import java.time.LocalDateTime;

public record DailyTransactionSummary(
    LocalDateTime date,
    TransactionStatus type,
    long count
) {}
