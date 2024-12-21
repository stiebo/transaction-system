package dev.stiebo.app.services;

import dev.stiebo.app.configuration.TransactionStatus;
import dev.stiebo.app.data.TransactionLimit;
import dev.stiebo.app.data.TransactionLimitRepository;
import dev.stiebo.app.data.TransactionRepository;
import dev.stiebo.app.dtos.DailyTransactionSummary;
import dev.stiebo.app.dtos.TransactionsByRegion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final TransactionRepository transactionRepository;
    private final TransactionLimitRepository transactionLimitRepository;

    public DashboardService(TransactionRepository transactionRepository, TransactionLimitRepository transactionLimitRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionLimitRepository = transactionLimitRepository;
    }

    public List<TransactionsByRegion> listTransactionsByRegion() {
        return transactionRepository.findTransactionsGroupedByRegion();
    }

    public List<DailyTransactionSummary> listDailyTransactionSummary() {
        return transactionRepository.findDailyTransactionSummary();
    }

    public TransactionLimit getLimit() {
        return transactionLimitRepository.findAnyOneTransactionLimit()
                .orElseThrow(() -> new RuntimeException("no TransactionLimits found"));
    }

    public Long getTotalTransactions() {
        return transactionRepository.count();
    }

    public Long getRejectedTransactions() {
        return transactionRepository.countByResult(TransactionStatus.PROHIBITED);
    }

    public Long getReviewedTransactions() {
        return transactionRepository.countByFeedbackIsNotEmpty();
    }




}
