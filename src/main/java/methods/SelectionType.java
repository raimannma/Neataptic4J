package methods;

public enum SelectionType {
    FITNESS_PROPORTIONATE("FITNESS_PROPORTIONATE"),
    POWER("POWER", 4),
    TOURNAMENT("TOURNAMENT", 5, 0.5);

    private final String name;
    public int power;
    public int size;
    public double probability;

    SelectionType(final String name, final int size, final double probability) {
        this.name = name;
        this.size = size;
        this.probability = probability;
    }

    SelectionType(final String name) {
        this.name = name;
    }

    SelectionType(final String name, final int power) {
        this.name = name;
        this.power = power;
    }
}
