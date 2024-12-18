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

    public static boolean luhnCheck(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }
        int n = number.length();
        int total = 0;
        boolean even = true;
        // iterate from right to left, double every 'even' value
        for (int i = n - 2; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (digit < 0 || digit > 9) {
                // value may only contain digits
                return false;
            }
            if (even) {
                digit <<= 1; // double value
            }
            even = !even;
            total += digit > 9 ? digit - 9 : digit;
        }
        int checksum = number.charAt(n - 1) - '0';
        return (total + checksum) % 10 == 0;
    }

}
