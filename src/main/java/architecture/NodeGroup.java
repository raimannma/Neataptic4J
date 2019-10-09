package architecture;

import methods.ConnectionType;
import methods.GatingType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class NodeGroup {
    final ConnectionListGroup connections;
    final ArrayList<Node> nodes;

    NodeGroup(final int size) {
        this.nodes = new ArrayList<>();
        this.connections = new ConnectionListGroup();
        IntStream.range(0, size).mapToObj(i -> new Node()).forEach(this.nodes::add);
    }

    void gate(final List<Connection> connections, final GatingType method) {
        if (method == null) {
            throw new RuntimeException("Please specify Gating.INPUT, Gating.OUTPUT");
        }
        final List<Node> nodes1 = new ArrayList<>();
        final List<Node> nodes2 = new ArrayList<>();
        for (final Connection connection : connections) {
            if (!nodes1.contains(connection.from)) {
                nodes1.add(connection.from);
            }
            if (!nodes2.contains(connection.to)) {
                nodes2.add(connection.to);
            }
        }

        switch (method) {
            case INPUT:
                for (int i = 0; i < nodes2.size(); i++) {
                    final Node gater = this.nodes.get(i % this.nodes.size());
                    nodes2.get(i).connections.in.stream().filter(connections::contains).forEach(gater::gate);
                }
                break;
            case OUTPUT:
                for (int i = 0; i < nodes1.size(); i++) {
                    final Node gater = this.nodes.get(i % this.nodes.size());
                    nodes1.get(i).connections.out.stream().filter(connections::contains).forEach(gater::gate);
                }
                break;
            case SELF:
                IntStream.range(0, nodes1.size()).forEach(i -> {
                    final Node node = nodes1.get(i);
                    if (connections.contains(node.connections.self)) {
                        this.nodes.get(i % this.nodes.size()).gate(node.connections.self);
                    }
                });
                break;
        }
    }

    List<Connection> connect(final NodeGroup target, final ConnectionType method) {
        return this.connect(target, method, 0);
    }

    List<Connection> connect(final NodeGroup target, ConnectionType method, final double weight) {
        final List<Connection> connections = new ArrayList<>();
        if (method == null) {
            method = this.equals(target) ? ConnectionType.ONE_TO_ONE : ConnectionType.ALL_TO_ALL;
        }
        if (method == ConnectionType.ALL_TO_ALL || method == ConnectionType.ALL_TO_ELSE) {
            for (int i = 0; i < this.nodes.size(); i++) {
                for (int j = 0; j < target.nodes.size(); j++) {
                    if (method == ConnectionType.ALL_TO_ELSE && this.nodes.get(i).equals(target.nodes.get(j))) {
                        continue;
                    }
                    final List<Connection> connection = this.nodes.get(i).connect(target.nodes.get(j), weight);
                    this.connections.out.add(connection.get(0));
                    target.connections.in.add(connection.get(0));
                    connections.add(connection.get(0));
                }
            }
        } else if (method == ConnectionType.ONE_TO_ONE) {
            if (this.nodes.size() != target.nodes.size()) {
                throw new RuntimeException("From and To group must be the same size!");
            }
            for (int i = 0; i < this.nodes.size(); i++) {
                final Connection connection = this.nodes.get(i).connect(target.nodes.get(i), weight).get(0);
                this.connections.self.add(connection);
                connections.add(connection);
            }
        }

        return connections;
    }

    void disconnect(final Node target) {
        this.disconnect(target, false);
    }

    void disconnect(final Node target, final boolean twoSided) {
        for (final Node node : this.nodes) {
            node.disconnect(target, twoSided);

            for (int j = this.connections.out.size() - 1; j >= 0; j--) {
                final Connection connection = this.connections.out.get(j);

                if (connection.from.equals(node) && connection.to.equals(target)) {
                    this.connections.out.remove(j);
                    break;
                }
            }
            if (twoSided) {
                for (int j = this.connections.in.size() - 1; j >= 0; j--) {
                    final Connection connection = this.connections.in.get(j);

                    if (connection.from.equals(target) && connection.to.equals(node)) {
                        this.connections.in.remove(j);
                        break;
                    }
                }
            }
        }
    }

    double[] activate(final double[] value) {
        if (value != null && value.length != this.nodes.size()) {
            throw new RuntimeException("Array with values should be same as the amount of nodes!");
        }
        return IntStream.range(0, this.nodes.size())
                .mapToDouble(i -> value == null ? this.nodes.get(i).activate() : this.nodes.get(i).activate(value[i]))
                .toArray();
    }

    void propagate(final double rate, final double momentum, final double[] target) {
        if (target != null && target.length != this.nodes.size()) {
            throw new RuntimeException("Array with values should be same as the amount of nodes!");
        }

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            if (target == null) {
                this.nodes.get(i).propagate(rate, momentum, true);
            } else {
                this.nodes.get(i).propagate(rate, momentum, true, target[i]);
            }
        }
    }

    void connect(final Layer target, final ConnectionType method, final double weight) {
        target.input(this, method, weight);
    }

    List<Connection> connect(final Node target, final ConnectionType method, final double weight) {
        final List<Connection> connections = new ArrayList<>();
        this.nodes.stream().map(node -> node.connect(target, weight).get(0)).forEach(connection -> {
            this.connections.out.add(connection);
            connections.add(connection);
        });
        return connections;
    }

    void set(final Node.NodeValues values) {
        this.nodes.forEach(node -> {
            if (values.bias != null) {
                node.bias = values.bias;
            }
            node.squash = values.squash == null ? node.squash : values.squash;
            node.type = values.type == null ? node.type : values.type;
        });
    }

    void clear() {
        this.nodes.forEach(Node::clear);
    }
}
