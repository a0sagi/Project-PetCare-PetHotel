package model;

public enum BookingStatus {
    CHECK_IN("Check-In"),
    IN_TREATMENT("Sedang Perawatan"),
    READY_FOR_CHECKOUT("Siap Check-Out"),
    CHECKED_OUT("Selesai");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public BookingStatus next() {
        return switch (this) {
            case CHECK_IN -> IN_TREATMENT;
            case IN_TREATMENT -> READY_FOR_CHECKOUT;
            case READY_FOR_CHECKOUT, CHECKED_OUT ->
                    throw new IllegalStateException("Status tidak dapat dimajukan lagi.");
        };
    }
}
