package model;

public final class AnimalFactory {
    private AnimalFactory() {
    }

    public static Pet create(PetType petType, String petName) {
        return switch (petType) {
            case KUCING -> new Kucing(petName);
            case ANJING -> new Anjing(petName);
            case KELINCI -> new Kelinci(petName);
        };
    }
}
