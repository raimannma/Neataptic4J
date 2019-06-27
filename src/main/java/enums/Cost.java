package enums;

import java.util.ArrayList;

public enum Cost {
    MSE {
        @Override
        public double run(final double[] target, final ArrayList<Double> output) {
            return 0;
        }
    };

    public abstract double run(double[] target, ArrayList<Double> output);
}
