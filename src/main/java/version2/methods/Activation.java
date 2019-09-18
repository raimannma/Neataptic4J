package version2.methods;

public enum Activation {
    LOGISTIC {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double fx = 1 / (1 + Math.exp(-x));
            if (!derivate) {
                return fx;
            }
            return fx * (1 - fx);
        }
    }, TANH {
        @Override
        public double calc(final double x, final boolean derivate) {
            if (derivate) {
                return 1 - Math.pow(Math.tanh(x), 2);
            }
            return Math.tanh(x);
        }
    }, IDENTITY {
        @Override
        public double calc(final double x, final boolean derivate) {
            return derivate ? 1 : x;
        }
    }, STEP {
        @Override
        public double calc(final double x, final boolean derivate) {
            return derivate ? 0 : x > 0 ? 1 : 0;
        }
    }, RELU {
        @Override
        public double calc(final double x, final boolean derivate) {
            if (derivate) {
                return x > 0 ? 1 : 0;
            }
            return x > 0 ? x : 0;
        }
    }, SOFTSIGN {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double d = 1 + Math.abs(x);
            return derivate ? x / Math.pow(d, 2) : x / d;
        }
    }, SINUSOID {
        @Override
        public double calc(final double x, final boolean derivate) {
            return derivate ? Math.cos(x) : Math.sin(x);
        }
    }, GAUSSIAN {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double d = Math.exp(-Math.pow(x, 2));
            return derivate ? -2 * x * d : d;
        }
    }, BENT_IDENTITY {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double d = Math.sqrt(Math.pow(x, 2) + 1);
            if (derivate) {
                return x / (2 * d) + 1;
            }
            return (d - 1) / 2 + x;
        }
    }, BIPOLAR {
        @Override
        public double calc(final double x, final boolean derivate) {
            return derivate ? 0 : x > 0 ? 1 : -1;
        }
    }, BIPOLAR_SIGMOID {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double d = 2 / (1 + Math.exp(-x)) - 1;
            if (derivate) {
                return 0.5 * (1 + d) * (1 - d);
            }
            return d;
        }
    }, HARD_TANH {
        @Override
        public double calc(final double x, final boolean derivate) {
            if (derivate) {
                return x > -1 && x < 1 ? 1 : 0;
            }
            return Math.max(-1, Math.min(1, x));
        }
    }, ABSOLUTE {
        @Override
        public double calc(final double x, final boolean derivate) {
            if (derivate) {
                return x < 0 ? -1 : 1;
            }
            return Math.abs(x);
        }
    }, INVERSE {
        @Override
        public double calc(final double x, final boolean derivate) {
            return derivate ? -1 : 1 - x;
        }
    }, SELU {
        @Override
        public double calc(final double x, final boolean derivate) {
            final double alpha = 1.6732632423543772848170429916717;
            final double scale = 1.0507009873554804934193349852946;
            final double fx = x > 0 ? x : alpha * Math.exp(x) - alpha;
            return derivate ? x > 0 ? scale : (fx + alpha) * scale : fx * scale;
        }
    };

    public abstract double calc(double x, boolean derivate);
}
