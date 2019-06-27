import java.util.ArrayList;
import java.util.stream.IntStream;

public enum Cost {
    MSE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            final double error = IntStream.range(0, output.size()).mapToDouble(i -> Math.pow(target[i] - output.get(i), 2)).sum();
            return error / output.size();
        }
    },
    CROSS_ENTROPY {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            final double error = IntStream.range(0, output.size()).mapToDouble(i -> -target[i] * Math.log(Math.max(output.get(i), 1e-15)) + (1 - target[i]) * Math.log(1 - Math.max(output.get(i), 1e-15))).sum();
            return error / output.size();
        }
    },
    BINARY {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            return IntStream.range(0, output.size()).map(i -> Math.round(target[i] * 2) != Math.round(output.get(i) * 2) ? 1 : 0).sum();
        }
    },
    MAE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            final double error = IntStream.range(0, output.size()).mapToDouble(i -> Math.abs(target[i] - output.get(i))).sum();
            return error / output.size();
        }
    },
    MAPE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            final double error = IntStream.range(0, output.size()).mapToDouble(i -> Math.abs((output.get(i) - target[i]) / Math.max(target[i], 1e-15))).sum();
            return error / output.size();
        }
    },
    MSLE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            return IntStream.range(0, output.size()).mapToDouble(i -> Math.log(Math.max(target[i], 1e-15)) - Math.log(Math.max(output.get(i), 1e-15))).sum();
        }
    },
    HINGE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            return IntStream.range(0, output.size()).mapToDouble(i -> Math.max(0, 1 - target[i] * output.get(i))).sum();
        }
    };

    public abstract double run(double[] target, ArrayList<Double> output);
}
