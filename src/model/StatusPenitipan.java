package model;

public enum StatusPenitipan {
    CHECK_IN("Check-In"),
    PERAWATAN("Sedang Dirawat"),
    SIAP_CHECKOUT("Siap Check-Out"),
    CHECK_OUT("Selesai");

    private final String label;

    StatusPenitipan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public StatusPenitipan next() {
        return switch (this) {
            case CHECK_IN -> PERAWATAN;
            case PERAWATAN -> SIAP_CHECKOUT;
            case SIAP_CHECKOUT -> CHECK_OUT;
            case CHECK_OUT -> this;
        };
    }
}
