package dev.stiebo.app.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "suspiciousip")
public class SuspiciousIp extends AbstractEntity{
    @Column(unique = true)
    private String ip;

    public String getIp() {
        return ip;
    }

    public SuspiciousIp setIp(String ip) {
        this.ip = ip;
        return this;
    }
}
