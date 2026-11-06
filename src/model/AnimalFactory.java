package model;

public final class AnimalFactory {
    private AnimalFactory() {
    }

    public static Hewan create(PetType type, String name) {
        return switch (type) {
            case KUCING -> new Kucing(name);
            case ANJING -> new Anjing(name);
            case KELINCI -> new Kelinci(name);
        };
    }
}
