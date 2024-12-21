package dev.stiebo.app.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionLimitRepository extends JpaRepository<TransactionLimit,Long> {
    @Query(value = "SELECT * FROM transactionlimit LIMIT 1", nativeQuery = true)
    Optional<TransactionLimit> findAnyOneTransactionLimit();
}
