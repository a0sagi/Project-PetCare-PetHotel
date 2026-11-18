package model;

import java.math.BigDecimal;

public abstract class Hewan {
    private final String name;
    private String CatatanKhusus;

    protected Hewan(String name) {
        this(name,  "");
    }

    protected Hewan(String name, String CatatanKhusus) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nama hewan wajib diisi.");
        }
        this.name = name.trim();
        this.CatatanKhusus = CatatanKhusus;
    }

    public String getName() {
        return name;
    }

    public String getCatatanKhusus() {
        return CatatanKhusus;
    }

    public abstract JenisHewan getJenisHewan();

    public abstract BigDecimal getTarifHarian();

    
}
