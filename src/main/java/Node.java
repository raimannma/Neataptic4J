import java.util.ArrayList;
import java.util.List;

public class Node {
    public int index;
    ConnectionHistory connections;
    NodeType type;
    double mask;
    Activation squash;
    double bias;
    private int totalDeltaBias;
    private double errorGated;
    private double errorResponsibility;
    private double errorProjected;
    private double activation;
    private double state;
    private double old;
    private double derivative;
    private int previousDeltaBias;

    public Node(final NodeType type) {
        this.bias = (type == NodeType.INPUT) ? 0 : Math.random() * 0.2 - 0.1;
        this.squash = Activation.LOGISTIC;
        this.type = (type != NodeType.NULL) ? type : NodeType.HIDDEN;

        this.activation = 0;
        this.state = 0;
        this.old = 0;
        this.mask = 1;
        this.previousDeltaBias = 0;
        this.totalDeltaBias = 0;
        this.connections = new ConnectionHistory(this);

        this.errorResponsibility = 0;
        this.errorProjected = 0;
        this.errorGated = 0;

    }

    public Node() {
        this(NodeType.NULL);
    }

    public static Node fromJSON(final String json) {
        //TODO
        return null;
    }

    public double activate(final Double input) {

        if (input != null) {
            this.activation = input;
            return this.activation;
        }

        this.old = this.state;
        this.state = this.connections.self.gain * this.connections.self.weight * this.state + this.bias;
        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            this.state += connection.from.activation * connection.weight * connection.gain;
        }

        this.activation = this.squash.run(this.state, false) * this.mask;
        this.derivative = this.squash.run(this.state, true);

        final List<Node> nodes = new ArrayList<>();
        final List<Double> influences = new ArrayList<>();
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

            connection.elegibility = this.connections.self.gain * this.connections.self.weight *
                    connection.elegibility + connection.from.activation * connection.gain;

