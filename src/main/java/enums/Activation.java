package enums;

public enum Activation {
    LOGISTIC {
        @Override
        public int run(final double state) {
            return 0;
        }

        @Override
        public double run(final double state, final boolean b) {
            return 0;
        }
    };

    public abstract int run(double state);

    public abstract double run(double state, boolean b);
}
