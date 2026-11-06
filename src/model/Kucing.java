package model;

import java.math.BigDecimal;

public final class Kucing extends Hewan {
    public Kucing(String name) {
        super(name);
    }

    @Override
    public PetType getType() {
        return PetType.KUCING;
    }

    @Override
    public BigDecimal getDailyRate() {
        return new BigDecimal("50000");
    }
}
