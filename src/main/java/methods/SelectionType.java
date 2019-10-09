package methods;

public enum SelectionType {
    FITNESS_PROPORTIONATE(),
    POWER(4),
    TOURNAMENT(5, 0.5);

    public int power;
    public int size;
    public double probability;

    SelectionType(final int size, final double probability) {
        this.size = size;
        this.probability = probability;
    }

    SelectionType() {
    }

    SelectionType(final int power) {
        this.power = power;
    }
}
