package model;

import java.math.BigDecimal;

public final class Kelinci extends Hewan {
    public Kelinci(String name) {
        super(name);
    }

    @Override
    public PetType getType() {
        return PetType.KELINCI;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("40000");
    }
}
