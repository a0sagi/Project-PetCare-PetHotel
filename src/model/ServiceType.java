package model;

import java.math.BigDecimal;

public enum ServiceType {
    GROOMING("Grooming", new BigDecimal("75000"), false),
    BOARDING("Boarding", BigDecimal.ZERO, true),
    FULL_SERVICE("Full Service", new BigDecimal("75000"), true);

    private final String label;
    private final BigDecimal groomingFee;
    private final boolean includesBoarding;

    ServiceType(String label, BigDecimal groomingFee, boolean includesBoarding) {
        this.label = label;
        this.groomingFee = groomingFee;
        this.includesBoarding = includesBoarding;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal calculateTotal(BigDecimal dailyRate, int durationDays) {
        BigDecimal boardingFee = includesBoarding
                ? dailyRate.multiply(BigDecimal.valueOf(durationDays))
                : BigDecimal.ZERO;
        return groomingFee.add(boardingFee);
    }

    public static ServiceType fromLabel(String label) {
        for (ServiceType serviceType : values()) {
            if (serviceType.label.equalsIgnoreCase(label)
                    || serviceType.name().equalsIgnoreCase(label)) {
                return serviceType;
            }
        }
        throw new IllegalArgumentException("Layanan tidak dikenal: " + label);
    }
}
