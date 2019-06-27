public enum Crossover {
    TWO_POINT {
        @Override
        public double[] config() {
            return new double[]{0.4, 0.9};
        }
    },
    UNIFORM,
    AVERAGE,
    SINGLE_POINT {
        @Override
        public double[] config() {
            return new double[]{0.4};
        }
    };

    public double[] config() {
        return null;
    }
}
