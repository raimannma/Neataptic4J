package methods;

public abstract class Rate {
    public Rate() {

    }

    public abstract double calc(double baseRate, int iterations);

    public static class FIXED extends Rate {
        @Override
        public double calc(final double baseRate, final int iterations) {
            return baseRate;
        }
    }

    public static class STEP extends Rate {

        private final int stepSize;
        private final double gamma;

        public STEP(final int stepSize) {
            this(stepSize, 0.9);
        }

        public STEP(final int stepSize, final double gamma) {
            this.stepSize = stepSize;
            this.gamma = gamma;
        }

        public STEP(final double gamma) {
            this(100, gamma);
        }

        public STEP() {
            this(100, 0.9);
        }

        @Override
        public double calc(final double baseRate, final int iterations) {
            return baseRate * Math.pow(this.gamma, Math.floor((double) iterations / this.stepSize));
        }
    }

    public static class EXP extends Rate {

        private final double gamma;

        public EXP() {
            this(0.999);
        }

        public EXP(final double gamma) {
            this.gamma = gamma;
        }

        @Override
        public double calc(final double baseRate, final int iterations) {
            return baseRate * Math.pow(this.gamma, iterations);
        }
    }

    public static class INV extends Rate {

        private final double gamma;
        private final double power;

        public INV() {
            this(0.001, 2);
        }

        public INV(final double gamma, final double power) {
            this.gamma = gamma;
            this.power = power;
        }

        public INV(final double gamma) {
            this(gamma, 2);
        }

        @Override
        public double calc(final double baseRate, final int iterations) {
            return baseRate * Math.pow(1 + this.gamma * iterations, -this.power);
        }
    }
}
