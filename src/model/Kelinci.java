package model;

import java.math.BigDecimal;

public final class Kelinci extends Pet {
    public Kelinci(String name) {
        super(name);
    }

    @Override
    public PetType getPetType() {
        return PetType.KELINCI;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("40000");
    }
}
