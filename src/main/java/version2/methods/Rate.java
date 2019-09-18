package version2.methods;

class Rate {
    public static double FIXED(final double baserate) {
        return baserate;
    }

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

    public static double EXP(final double baserate, final int iteration) {
        return baserate * Math.pow(0.999, iteration);
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
}
