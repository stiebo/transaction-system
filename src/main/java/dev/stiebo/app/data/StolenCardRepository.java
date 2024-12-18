package dev.stiebo.app.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long>,
        JpaSpecificationExecutor<StolenCard> {
    Boolean existsByNumber(String number);

    Optional<StolenCard> findByNumber(String number);

    List<StolenCard> findAllByOrderByIdAsc();
}
