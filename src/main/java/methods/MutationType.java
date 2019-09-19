package methods;

import static methods.Activation.*;

public enum MutationType {
    ADD_NODE("ADD_NODE"),
    SUB_NODE("SUB_NODE", true),
    ADD_CONN("ADD_CONN"),
    SUB_CONN("REMOVE_CONN"),
    MOD_WEIGHT("MOD_WEIGHT", -1, 1),
    MOD_BIAS("MOD_BIAS", -1, 1),
    MOD_ACTIVATION("MOD_ACTIVATION", true, new Activation[]{
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
    ADD_SELF_CONN("ADD_SELF_CONN"),
    SUB_SELF_CONN("SUB_SELF_CONN"),
    ADD_GATE("ADD_GATE"),
    SUB_GATE("SUB_GATE"),
    ADD_BACK_CONN("ADD_BACK_CONN"),
    SUB_BACK_CONN("SUB_BACK_CONN"),
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

    private final String name;
    public Activation[] allowed;
    public int min;
    public int max;
    public boolean keepGates;
    public boolean mutateOutput;

    MutationType(final String name) {
        this.name = name;
    }

    MutationType(final String name, final int min, final int max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    MutationType(final String name, final boolean keepGates) {
        this.name = name;
        if (name.equals("SWAP_NODES")) {
            this.mutateOutput = keepGates;
        } else {
            this.keepGates = keepGates;
        }
    }

    MutationType(final String name, final boolean mutateOutput, final Activation[] allowed) {
        this.name = name;
        this.mutateOutput = mutateOutput;
        this.allowed = allowed;
    }
}
