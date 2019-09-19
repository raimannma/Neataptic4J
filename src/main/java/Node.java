import com.google.gson.JsonObject;
import methods.Activation;
import methods.MutationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static Node.NodeType.CONSTANT;
import static methods.Activation.LOGISTIC;

public class Node {

    final ConnectionListNode connections;
    int index;
    Activation squash;
    double bias;
    NodeType type;
    double mask;
    private double prevDeltaBias;
    private double totalDeltaBias;
    private double old;
    private double state;
    private double activation;
    private double errorGated;
    private double errorResponsibility;
    private double errorProjected;
    private double derivative;

    public Node() {
        this(NodeType.HIDDEN);
    }

    public Node(final NodeType type) {
        this.bias = type == NodeType.INPUT ? 0 : Math.random() * 0.2 - 0.1;
        this.squash = LOGISTIC;
        this.type = type;

        this.activation = 0;
        this.state = 0;
        this.old = 0;

        this.mask = 1;

        this.prevDeltaBias = 0;

        this.totalDeltaBias = 0;

        this.connections = new ConnectionListNode(this);

        this.errorResponsibility = 0;
        this.errorProjected = 0;
        this.errorGated = 0;
    }

    static Node fromJSON(final JsonObject jsonObject) {
        final Node node = new Node();
        node.bias = jsonObject.get("bias").getAsDouble();
        node.type = NodeType.valueOf(jsonObject.get("type").getAsString());
        node.squash = Activation.valueOf(jsonObject.get("squash").getAsString());
        node.mask = jsonObject.get("mask").getAsDouble();
        return node;
    }

    double activate() {
        this.old = this.state;
        this.state = this.connections.self.gain * this.connections.self.weight * this.state + this.bias;
        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            this.state += connection.from.activation * connection.weight * connection.gain;
        }

        this.activation = this.squash.calc(this.state) * this.mask;
        this.derivative = this.squash.calc(this.state, true);

