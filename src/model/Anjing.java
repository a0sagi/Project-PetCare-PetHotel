package model;

import java.math.BigDecimal;

public final class Anjing extends Pet {
    public Anjing(String name) {
        super(name);
    }

    @Override
    public PetType getPetType() {
        return PetType.ANJING;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("60000");
    }
}
