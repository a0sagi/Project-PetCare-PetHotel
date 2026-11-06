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
        for (PetType type : values()) {
            if (type.label.equalsIgnoreCase(label) || type.name().equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Jenis hewan tidak dikenal: " + label);
    }
}
