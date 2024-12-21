package dev.stiebo.app.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactionlimit")
public class TransactionLimit extends AbstractEntity{
    private Long maxAllowed;
    private Long maxManual;

    public TransactionLimit() {
    }

    public TransactionLimit(Long maxAllowed, Long maxManual) {
        this.maxAllowed = maxAllowed;
        this.maxManual = maxManual;
    }

    public Long getMaxAllowed() {
        return maxAllowed;
    }

    public TransactionLimit setMaxAllowed(Long maxAllowed) {
        this.maxAllowed = maxAllowed;
        return this;
    }

    public Long getMaxManual() {
        return maxManual;
    }

    public TransactionLimit setMaxManual(Long maxManual) {
        this.maxManual = maxManual;
        return this;
    }
}
