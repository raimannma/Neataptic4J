package architecture;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class Connection {
    final Node to;
    public double weight;
    Node from;
    double prevDeltaWeight;
    double totalDeltaWeight;
    Node gater;
    double gain;
    ArrayList<Node> xTraceNodes;
    ArrayList<Double> xTraceValues;
    double elegibility;

    Connection(final Node from, final Node to, final double weight) {
        this(from, to);
        this.weight = weight;
    }

    private Connection(final Node from, final Node to) {
        this.from = from;
        this.to = to;
        this.gain = 1;

        this.weight = Math.random() * 0.2 - 0.1;

        this.gater = null;
        this.elegibility = 0;
        this.prevDeltaWeight = 0;
        this.totalDeltaWeight = 0;

        this.xTraceNodes = new ArrayList<>();
        this.xTraceValues = new ArrayList<>();
    }

    static int getInnovationID(final int a, final int b) {
        return (int) Math.floor(0.5 * (a + b) * (a + b + 1) + b);
    }

    JsonObject toJSON() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("weight", this.weight);
        return jsonObject;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "to=" + this.to +
                ", weight=" + this.weight +
                ", from=" + this.from +
                '}';
    }
}