        final List<Node> nodes = new ArrayList<>();
        final ArrayList<Double> influences = IntStream.range(0, this.connections.gated.size()).mapToObj(i -> 0.0).collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < this.connections.gated.size(); i++) {
            final Connection connection = this.connections.gated.get(i);
            final Node node = connection.to;

            final int index = nodes.indexOf(node);
            if (index > -1) {
                influences.set(index, influences.get(index) + connection.weight * connection.from.activation);
            } else {
                nodes.add(node);
                influences.add(connection.weight * connection.from.activation +
                        (node.connections.self.gater.equals(this) ? node.old : 0));
            }
            connection.gain = this.activation;
        }

        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            connection.elegibility = this.connections.self.gain * this.connections.self.weight * connection.elegibility + connection.from.activation * connection.gain;

            for (int j = 0; j < nodes.size(); j++) {
                final Node node = nodes.get(i);
                final double influence = influences.get(i);
                final int index = connection.xTraceNodes.indexOf(node);
                if (index > -1) {
                    connection.xTraceValues.set(index,
                            node.connections.self.gain * node.connections.self.weight * connection.xTraceValues.get(index) +
                                    this.derivative * connection.elegibility * influence);
                } else {
                    connection.xTraceNodes.add(node);
                    connection.xTraceValues.add(this.derivative * connection.elegibility * influence);
                }
            }
        }
        return this.activation;
    }

    double activate(final double input) {
        this.activation = input;
        return this.activation;
    }

    double noTraceActivation() {
        this.state = this.connections.self.gain * this.connections.self.weight * this.state + this.bias;
        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            this.state += connection.from.activation * connection.weight * connection.gain;
        }
        this.activation = this.squash.calc(this.state);

        IntStream.range(0, this.connections.in.size()).forEach(i -> this.connections.gated.get(i).gain = this.activation);
        return this.activation;
    }

    double noTraceActivation(final double input) {
        this.activation = input;
        return this.activation;
    }

    void propagate(final double momentum, final boolean update, final double target) {
        this.propagate(0.3, momentum, update, target);
    }

    void propagate(final double rate, final double momentum, final boolean update, final double target) {
        double error;
        if (this.type == NodeType.OUTPUT) {
            this.errorResponsibility = target - this.activation;
            this.errorProjected = target - this.activation;
        } else {
            error = this.connections.out.stream().mapToDouble(connection -> connection.to.errorResponsibility * connection.weight * connection.gain).sum();

            this.errorProjected = this.derivative * error;

            error = 0;

            for (int i = 0; i < this.connections.gated.size(); i++) {
                final Connection connection = this.connections.gated.get(i);
                final Node node = connection.to;
                double influence = node.connections.self.gater.equals(this) ? node.old : 0;
                influence += connection.weight * connection.from.activation;
                error += node.errorResponsibility * influence;
            }

            this.errorGated = this.derivative * error;

            this.errorResponsibility = this.errorProjected * this.errorGated;
        }

        if (this.type == CONSTANT) {
            return;
        }
        this.connections.in.forEach(connection -> {
            final double gradient = this.errorProjected * connection.elegibility +
                    IntStream.range(0, connection.xTraceNodes.size())
                            .mapToDouble(j -> connection.xTraceNodes.get(j).errorResponsibility * connection.xTraceValues.get(j))
                            .sum();
            final double deltaWeight = rate * gradient * this.mask;
            connection.totalDeltaWeight += deltaWeight;
            if (update) {
                connection.totalDeltaWeight += momentum * connection.prevDeltaWeight;
                connection.weight += connection.totalDeltaWeight;
                connection.prevDeltaWeight = connection.totalDeltaWeight;
                connection.totalDeltaWeight = 0;
            }
        });

        final double deltaBias = rate * this.errorResponsibility;
        this.totalDeltaBias += deltaBias;
        if (update) {
            this.totalDeltaBias += momentum * this.prevDeltaBias;
            this.bias += this.totalDeltaBias;
            this.prevDeltaBias = this.totalDeltaBias;
            this.totalDeltaBias = 0;
        }
    }

    void propagate(final double rate, final double momentum, final boolean update) {
        this.propagate(rate, momentum, update, 0);
    }

    void propagate(final boolean update, final double target) {
        this.propagate(0.3, 0, update, target);
    }

    void propagate(final boolean update, final double target, final double rate) {
        this.propagate(rate, 0, update, target);
    }

    List<Connection> connect(final Node target) {
        return this.connect(target, 1);
    }

    List<Connection> connect(final Node target, final double weight) {
        final List<Connection> connections = new ArrayList<>();
        if (target.equals(this)) {
            if (this.connections.self.weight != 0) {
                throw new RuntimeException("This connection already exists!");
            } else {
                this.connections.self.weight = weight;
            }
            connections.add(this.connections.self);
        } else if (this.isProjectingTo(target)) {
            throw new RuntimeException("Already projecting a connection to this node!");
        } else {
            final Connection connection = new Connection(this, target, weight);
            target.connections.in.add(connection);
            this.connections.out.add(connection);
            connections.add(connection);
        }
        return connections;
    }

    boolean isProjectingTo(final Node node) {
        return node.equals(this) && this.connections.self.weight != 0 ||
                IntStream.range(0, this.connections.out.size()).anyMatch(i -> this.connections.out.get(i).to.equals(node));
    }

    private List<Connection> connect(final NodeGroup target, final double weight) {
        final List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < target.nodes.size(); i++) {
            final Connection connection = new Connection(this, target.nodes.get(i), weight);
            target.nodes.get(i).connections.in.add(connection);
            this.connections.out.add(connection);
            target.connections.in.add(connection);

            connections.add(connection);
        }
        return connections;
    }

    void disconnect(final Node node) {
        this.disconnect(node, false);
    }

    void clear() {
        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            connection.elegibility = 0;
            connection.xTraceNodes = new ArrayList<>();
            connection.xTraceValues = new ArrayList<>();
        }
        for (int i = 0; i < this.connections.gated.size(); i++) {
            this.connections.gated.get(i).gain = 0;
        }
        this.errorResponsibility = 0;
        this.errorProjected = 0;
        this.errorGated = 0;
        this.old = 0;
        this.state = 0;
        this.activation = 0;
    }

    void disconnect(final Node node, final boolean twoSided) {
        if (this.equals(node)) {
            this.connections.self.weight = 0;
            return;
        }
        for (final Connection connection : new ArrayList<>(this.connections.out)) {
            if (connection.to.equals(node)) {
                this.connections.out.remove(connection);
                connection.to.connections.in.remove(connection);
                if (connection.gater != null) {
                    connection.gater.ungate(connection);
                }
                break;
            }
        }
        if (twoSided) {
            node.disconnect(this);
        }
    }

    void ungate(final Connection connection) {
        this.ungate(new Connection[]{connection});
    }

    private void ungate(final Connection[] connections) {
        for (int i = connections.length - 1; i >= 0; i--) {
            final Connection connection = connections[i];
            this.connections.gated.remove(connection);
            connection.gater = null;
            connection.gain = 1;
        }
    }

    void gate(final Connection connection) {
        this.gate(new Connection[]{connection});
    }

    private void gate(final Connection[] connections) {
        Arrays.stream(connections).forEach(connection -> {
            this.connections.gated.add(connection);
            connection.gater = this;
        });
    }

    void mutate(final MutationType method) {
        switch (method) {
            case MOD_ACTIVATION:
                this.squash = method.allowed[(int) (Utils.indexOf(method.allowed, this.squash) + Math.floor(Math.random() * (method.allowed.length - 1)) + 1) % method.allowed.length];
                break;
            case MOD_BIAS:
                this.bias += Math.random() * (method.max - method.min) + method.min;
                break;
        }
    }

    boolean isProjectedBy(final Node node) {
        return node.equals(this) && this.connections.self.weight != 0 ||
                this.connections.in.stream().anyMatch(connection -> connection.from.equals(node));
    }

    JsonObject toJSON() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bias", this.bias);
        jsonObject.addProperty("type", this.type.name());
        jsonObject.addProperty("squash", this.squash.name());
        jsonObject.addProperty("mask", this.mask);
        return jsonObject;
    }

    enum NodeType {HIDDEN, INPUT, OUTPUT, CONSTANT}

    static class NodeValues {
        Double bias = null;
        Activation squash = null;
        NodeType type = null;

        NodeValues setBias(final double bias) {
            this.bias = bias;
            return this;
        }

        NodeValues setSquash(final Activation squash) {
            this.squash = squash;
            return this;
        }

        NodeValues setType(final NodeType type) {
            this.type = type;
            return this;
        }
    }
}
