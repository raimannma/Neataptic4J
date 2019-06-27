public enum Activation {
    LOGISTIC {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double fx = 1 / (1 + Math.exp(-x));
            return !derivate ? fx : fx * (1 - fx);
        }
    }, TANH {
        @Override
        public double run(final double x, final Boolean derivate) {
            if (derivate) {
                return 1 - Math.pow(Math.tanh(x), 2);
            } else {
                return Math.tanh(x);
            }
        }
    }, IDENTITY {
        @Override
        public double run(final double x, final Boolean derivate) {
            return derivate ? 1 : x;
        }
    }, INVERSE {
        @Override
        public double run(final double x, final Boolean derivate) {
            return derivate ? -1 : 1 - x;
        }
    }, STEP {
        @Override
        public double run(final double x, final Boolean derivate) {
            return derivate ? 0 : x > 0 ? 1 : 0;
        }
    }, RELU {
        @Override
        public double run(final double x, final Boolean derivate) {
            if (derivate) {
                return x > 0 ? 1 : 0;
            }
            return x > 0 ? x : 0;
        }
    }, SOFTSIGN {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double d = 1 + Math.abs(x);
            if (derivate) {
                return x / Math.pow(d, 2);
            }
            return x / d;
        }
    }, SINUSOID {
        @Override
        public double run(final double x, final Boolean derivate) {
            if (derivate) {
                return Math.cos(x);
            }
            return Math.sin(x);
        }
    }, GAUSSIAN {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double d = Math.exp(-Math.pow(x, 2));
            if (derivate) {
                return -2 * x * d;
            }
            return d;
        }
    }, BENT_IDENTITY {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double d = Math.sqrt(Math.pow(x, 2) + 1);
            if (derivate) {
                return x / (2 * d) + 1;
            }
            return (d - 1) / 2 + x;
        }
    }, BIPOLAR {
        @Override
        public double run(final double x, final Boolean derivate) {
            return derivate ? 0 : x > 0 ? 1 : -1;
        }
    }, BIPOLAR_SIGMOID {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double d = 2 / (1 + Math.exp(-x)) - 1;
            if (derivate) {
                return 0.5 * (1 + d) * (1 - d);
            }
            return d;
        }
    }, HARD_TANH {
        @Override
        public double run(final double x, final Boolean derivate) {
            if (derivate) {
                return x > -1 && x < 1 ? 1 : 0;
            }
            return Math.max(-1, Math.min(1, x));
        }
    }, ABSOLUTE {
        @Override
        public double run(final double x, final Boolean derivate) {
            if (derivate) {
                return x < 0 ? -1 : 1;
            }
            return Math.abs(x);
        }
    }, SELU {
        @Override
        public double run(final double x, final Boolean derivate) {
            final double alpha = 1.6732632423543772848170429916717;
            final double scale = 1.0507009873554804934193349852946;
            final double fx = x > 0 ? x : alpha * Math.exp(x) - alpha;
            if (derivate) {
                return x > 0 ? scale : (fx + alpha) * scale;
            }
            return fx * scale;
        }
    };

    public abstract double run(double x, Boolean derivate);
}
