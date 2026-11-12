package model;

import java.math.BigDecimal;

public final class Kucing extends Pet {
    public Kucing(String name) {
        super(name);
    }

    @Override
    public PetType getPetType() {
        return PetType.KUCING;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("50000");
    }
}
