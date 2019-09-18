package version1;

public enum Rate {
    FIXED {
        @Override
        public double getRate(final double baseRate, final int iteration) {
            return baseRate;
        }
    }, STEP {
        public final int stepSize = 100;
        public final double gamma = 0.9;

        @Override
        public double getRate(final double baseRate, final int iteration) {
            return baseRate * Math.pow(this.gamma, (Math.floor(iteration / this.stepSize)));
        }
    }, EXP {
        public final double gamma = 0.999;

        @Override
        public double getRate(final double baseRate, final int iteration) {
            return baseRate * Math.pow(this.gamma, iteration);
        }
    }, INV {
        public final double gamma = 0.001;
        public final int power = 2;

        @Override
        public double getRate(final double baseRate, final int iteration) {
            return baseRate * Math.pow(1 + this.gamma * iteration, -this.power);
        }
    };

    public abstract double getRate(double baseRate, int iteration);
}
