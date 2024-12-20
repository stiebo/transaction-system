package dev.stiebo.app.data;

import dev.stiebo.app.configuration.Region;
import dev.stiebo.app.configuration.TransactionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.LuhnCheck;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction extends AbstractEntity {
    private Long amount;
    private String ip;
    @LuhnCheck(message = "Not a valid credit card number")
    private String number;
    @Enumerated(EnumType.STRING)
    private Region region;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private TransactionStatus result;
    @Enumerated(EnumType.STRING)
    private TransactionStatus feedback;

    public Transaction() {
    }

    public Long getAmount() {
        return amount;
    }

    public Transaction setAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public Transaction setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getNumber() {
        return number;
    }

    public Transaction setNumber(String number) {
        this.number = number;
        return this;
    }

    public Region getRegion() {
        return region;
    }

    public Transaction setRegion(Region region) {
        this.region = region;
        return this;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Transaction setDate(LocalDateTime date) {
        this.date = date;
        return this;
    }

    public TransactionStatus getResult() {
        return result;
    }

    public Transaction setResult(TransactionStatus result) {
        this.result = result;
        return this;
    }

    public TransactionStatus getFeedback() {
        return feedback;
    }

    public Transaction setFeedback(TransactionStatus feedback) {
        this.feedback = feedback;
        return this;
    }
}
