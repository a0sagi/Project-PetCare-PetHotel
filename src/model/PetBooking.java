package model;

import java.math.BigDecimal;
import java.util.Objects;

public class PetBooking {
    private final int bookingId;
    private final String ownerName;
    private final int ownerPhone;
    private final Pet animal;
    private final ServiceType serviceType;
    private final int durationDays;
    private BookingStatus status;
    private final BigDecimal dailyRate;
    private final BigDecimal serviceFee;
    private BigDecimal totalBill;

    public PetBooking(int bookingId, String ownerName, int ownerPhone,
                          Pet animal, ServiceType serviceType, int durationDays) {
        this(bookingId, ownerName, ownerPhone, animal, serviceType, durationDays,
                BookingStatus.CHECK_IN);
    }

    public PetBooking(int bookingId, String ownerName, int ownerPhone,
                          Pet animal, ServiceType serviceType, int durationDays, BookingStatus status) {
        if (bookingId < 1) {
            throw new IllegalArgumentException("ID booking harus lebih besar dari 0.");
        }
        this.bookingId = bookingId;
        this.ownerName = requireText(ownerName, "Nama pemilik");
        if (ownerPhone < 1) {
            throw new IllegalArgumentException("Nomor telepon harus lebih besar dari 0.");
        }
        this.ownerPhone = ownerPhone;
        this.animal = Objects.requireNonNull(animal, "Hewan wajib dipilih.");
        this.serviceType = Objects.requireNonNull(serviceType, "Layanan wajib dipilih.");
        if (durationDays < 1) {
            throw new IllegalArgumentException("Durasi minimal 1 hari.");
        }
        this.durationDays = durationDays;
        this.status = Objects.requireNonNull(status, "Status booking wajib diisi.");
        this.dailyRate = animal.getDailyRate();
        this.serviceFee = serviceType == ServiceType.BOARDING
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
        return serviceType.calculateTotal(dailyRate, durationDays);
    }

    public void markCheckedOut() {
        if (status != BookingStatus.READY_FOR_CHECKOUT) {
            throw new IllegalStateException("Booking harus berstatus Siap Check-Out sebelum checkout.");
        }
        totalBill = calculateTotalBill();
        status = BookingStatus.CHECKED_OUT;
    }

    public int getBookingId() { return bookingId; }
    public String getOwnerName() { return ownerName; }
    public int getOwnerPhone() { return ownerPhone; }
    public Pet getAnimal() { return animal; }
    public String getPetName() { return animal.getName(); }
    public PetType getPetType() { return animal.getPetType(); }
    public ServiceType getServiceType() { return serviceType; }
    public int getDurationDays() { return durationDays; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public BigDecimal getServiceFee() { return serviceFee; }
    public BigDecimal getTotalBill() { return totalBill; }

    public void setStatus(BookingStatus status) {
        this.status = Objects.requireNonNull(status, "Status booking wajib diisi.");
    }

    public void setTotalBill(BigDecimal totalBill) {
        this.totalBill = Objects.requireNonNull(totalBill, "Total tagihan wajib diisi.");
    }
}
