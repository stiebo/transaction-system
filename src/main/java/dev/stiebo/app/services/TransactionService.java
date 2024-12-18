package dev.stiebo.app.services;

import dev.stiebo.app.data.*;
import dev.stiebo.app.dtos.PostTransactionFeedback;
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

    public PostTransactionFeedback postTransaction(Transaction transaction) {
        String result;
        StringBuilder info = new StringBuilder();

        if (transaction.getAmount() <= transactionLimit.getMaxAllowed()) {
            result = "ALLOWED";
            info.append("none");
        } else if (transaction.getAmount() <= transactionLimit.getMaxManual()) {
            result = "MANUAL_PROCESSING";
            info.append("amount");
        } else {
            result = "PROHIBITED";
            info.append("amount");
        }

        if (stolenCardRepository.existsByNumber(transaction.getNumber())) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("card-number");
        }

        if (suspiciousIPRepository.existsByIp(transaction.getIp())) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("ip");
        }

        LocalDateTime oneHourAgo = transaction.getDate().minusHours(1);
        Long countTransactionsDiffRegion = transactionRepository.
                countDistinctRegionsInPeriodExcludingCurrentRegion(oneHourAgo, transaction.getDate(),
                        transaction.getRegion());

        if (countTransactionsDiffRegion > 2) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("region-correlation");
        } else if (countTransactionsDiffRegion == 2 && !result.equals("PROHIBITED")) {
            if (result.equals("MANUAL_PROCESSING")) {
                info.append(", ");
            } else {
                result = "MANUAL_PROCESSING";
                info.setLength(0);
            }
            info.append("region-correlation");
        }

        Long countTransactionUniqueDiffIp = transactionRepository
                .countDistinctIpsInPeriodExcludingCurrentIp(oneHourAgo, transaction.getDate(),
                        transaction.getIp());

        if (countTransactionUniqueDiffIp > 2) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("ip-correlation");
        } else if (countTransactionUniqueDiffIp == 2 && !result.equals("PROHIBITED")) {
            if (result.equals("MANUAL_PROCESSING")) {
                info.append(", ");
            } else {
                result = "MANUAL_PROCESSING";
                info.setLength(0);
            }
            info.append("ip-correlation");
        }

        transaction
                .setResult(result)
                .setFeedback("");
        transactionRepository.save(transaction);
        return new PostTransactionFeedback(result, info.toString());
    }

//    public TransactionOutDto updateTransactionFeedback(UpdateTransactionFeedback feedback) {
//        Transaction transaction = transactionRepository.findById(feedback.transactionId())
//                .orElseThrow(TransactionNotFoundException::new);
//        if (!transaction.getFeedback().isEmpty()) {
//            throw new TransactionFeedbackAlreadyExistsException();
//        }
//
//        // if validity equals feedback: exception
//        if (transaction.getResult().equals(feedback.feedback())) {
//            throw new TransactionFeedbackUnprocessableException();
//        }
//
//        // save feedback into db
//        transaction.setFeedback(feedback.feedback());
//        transactionRepository.save(transaction);
//
//        // adjust limits
//        if (transaction.getResult().equals("ALLOWED")) {
//            // dec maxAllowed
//            transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
//                    transaction.getAmount(), false));
//            if (feedback.feedback().equals("PROHIBITED")) {
//                // dec maxManual
//                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
//                        transaction.getAmount(), false));
//            }
//        } else if (transaction.getResult().equals("MANUAL_PROCESSING")) {
//            if (feedback.feedback().equals("ALLOWED")) {
//                // inc maxAllowed
//                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
//                        transaction.getAmount(), true));
//            } else {
//                // dec maxManual
//                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
//                        transaction.getAmount(), false));
//            }
//        } else {
//            // inc Manual
//            transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
//                    transaction.getAmount(), true));
//            if (feedback.feedback().equals("ALLOWED")) {
//                // inc maxAllowed
//                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
//                        transaction.getAmount(), true));
//            }
//        }
//
//        // save new limit
//        transactionLimit = transactionLimitRepository.save(transactionLimit);
//        return mapper.toDto(transaction);
//    }
//
//    private Long updateLimit(Long currentLimit, Long transactionValue, Boolean increase) {
//        return (long) Math.ceil((0.8 * currentLimit +
//                (increase ? 0.2 * transactionValue : -0.2 * transactionValue)));
//    }

   public Page<Transaction> list(Pageable pageable, Specification<Transaction> filter) {
        return transactionRepository.findAll(filter, pageable);
    }

//    public TransactionOutDto[] getTransactionHistory() {
//        List<Transaction> transactions = transactionRepository.findAllByOrderByIdAsc();
//        return transactions.stream()
//                .map(mapper::toDto)
//                .toArray(TransactionOutDto[]::new);
//    }
//
//    public TransactionOutDto[] getTransactionHistoryByNumber(String number) {
//        List<Transaction> transactions = transactionRepository.findAllByNumberOrderByIdAsc(number);
//        if (transactions.isEmpty()) {
//            throw new TransactionNotFoundException();
//        }
//        return transactions.stream()
//                .map(mapper::toDto)
//                .toArray(TransactionOutDto[]::new);
//    }
//
//    public SuspiciousIpOutDto postSuspiciousIp(SuspiciousIpInDto suspiciousIpInDto) {
//        if (suspiciousIPRepository.existsByIp(suspiciousIpInDto.ip())) {
//            throw new SuspiciousIpExistsException();
//        }
//        return mapper.toDto(suspiciousIPRepository.save(mapper.toSuspisiousIp(suspiciousIpInDto)));
//    }
//
//    public void deleteSuspiciousIp(String ip) {
//        SuspiciousIp suspiciousIp = suspiciousIPRepository.findByIp(ip)
//                .orElseThrow(SuspiciousIpNotFoundException::new);
//        suspiciousIPRepository.delete(suspiciousIp);
//    }
//
//    public SuspiciousIpOutDto[] getSuspiciousIps() {
//        List<SuspiciousIp> suspiciousIps = suspiciousIPRepository.findAllByOrderByIdAsc();
//        return suspiciousIps.stream()
//                .map(mapper::toDto)
//                .toArray(SuspiciousIpOutDto[]::new);
//    }
//
//    public StolenCardOutDto postStolenCard(StolenCardInDto stolenCardInDto) {
//        if (stolenCardRepository.existsByNumber(stolenCardInDto.number())) {
//            throw new StolenCardExistsException();
//        }
//        return mapper.toDto(stolenCardRepository.save(mapper.toStolenCard(stolenCardInDto)));
//    }
//
//    public void deleteStolenCard(String number) {
//        StolenCard stolenCard = stolenCardRepository.findByNumber(number)
//                .orElseThrow(StolenCardNotFoundException::new);
//        stolenCardRepository.delete(stolenCard);
//    }
//
//    public StolenCardOutDto[] getStolenCards() {
//        List<StolenCard> stolenCards = stolenCardRepository.findAllByOrderByIdAsc();
//        return stolenCards.stream()
//                .map(mapper::toDto)
//                .toArray(StolenCardOutDto[]::new);
//    }

}
