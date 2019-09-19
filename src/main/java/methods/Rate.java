package methods;

public class Rate {
    public static double STEP(final double baseRate, final int iteration, final double gamma, final int stepSize) {
        return baseRate * Math.pow(gamma, Math.floor((double) iteration / stepSize));
    }

    public static double STEP(final double baseRate, final int iteration, final int stepSize) {
        return baseRate * Math.pow(0.9, Math.floor((double) iteration / stepSize));
    }

    public static double STEP(final double baseRate, final int iteration, final double gamma) {
        return baseRate * Math.pow(gamma, Math.floor((double) iteration / 100));
    }

    public static double EXP(final double baserate, final int iteration, final double gamma) {
        return baserate * Math.pow(gamma, iteration);
    }

    public static double INV(final double baserate, final int iteration, final double gamma, final int power) {
        return baserate * Math.pow(1 + gamma * iteration, -power);
    }

    public static double INV(final double baserate, final int iteration, final int power) {
        return baserate * Math.pow(1 + 0.001 * iteration, -power);
    }

    public static double INV(final double baserate, final int iteration, final double gamma) {
        return baserate * Math.pow(1 + gamma * iteration, -2);
    }

    public static double calc(final double baseRate, final int iteration, final RatePolicy ratePolicy) {
        switch (ratePolicy) {
            case FIXED:
                return Rate.FIXED(baseRate);
            case EXP:
                return Rate.EXP(baseRate, iteration);
            case INV:
                return Rate.INV(baseRate, iteration);
            case STEP:
                return Rate.STEP(baseRate, iteration);
        }
        return 0;
    }

    private static double FIXED(final double baserate) {
        return baserate;
    }

    private static double EXP(final double baserate, final int iteration) {
        return baserate * Math.pow(0.999, iteration);
    }

    private static double INV(final double baserate, final int iteration) {
        return baserate * Math.pow(1 + 0.001 * iteration, -2);
    }

    private static double STEP(final double baseRate, final int iteration) {
        return baseRate * Math.pow(0.9, Math.floor((double) iteration / 100));
    }


    public enum RatePolicy {
        FIXED, EXP, INV, STEP
    }
}
