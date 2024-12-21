package dev.stiebo.app.data;

import dev.stiebo.app.configuration.Region;
import dev.stiebo.app.configuration.TransactionStatus;
import dev.stiebo.app.dtos.DailyTransactionSummary;
import dev.stiebo.app.dtos.TransactionsByRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    @Query("SELECT COUNT(DISTINCT t.region) " +
            "FROM Transaction t " +
            "WHERE t.date BETWEEN :startDateTime AND :endDateTime " +
            "AND t.region <> :currentRegion")
    Long countDistinctRegionsInPeriodExcludingCurrentRegion(@Param("startDateTime") LocalDateTime startDateTime,
                                                            @Param("endDateTime") LocalDateTime endDateTime,
                                                            @Param("currentRegion") Region currentRegion);

    @Query("SELECT COUNT(DISTINCT t.ip) " +
            "FROM Transaction t " +
            "WHERE t.date BETWEEN :startDateTime AND :endDateTime " +
            "AND t.ip <> :currentIp")
    Long countDistinctIpsInPeriodExcludingCurrentIp(@Param("startDateTime") LocalDateTime startDateTime,
                                                    @Param("endDateTime") LocalDateTime endDateTime,
                                                    @Param("currentIp") String currentIp);

    List<Transaction> findAllByOrderByIdAsc();

    List<Transaction> findAllByNumberOrderByIdAsc(String number);

    @Query("SELECT new dev.stiebo.app.dtos.TransactionsByRegion(t.region, COUNT(t)) " +
            "FROM Transaction t " +
            "GROUP BY t.region")
    List<TransactionsByRegion> findTransactionsGroupedByRegion();

    @Query("""
            SELECT new dev.stiebo.app.dtos.DailyTransactionSummary
            (DATE_TRUNC('DAY', t.date) as date, t.result, COUNT(*))
            FROM Transaction t
            GROUP BY date, t.result
            ORDER BY date ASC
            """)
    List<DailyTransactionSummary> findDailyTransactionSummary();

    Long countByResult(TransactionStatus result);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.feedback IN (ALLOWED, MANUAL_PROCESSING, PROHIBITED)")
    Long countByFeedbackIsNotEmpty();

}