            for (int j = 0; j < nodes.size(); j++) {
                final Node node = nodes.get(j);
                final double influence = influences.get(j);

                final int index = connection.xTraceNodes.indexOf(node);
                if (index > -1) {
                    connection.xTraceValues.set(index, node.connections.self.gain * node.connections.self.weight *
                            connection.xTraceValues.get(index) + this.derivative * connection.elegibility * influence);
                } else {
                    connection.xTraceNodes.add(node);
                    connection.xTraceValues.add(this.derivative * connection.elegibility * influence);
                }
            }
        }
        return this.activation;
    }

    public double noTraceActivate(final Double input) {
        if (input != null) {
            this.activation = input;
            return this.activation;
        }
        this.state = this.connections.self.gain * this.connections.self.weight * this.state + this.bias;

        for (int i = 0; i < this.connections.in.size(); i++) {
            final Connection connection = this.connections.in.get(i);
            this.state += connection.from.activation * connection.weight * connection.gain;
        }

        this.activation = this.squash.run(this.state, false);

        for (int i = 0; i < this.connections.gated.size(); i++) {
            this.connections.gated.get(i).gain = this.activation;
        }
        return this.activation;
    }

    public void propagate(final double rate, final double momentum, final boolean update, final Double target) {
        double error = 0;

        if (this.type == NodeType.OUTPUT) {
            this.errorResponsibility = target - this.activation;
            this.errorProjected = target - this.activation;
        } else {
            for (int i = 0; i < this.connections.out.size(); i++) {
                final Connection connection = this.connections.out.get(i);
                final Node node = connection.to;
                error += node.errorResponsibility * connection.weight * connection.gain;
            }

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
            this.errorResponsibility = this.errorProjected + this.errorGated;
            if (this.type == NodeType.CONSTANT) {
                return;
            }

            for (int i = 0; i < this.connections.in.size(); i++) {
                final Connection connection = this.connections.in.get(i);
                double gradient = this.errorProjected * connection.elegibility;
                for (int j = 0; j < connection.xTraceNodes.size(); j++) {
                    final Node node = connection.xTraceNodes.get(j);
                    final double value = connection.xTraceValues.get(j);
                    gradient += node.errorResponsibility * value;
                }

                final double deltaWeight = rate * gradient * this.mask;
                connection.totalDeltaWeight += deltaWeight;
                if (update) {
                    connection.totalDeltaWeight += momentum * connection.previousDeltaWeight;
                    connection.weight += connection.totalDeltaWeight;
                    connection.previousDeltaWeight = connection.totalDeltaWeight;
                    connection.totalDeltaWeight = 0;
                }
            }
            final double deltaBias = rate * this.errorResponsibility;
            this.totalDeltaBias += deltaBias;
            if (update) {
                this.totalDeltaBias += momentum * this.previousDeltaBias;
                this.bias += this.totalDeltaBias;
                this.previousDeltaBias = this.totalDeltaBias;
                this.totalDeltaBias = 0;
            }
        }
    }

    public List<Connection> connect(final Node target) {
        return this.connect(target, 1.0);
    }

    public List<Connection> connect(final NodeGroup target) {
        return this.connect(target, 1.0);
    }

    public List<Connection> connect(final NodeGroup target, final Double weight) {
        final List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < target.nodes.size(); i++) {
            final Connection connection;
            if (weight == null) {
                connection = new Connection(this, target.nodes.get(i));
            } else {
                connection = new Connection(this, target.nodes.get(i), weight);
            }
            target.nodes.get(i).connections.in.add(connection);
            this.connections.out.add(connection);
            target.connections.in.add(connection);
        }
        return connections;
    }

    public List<Connection> connect(final Node target, final Double weight) {
        final List<Connection> connections = new ArrayList<>();
        if (target.equals(this)) {
            if (this.connections.self.weight != 0) {
                throw new RuntimeException("This connection already exists!");
            } else {
                this.connections.self.weight = weight != null ? weight : 1;
            }
            connections.add(this.connections.self);
        } else if (this.isProjectingTo(target)) {
            throw new RuntimeException("Already projecting a connection to this node!");
        } else {
            final Connection connection;
            if (weight == null) {
                connection = new Connection(this, target);
            } else {
                connection = new Connection(this, target, weight);
            }
            target.connections.in.add(connection);
            this.connections.out.add(connection);

            connections.add(connection);
        }
        return connections;
    }

    public void disconnect(final Node node) {
        this.disconnect(node, false);
    }

    public void disconnect(final Node node, final boolean twoSided) {
        if (node.equals(this)) {
            this.connections.self.weight = 0;
            return;
        }
        for (int i = 0; i < this.connections.out.size(); i++) {
            final Connection connection = this.connections.out.get(i);
            if (connection.to.equals(node)) {
                this.connections.out.remove(i);
                connection.to.connections.in.remove(connection);
                if (connection.gater != null) {
                    connection.gater.ungate(connection);
                }
                break;
            }
        }
        if (twoSided) {
            node.disconnect(this, false);
        }
    }

    public void gate(final Connection connection) {
        this.connections.gated.add(connection);
        connection.gater = this;
    }

    public void gate(final Connection[] connections) {
        for (final Connection connection : connections) {
            this.connections.gated.add(connection);
            connection.gater = this;
        }
    }

    public void ungate(final Connection[] connections) {
        for (int i = connections.length - 1; i >= 0; i--) {
            final Connection connection = connections[i];
            this.connections.gated.remove(connection);
            connection.gater = null;
            connection.gain = 1;
        }
    }

    public void ungate(final Connection connection) {
        this.connections.gated.remove(connection);
        connection.gater = null;
        connection.gain = 1;
    }

    public void clear() {
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

    public void mutate(final Mutation method) {
        if (method == Mutation.NULL) {
            throw new RuntimeException("No mutation method given!");
        }
        switch (method) {
            case MOD_ACTIVATION:
                this.squash = method.allowed().get((int) (method.allowed().indexOf(this.squash) +
                        Math.floor(Math.random() * (method.allowed().size() - 1)) + 1) % method.allowed().size());
                break;
            case MOD_BIAS:
                this.bias += Math.random() * (method.max() - method.min()) + method.min();
                break;
            default:
                break;
        }
    }

    public boolean isProjectingTo(final Node node) {
        if (node.equals(this) && this.connections.self.weight != 0) {
            return true;
        }
        for (int i = 0; i < this.connections.out.size(); i++) {
            if (this.connections.out.get(i).to.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public boolean isProjectedBy(final Node node) {
        if (node.equals(this) && this.connections.self.weight != 0) {
            return true;
        }
        for (int i = 0; i < this.connections.in.size(); i++) {
            if (this.connections.in.get(i).from.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public String toJSON() {
        //TODO
        return "";
    }

    public Double activate() {
        return this.activate(null);
    }

    public Double noTraceActivate() {
        return this.noTraceActivate(null);
    }

    public void propagate(final double rate, final double momentum, final boolean update) {
        this.propagate(rate, momentum, update, null);
    }
}
