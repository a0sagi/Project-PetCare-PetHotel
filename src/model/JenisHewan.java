package model;

public enum JenisHewan {
    KUCING("Kucing"),
    ANJING("Anjing"),
    KELINCI("Kelinci");

    private final String label;

    JenisHewan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static JenisHewan fromLabel(String label) {
        for (JenisHewan jenis : values()) {
            if (jenis.label.equalsIgnoreCase(label)
                    || jenis.name().equalsIgnoreCase(label)) {
                return jenis;
            }
        }
        throw new IllegalArgumentException("Jenis hewan tidak dikenal: " + label);
    }
}
