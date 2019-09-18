package version1;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Connection {
    public Node to;
    public double gain;
    public double weight;
    public Node from;
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

    public static Connection fromJSON(final JsonObject json, final List<Node> nodes) {
        return new Connection(
                Node.fromJSON(json.get("from").getAsJsonObject()),
                Node.fromJSON(json.get("to").getAsJsonObject()),
                json.get("weight").getAsDouble());
    }

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();
        json.addProperty("weight", this.weight);
        json.add("from", this.from.toJSON());
        json.add("to", this.to.toJSON());
        return json;
    }

    public Connection copy() {
        final Connection copy = new Connection(this.from.copy(), this.to.copy(), this.weight);
        copy.gain = this.gain;
        if (this.gater != null) {
            copy.gater = this.gater.copy();
        }
        copy.elegibility = this.elegibility;
        copy.xTraceValues = new ArrayList<>(this.xTraceValues);
        copy.xTraceNodes = new ArrayList<>(this.xTraceNodes);
        copy.previousDeltaWeight = this.previousDeltaWeight;
        copy.totalDeltaWeight = this.totalDeltaWeight;
        return copy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Connection that = (Connection) o;
        return Double.compare(that.weight, this.weight) == 0 &&
                Objects.equals(this.to, that.to) &&
                Objects.equals(this.from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.to, this.weight, this.from);
    }

    @Override
    public String toString() {
        return "version1.Connection{" +
                "to=" + this.to +
                ", weight=" + this.weight +
                ", from=" + this.from +
                '}';
    }

    public enum Method {
        ONE_TO_ONE, ALL_TO_ELSE, ALL_TO_ALL
    }

    public enum Gating {OUTPUT, SELF, INPUT}
}
