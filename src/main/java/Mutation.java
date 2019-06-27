import java.util.ArrayList;

public enum Mutation {

    NULL, MOD_ACTIVATION {
        @Override
        public ArrayList<Activation> allowed() {
            return Utils.toList(new Activation[]{
                    Activation.LOGISTIC,
                    Activation.TANH,
                    Activation.RELU,
                    Activation.IDENTITY,
                    Activation.STEP,
                    Activation.SOFTSIGN,
                    Activation.SINUSOID,
                    Activation.GAUSSIAN,
                    Activation.BENT_IDENTITY,
                    Activation.BIPOLAR,
                    Activation.BIPOLAR_SIGMOID,
                    Activation.HARD_TANH,
                    Activation.ABSOLUTE,
                    Activation.INVERSE,
                    Activation.SELU
            });
        }

        @Override
        public boolean mutateOutput() {
            return true;
        }
    }, MOD_BIAS {
        @Override
        public double max() {
            return 1;
        }

        @Override
        public double min() {
            return -1;
        }
    }, SUB_NODE {
        @Override
        public boolean keepGates() {
            return true;
        }
    }, ADD_NODE, ADD_CONN, SUB_CONN, MOD_WEIGHT {
        @Override
        public double max() {
            return 1;
        }

        @Override
        public double min() {
            return -1;
        }
    }, ADD_SELF_CONN, SUB_SELF_CONN, ADD_GATE, SUB_GATE, ADD_BACK_CONN, SUB_BACK_CONN, SWAP_NODES {
        @Override
        public boolean mutateOutput() {
            return true;
        }
    };

    public static final Mutation[] FFW = new Mutation[]{
            Mutation.ADD_NODE,
            Mutation.SUB_NODE,
            Mutation.ADD_CONN,
            Mutation.SUB_CONN,
            Mutation.MOD_WEIGHT,
            Mutation.MOD_BIAS,
            Mutation.MOD_ACTIVATION,
            Mutation.SWAP_NODES
    };
    public static Mutation[] ALL = new Mutation[]{
            Mutation.ADD_NODE,
            Mutation.SUB_NODE,
            Mutation.ADD_CONN,
            Mutation.SUB_CONN,
            Mutation.MOD_WEIGHT,
            Mutation.MOD_BIAS,
            Mutation.MOD_ACTIVATION,
            Mutation.ADD_GATE,
            Mutation.SUB_GATE,
            Mutation.ADD_SELF_CONN,
            Mutation.SUB_SELF_CONN,
            Mutation.ADD_BACK_CONN,
            Mutation.SUB_BACK_CONN,
            Mutation.SWAP_NODES
    };

    public boolean keepGates() {
        return false;
    }

    public ArrayList<Activation> allowed() {
        return null;
    }

    public double max() {
        return 0;
    }

    public double min() {
        return 0;
    }

    public boolean mutateOutput() {
        return false;
    }
}
