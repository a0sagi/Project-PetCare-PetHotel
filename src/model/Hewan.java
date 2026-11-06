package model;

import java.math.BigDecimal;

public abstract class Hewan {
    private final String name;

    protected Hewan(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama hewan wajib diisi.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public abstract PetType getType();

    public abstract BigDecimal getDailyRate();
}
