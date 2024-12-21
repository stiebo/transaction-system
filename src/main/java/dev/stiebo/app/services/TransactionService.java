package dev.stiebo.app.services;

import dev.stiebo.app.configuration.TransactionStatus;
import dev.stiebo.app.data.*;
import dev.stiebo.app.dtos.PostTransactionFeedback;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final SuspiciousIpRepository suspiciousIPRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionLimitRepository transactionLimitRepository;
    private TransactionLimit transactionLimit;

    public TransactionService(TransactionRepository transactionRepository,
                              SuspiciousIpRepository suspiciousIPRepository,
                              StolenCardRepository stolenCardRepository,
                              TransactionLimitRepository transactionLimitRepository,
                              @Qualifier("defaultMaxAllowed")
                              Long defaultMaxAllowed,
                              @Qualifier("defaultMaxManual")
                              Long defaultMaxManual) {
        this.transactionRepository = transactionRepository;
        this.suspiciousIPRepository = suspiciousIPRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionLimitRepository = transactionLimitRepository;
        transactionLimit = transactionLimitRepository.findAnyOneTransactionLimit()
                .orElseGet(() -> {
                    return transactionLimitRepository.save(
                            new TransactionLimit(defaultMaxAllowed, defaultMaxManual));
                });
    }

    @Transactional
    public PostTransactionFeedback postTransaction(Transaction transaction) {
        TransactionStatus result;
        StringBuilder info = new StringBuilder();

        if (transaction.getAmount() <= transactionLimit.getMaxAllowed()) {
            result = TransactionStatus.ALLOWED;
            info.append("none");
        } else if (transaction.getAmount() <= transactionLimit.getMaxManual()) {
            result = TransactionStatus.MANUAL_PROCESSING;
            info.append("amount");
        } else {
            result = TransactionStatus.PROHIBITED;
            info.append("amount");
        }

        if (stolenCardRepository.existsByNumber(transaction.getNumber())) {
            if (result == TransactionStatus.PROHIBITED) {
                info.append(", ");
            } else {
                result = TransactionStatus.PROHIBITED;
                info.setLength(0);
            }
            info.append("card-number");
        }

        if (suspiciousIPRepository.existsByIp(transaction.getIp())) {
            if (result == TransactionStatus.PROHIBITED) {
                info.append(", ");
            } else {
                result = TransactionStatus.PROHIBITED;
                info.setLength(0);
            }
            info.append("ip");
        }

        LocalDateTime oneHourAgo = transaction.getDate().minusHours(1);
        Long countTransactionsDiffRegion = transactionRepository.
                countDistinctRegionsInPeriodExcludingCurrentRegion(oneHourAgo, transaction.getDate(),
                        transaction.getRegion());

        if (countTransactionsDiffRegion > 2) {
            if (result == TransactionStatus.PROHIBITED) {
                info.append(", ");
            } else {
                result = TransactionStatus.PROHIBITED;
                info.setLength(0);
            }
            info.append("region-correlation");
        } else if (countTransactionsDiffRegion == 2 && !(result == TransactionStatus.PROHIBITED)) {
            if (result == TransactionStatus.MANUAL_PROCESSING) {
                info.append(", ");
            } else {
                result = TransactionStatus.MANUAL_PROCESSING;
                info.setLength(0);
            }
            info.append("region-correlation");
        }

        Long countTransactionUniqueDiffIp = transactionRepository
                .countDistinctIpsInPeriodExcludingCurrentIp(oneHourAgo, transaction.getDate(),
                        transaction.getIp());

        if (countTransactionUniqueDiffIp > 2) {
            if (result == TransactionStatus.PROHIBITED) {
                info.append(", ");
            } else {
                result = TransactionStatus.PROHIBITED;
                info.setLength(0);
            }
            info.append("ip-correlation");
        } else if (countTransactionUniqueDiffIp == 2 && !(result == TransactionStatus.PROHIBITED)) {
            if (result == TransactionStatus.MANUAL_PROCESSING) {
                info.append(", ");
            } else {
                result = TransactionStatus.MANUAL_PROCESSING;
                info.setLength(0);
            }
            info.append("ip-correlation");
        }

        transaction
                .setResult(result)
                .setFeedback(null);
        transactionRepository.save(transaction);
        return new PostTransactionFeedback(result, info.toString());
    }

    @Transactional
    public void updateTransactionFeedback(Long id, TransactionStatus feedback) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));
        if (!(transaction.getFeedback() == null)) {
            throw new RuntimeException("Feedback already existed");
        }

        // if validity equals feedback: exception
        if (transaction.getResult() == feedback) {
            throw new RuntimeException("Feedback cannot equal Result.");
        }

        // save feedback into db
        transaction.setFeedback(feedback);
        transactionRepository.save(transaction);

        // adjust limits
        if (transaction.getResult() == TransactionStatus.ALLOWED) {
            // dec maxAllowed
            transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                    transaction.getAmount(), false));
            if (feedback == TransactionStatus.PROHIBITED) {
                // dec maxManual
                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                        transaction.getAmount(), false));
            }
        } else if (transaction.getResult() == TransactionStatus.MANUAL_PROCESSING) {
            if (feedback == TransactionStatus.ALLOWED) {
                // inc maxAllowed
                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                        transaction.getAmount(), true));
            } else {
                // dec maxManual
                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                        transaction.getAmount(), false));
            }
        } else {
            // inc Manual
            transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                    transaction.getAmount(), true));
            if (feedback == TransactionStatus.ALLOWED) {
                // inc maxAllowed
                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                        transaction.getAmount(), true));
            }
        }

        // save new limit
        transactionLimit = transactionLimitRepository.save(transactionLimit);
//        return mapper.toDto(transaction);
    }

    private Long updateLimit(Long currentLimit, Long transactionValue, Boolean increase) {
        return (long) Math.ceil((0.8 * currentLimit +
                (increase ? 0.2 * transactionValue : -0.2 * transactionValue)));
    }

   public Page<Transaction> list(Pageable pageable, Specification<Transaction> filter) {
        return transactionRepository.findAll(filter, pageable);
    }

}
