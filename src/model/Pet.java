package model;

import java.math.BigDecimal;

public abstract class Pet {
    private final String name;

    protected Pet(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama hewan wajib diisi.");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    public abstract PetType getPetType();

    public abstract BigDecimal getDailyRate();
}
