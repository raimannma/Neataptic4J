package enums;

public enum Rate {
    FIXED {
        @Override
        public double getRate(final double baseRate, final int iteration) {
            return baseRate;
        }
    };

    public abstract double getRate(double baseRate, int iteration);
}
