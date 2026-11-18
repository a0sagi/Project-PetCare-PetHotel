package model;

import java.math.BigDecimal;

public final class Kucing extends Hewan {
    public Kucing(String name) {
        this(name, "");
    }

    public Kucing(String name, String CatatanKhusus) {
        super(name, CatatanKhusus);
    }

    @Override
    public JenisHewan getJenisHewan() {
        return JenisHewan.KUCING;
    }

    @Override
    public BigDecimal getTarifHarian() {
        return new BigDecimal("50000");
    }
}
