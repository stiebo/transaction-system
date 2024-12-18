package dev.stiebo.app.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "suspiciousip")
public class SuspiciousIp extends AbstractEntity{
    private String ip;

    public String getIp() {
        return ip;
    }

    public SuspiciousIp setIp(String ip) {
        this.ip = ip;
        return this;
    }
}
