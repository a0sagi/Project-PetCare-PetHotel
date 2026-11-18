package model;

public enum StatusPenitipan {
    CHECK_IN("Check-In"),
    PERAWATAN("Sedang Perawatan"),
    CHECK_OUT("Check-Out"),
    DIBATALKAN("Dibatalkan");

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
            case PERAWATAN -> CHECK_OUT;
            case CHECK_OUT, DIBATALKAN -> this;
        };
    }
}
