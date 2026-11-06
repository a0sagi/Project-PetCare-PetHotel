package model;

import java.math.BigDecimal;

public final class Anjing extends Hewan {
    public Anjing(String name) {
        super(name);
    }

    @Override
    public PetType getType() {
        return PetType.ANJING;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("60000");
    }
}
