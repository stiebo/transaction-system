package dev.stiebo.app.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.validator.constraints.LuhnCheck;

@Entity
@Table(name = "stolencard")
public class StolenCard extends AbstractEntity{
    @LuhnCheck
    private String number;

    public String getNumber() {
        return number;
    }

    public StolenCard setNumber(String number) {
        this.number = number;
        return this;
    }
}
