package dev.stiebo.app.data;

import dev.stiebo.app.configuration.Region;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction extends AbstractEntity {
    private Long amount;
    private String ip;
    private String number;
    @Enumerated(EnumType.STRING)
    private Region region;
    private LocalDateTime date;
    private String result;
    private String feedback;

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

    public String getResult() {
        return result;
    }

    public Transaction setResult(String result) {
        this.result = result;
        return this;
    }

    public String getFeedback() {
        return feedback;
    }

    public Transaction setFeedback(String feedback) {
        this.feedback = feedback;
        return this;
    }
}
