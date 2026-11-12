package model;

public enum PetType {
    KUCING("Kucing"),
    ANJING("Anjing"),
    KELINCI("Kelinci");

    private final String label;

    PetType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PetType fromLabel(String label) {
        for (PetType petType : values()) {
            if (petType.label.equalsIgnoreCase(label)
                    || petType.name().equalsIgnoreCase(label)) {
                return petType;
            }
        }
        throw new IllegalArgumentException("Jenis hewan tidak dikenal: " + label);
    }
}
