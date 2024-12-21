package dev.stiebo.app.dtos;

import dev.stiebo.app.configuration.TransactionStatus;

public record PostTransactionFeedback(
        TransactionStatus result,
        String info
) {
}
