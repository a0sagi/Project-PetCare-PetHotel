package model;

public final class HewanFactory {
    private HewanFactory() {
    }

    public static Hewan create(JenisHewan jenisHewan, String namaHewan) {
        return create(jenisHewan, namaHewan, "");
    }

    public static Hewan create(JenisHewan jenisHewan, String namaHewan, String catatanKhusus    ) {
        return switch (jenisHewan) {
            case KUCING -> new Kucing(namaHewan, catatanKhusus);
            case ANJING -> new Anjing(namaHewan, catatanKhusus);
            case KELINCI -> new Kelinci(namaHewan, catatanKhusus);
        };
    }
}
