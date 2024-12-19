package dev.stiebo.app.dtos;

import dev.stiebo.app.configuration.Region;

public record TransactionsByRegion(
        Region region,
        long transactions
) {
}
