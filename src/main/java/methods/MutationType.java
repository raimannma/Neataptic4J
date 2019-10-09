package methods;

import static methods.Activation.*;

public enum MutationType {
    ADD_NODE(),
    SUB_NODE("SUB_NODE", true),
    ADD_CONN(),
    SUB_CONN(),
    MOD_WEIGHT(-1, 1),
    MOD_BIAS(-1, 1),
    MOD_ACTIVATION(true, new Activation[]{
            LOGISTIC,
            TANH,
            RELU,
            IDENTITY,
            STEP,
            SOFTSIGN,
            SINUSOID,
            GAUSSIAN,
            BENT_IDENTITY,
            BIPOLAR,
            BIPOLAR_SIGMOID,
            HARD_TANH,
            ABSOLUTE,
            INVERSE,
            SELU
    }),
    ADD_SELF_CONN(),
    SUB_SELF_CONN(),
    ADD_GATE(),
    SUB_GATE(),
    ADD_BACK_CONN(),
    SUB_BACK_CONN(),
    SWAP_NODES("SWAP_NODES", true);

    public static final MutationType[] ALL = new MutationType[]{
            ADD_NODE,
            SUB_NODE,
            ADD_CONN,
            SUB_CONN,
            MOD_WEIGHT,
            MOD_BIAS,
            MOD_ACTIVATION,
            ADD_GATE,
            SUB_GATE,
            ADD_SELF_CONN,
            SUB_SELF_CONN,
            ADD_BACK_CONN,
            SUB_BACK_CONN,
            SWAP_NODES
    };
    public static final MutationType[] FFW = new MutationType[]{
            ADD_NODE,
            SUB_NODE,
            ADD_CONN,
            SUB_CONN,
            MOD_WEIGHT,
            MOD_BIAS,
            MOD_ACTIVATION,
            SWAP_NODES
    };

    public Activation[] allowed;
    public int min;
    public int max;
    public boolean keepGates;
    public boolean mutateOutput;

    MutationType() {
    }

    MutationType(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    MutationType(final String name, final boolean keepGates) {
        if (name.equals("SWAP_NODES")) {
            this.mutateOutput = keepGates;
        } else {
            this.keepGates = keepGates;
        }
    }

    MutationType(final boolean mutateOutput, final Activation[] allowed) {
        this.mutateOutput = mutateOutput;
        this.allowed = allowed;
    }
}
