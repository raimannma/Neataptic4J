package version2.methods;

import java.util.stream.IntStream;

public enum Cost {
    CROSS_ENTROPY {
        @Override
        double calc(final double[] target, final double[] output) {
            final double error = IntStream.range(0, output.length)
                    .mapToDouble(i -> -(target[i] * Math.log(Math.max(output[i], 1e-15)) + (1 - target[i]) * Math.log(1 - Math.max(output[i], 1e-15))))
                    .sum();
            return error / output.length;
        }
    }, MSE {
        @Override
        double calc(final double[] target, final double[] output) {
            final double error = IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.pow(target[i] - output[i], 2))
                    .sum();
            return error / output.length;
        }
    }, BINARY {
        @Override
        double calc(final double[] target, final double[] output) {
            return IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.round(target[i] * 2) != Math.round(output[i] * 2) ? 1 : 0)
                    .sum();
        }
    }, MAE {
        @Override
        double calc(final double[] target, final double[] output) {
            final double error = IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.abs(target[i] - output[i]))
                    .sum();
            return error / output.length;
        }
    }, MAPE {
        @Override
        double calc(final double[] target, final double[] output) {
            final double error = IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.abs((output[i] - target[i]) / Math.max(target[i], 1e-15)))
                    .sum();
            return error / output.length;
        }
    }, MSLE {
        @Override
        double calc(final double[] target, final double[] output) {
            return IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.log(Math.max(target[i], 1e-15)) - Math.log(Math.max(output[i], 1e-15)))
                    .sum();
        }
    }, HINGE {
        @Override
        double calc(final double[] target, final double[] output) {
            return IntStream.range(0, output.length)
                    .mapToDouble(i -> Math.max(0, 1 - target[i] * output[i]))
                    .sum();
        }
    };

    abstract double calc(double[] target, double[] output);
}
