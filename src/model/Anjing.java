package model;

import java.math.BigDecimal;

public final class Anjing extends Hewan {
    public Anjing(String name) {
        this(name, "");
    }

    public Anjing(String name, String CatatanKhusus) {
        super(name, CatatanKhusus);
    }

    @Override
    public JenisHewan getJenisHewan() {
        return JenisHewan.ANJING;
    }

    @Override
    public BigDecimal getTarifHarian() {
        return new BigDecimal("60000");
    }
}
