import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Connection {
    public double gain;
    public double weight;
    public Node from;
    public Node to;
    @Nullable
    public Node gater;
    public double elegibility;
    public ArrayList<Double> xTraceValues;
    public double previousDeltaWeight;
    public double totalDeltaWeight;
    ArrayList<Node> xTraceNodes;

    public Connection(final Node from, final Node to) {
        this(from, to, Math.random() * 0.2 - 0.1);
    }

    public Connection(final Node from, final Node to, final Double weight) {
        this.from = from;
        this.to = to;
        this.gain = 1;
        this.weight = weight;

        this.gater = null;
        this.elegibility = 0;
        this.previousDeltaWeight = 0;
        this.totalDeltaWeight = 0;

        this.xTraceNodes = new ArrayList<>();
        this.xTraceValues = new ArrayList<>();
    }

    public static int getInnovationID(final double a, final double b) {
        return (int) Math.round(0.5 * (a + b) * (a + b + 1) + b);
    }

    public String toJSON() {
        //TODO
        return "";
    }

    public enum Method {
        ONE_TO_ONE, ALL_TO_ELSE, ALL_TO_ALL
    }

    public enum Gating {OUTPUT, SELF, INPUT}
}
