package enums;

import java.util.ArrayList;

public enum Mutation {
    NULL, MOD_ACTIVATION, MOD_BIAS, SUB_NODE;
    public ArrayList<Activation> allowed;
    public double max, min;
    public boolean keepGates;
}
