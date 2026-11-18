package model;

import java.math.BigDecimal;

public final class Kelinci extends Hewan {
    public Kelinci(String name) {
        this(name, "");
    }

    public Kelinci(String name, String CatatanKhusus) {
        super(name, CatatanKhusus);
    }

    @Override
    public JenisHewan getJenisHewan() {
        return JenisHewan.KELINCI;
    }

    @Override
    public BigDecimal getTarifHarian() {
        return new BigDecimal("40000");
    }
}
