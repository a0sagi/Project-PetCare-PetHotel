package model;

import java.math.BigDecimal;
import java.util.Objects;

public class PetBooking {
    private final int idBooking;
    private final String namaPemilik;
    private final String nomorTelpon;
    private final Hewan hewan;
    private final JenisLayanan jenisLayanan;
    private final int durationDays;
    private StatusPenitipan status;
    private final BigDecimal tarifHarian;
    private final BigDecimal serviceFee;
    private BigDecimal totalBill;

    public PetBooking(int idBooking, String namaPemilik, String nomorTelpon,
                     Hewan hewan, JenisLayanan jenisLayanan, int durationDays) {
        this(idBooking, namaPemilik, nomorTelpon, hewan, jenisLayanan, durationDays,
                StatusPenitipan.CHECK_IN);
    }

    public PetBooking(int idBooking, String namaPemilik, String nomorTelpon,
                     Hewan hewan, JenisLayanan jenisLayanan, int durationDays, StatusPenitipan status) {
        if (idBooking < 1) {
            throw new IllegalArgumentException("ID booking harus lebih besar dari 0.");
        }
        this.idBooking = idBooking;
        this.namaPemilik = requireText(namaPemilik, "Nama pemilik");
        this.nomorTelpon = requireText(nomorTelpon, "Nomor telepon");
        this.hewan = Objects.requireNonNull(hewan, "Hewan wajib dipilih.");
        this.jenisLayanan = Objects.requireNonNull(jenisLayanan, "Layanan wajib dipilih.");
        if (durationDays < 1) {
            throw new IllegalArgumentException("Durasi minimal 1 hari.");
        }
        this.durationDays = durationDays;
        this.status = Objects.requireNonNull(status, "Status booking wajib diisi.");
        this.tarifHarian = hewan.getTarifHarian();
        this.serviceFee = jenisLayanan == JenisLayanan.BOARDING
                ? BigDecimal.ZERO : new BigDecimal("75000");
        this.totalBill = calculateTotalBill();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " wajib diisi.");
        }
        return value.trim();
    }

    public BigDecimal calculateTotalBill() {
        return jenisLayanan.calculateTotal(tarifHarian, durationDays);
    }

    /**
     * Menyelesaikan booking. Harus berstatus PERAWATAN sebelum checkout dilakukan.
     */
    public void markCheckedOut() {
        if (status != StatusPenitipan.PERAWATAN) {
            throw new IllegalStateException(
                    "Booking harus berstatus Sedang Perawatan sebelum checkout.");
        }
        totalBill = calculateTotalBill();
        status = StatusPenitipan.CHECK_OUT;
    }

    public int getIdBooking()           { return idBooking; }
    public String getNamaPemilik()      { return namaPemilik; }
    public String getNomorTelpon()      { return nomorTelpon; }
    public Hewan getHewan()             { return hewan; }
    public String getNamaHewan()        { return hewan.getName(); }
    public JenisHewan getJenisHewan()   { return hewan.getJenisHewan(); }
    public String getCatatanKhusus()     { return hewan.getCatatanKhusus(); }
    public JenisLayanan getJenisLayanan() { return jenisLayanan; }
    public int getDurasiHari()          { return durationDays; }
    public StatusPenitipan getStatus()  { return status; }
    public BigDecimal getTarifHarian()  { return tarifHarian; }
    public BigDecimal getServiceFee()   { return serviceFee; }
    public BigDecimal getTotalBill()    { return totalBill; }

    public void setStatus(StatusPenitipan status) {
        this.status = Objects.requireNonNull(status, "Status booking wajib diisi.");
    }

    public void setTotalBill(BigDecimal totalBill) {
        this.totalBill = Objects.requireNonNull(totalBill, "Total tagihan wajib diisi.");
    }
}
