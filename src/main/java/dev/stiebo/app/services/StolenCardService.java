package dev.stiebo.app.services;

import dev.stiebo.app.data.StolenCard;
import dev.stiebo.app.data.StolenCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class StolenCardService {
    private final StolenCardRepository repository;

    public StolenCardService(StolenCardRepository repository) {
        this.repository = repository;
    }

    public Page<StolenCard> list(Pageable pageable, Specification<StolenCard> filter) {
        return repository.findAll(filter, pageable);
    }

    public StolenCard create(StolenCard card) {
        if (repository.existsByNumber(card.getNumber())) {
            throw new RuntimeException("Card already in list.");
        }
        return repository.save(card);
    }

    public void delete(StolenCard card) {
        if (!repository.existsByNumber(card.getNumber())) {
            throw new RuntimeException("Card not in list.");
        }
        repository.delete(card);
    }

}
