package enums;

import java.util.ArrayList;

public enum Mutation {
    NULL, MOD_ACTIVATION, MOD_BIAS, SUB_NODE, ADD_NODE, ADD_CONN, SUB_CONN, MOD_WEIGHT, ADD_SELF_CONN, SUB_SELF_CONN, ADD_GATE, SUB_GATE, ADD_BACK_CONN, SUB_BACK_CONN, SWAP_NODES;
    public ArrayList<Activation> allowed;
    public double max, min;
    public boolean keepGates;
    public boolean mutateOutput;
}
