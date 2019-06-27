import enums.Activation;
import enums.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class NodeGroup {
    public ArrayList<Node> nodes;
    public ConnectionHistory connections;

    public NodeGroup(final int size) {
        this.nodes = new ArrayList<>();
        this.connections = new ConnectionHistory();

        IntStream.range(0, size).forEach(i -> this.nodes.add(new Node()));
    }

    public ArrayList<Double> activate(final double[] value) {
        final ArrayList<Double> values = new ArrayList<>();
        if (value != null && value.length != this.nodes.size()) {
            throw new RuntimeException("Array with values should be same as the amount of nodes!");
        }
        for (int i = 0; i < this.nodes.size(); i++) {
            final double activation;
            if (value == null) {
                activation = this.nodes.get(i).activate();
            } else {
                activation = this.nodes.get(i).activate(value[i]);
            }
            values.add(activation);
        }
        return values;
    }

    public void propagate(final double rate, final double momentum, final double[] target) {
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

    public ArrayList<Connection> connect(final Layer target, final Connection.Method method, final Double weight) {
        return target.input(this, method, weight);
    }

    public ArrayList<Connection> connect(final Node target, final Connection.Method method, final Double weight) {
        final ArrayList<Connection> connections = new ArrayList<>();
        for (final Node node : this.nodes) {
            final List<Connection> connection = node.connect(target, weight);
            this.connections.out.add(connection.get(0));
            connections.add(connection.get(0));
        }
        return connections;
    }


    public ArrayList<Connection> connect(final NodeGroup target, Connection.Method method, final Double weight) {
        final ArrayList<Connection> connections = new ArrayList<>();
        if (method == null) {
            if (!this.equals(target)) {
                System.out.println("No group connection specified, using ALL_TO_ALL");
                method = Connection.Method.ALL_TO_ALL;
            } else {
                System.out.println("No group connection specified, using ONE_TO_ONE");
                method = Connection.Method.ONE_TO_ONE;
            }
        }
        if (method == Connection.Method.ALL_TO_ALL || method == Connection.Method.ALL_TO_ELSE) {
            for (int i = 0; i < this.nodes.size(); i++) {
                for (int j = 0; j < target.nodes.size(); j++) {
                    if (method == Connection.Method.ALL_TO_ELSE && this.nodes.get(i) == target.nodes.get(j)) {
                        continue;
                    }
                    final List<Connection> connection = this.nodes.get(i).connect(target.nodes.get(j), weight);
                    this.connections.out.add(connection.get(0));
                    target.connections.in.add(connection.get(0));
                    connections.add(connection.get(0));
                }
            }
        } else if (method == Connection.Method.ONE_TO_ONE) {
            if (this.nodes.size() != target.nodes.size()) {
                throw new RuntimeException("From and To group must be the same size!");
            }

            for (int i = 0; i < this.nodes.size(); i++) {
                final List<Connection> connection = this.nodes.get(i).connect(target.nodes.get(i), weight);
                this.connections.selfArr.add(connection.get(0));
                connections.add(connection.get(0));
            }
        }
        return connections;
    }

    public void gate(final List<Connection> connections, final Connection.Gating method) {
        if (method == null) {
            throw new RuntimeException("Please specify Gating.INPUT, Gating.OUTPUT");
        }
        final List<Node> nodes1 = new ArrayList<>();
        final ArrayList<Node> nodes2 = new ArrayList<>();
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
                    final Node node = nodes2.get(i);
                    final Node gater = this.nodes.get(i % this.nodes.size());
                    for (int j = 0; j < node.connections.in.size(); j++) {
                        final Connection conn = node.connections.in.get(j);
                        if (connections.contains(conn)) {
                            gater.gate(conn);
                        }
                    }
                }
                break;
            case OUTPUT:
                for (int i = 0; i < nodes1.size(); i++) {
                    final Node node = nodes1.get(i);
                    final Node gater = this.nodes.get(i % this.nodes.size());

                    for (int j = 0; j < node.connections.out.size(); j++) {
                        final Connection conn = node.connections.out.get(j);
                        if (connections.contains(conn)) {
                            gater.gate(conn);
                        }
                    }
                }
                break;
            case SELF:
                for (int i = 0; i < nodes1.size(); i++) {
                    final Node node = nodes1.get(i);
                    final Node gater = this.nodes.get(i % this.nodes.size());

                    if (connections.contains(node.connections.self)) {
                        gater.gate(node.connections.self);
                    }
                }
                break;
        }
    }

    public void set(final Double bias, final Activation squash, final NodeType type) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (bias != null) {
                this.nodes.get(i).bias = bias;
            }

            this.nodes.get(i).squash = squash == null ? this.nodes.get(i).squash : squash;
            this.nodes.get(i).type = type == null ? this.nodes.get(i).type : type;
        }
    }

    public void clear() {
        for (final Node node : this.nodes) {
            node.clear();
        }
    }

    public void disconnect(final Node target, final Boolean twosided) {
        for (int i = 0; i < this.nodes.size(); i++) {
            this.nodes.get(i).disconnect(target, twosided);
            for (int j = this.connections.out.size() - 1; j >= 0; j--) {
                final Connection conn = this.connections.out.get(j);

                if (conn.from.equals(this.nodes.get(i)) && conn.to.equals(target)) {
                    this.connections.out.remove(j);
                    break;
                }
            }
        }
    }

    public void disconnect(final NodeGroup target, Boolean twosided) {
        twosided = twosided != null ? twosided : false;
        for (int i = 0; i < this.nodes.size(); i++) {
            for (int j = 0; j < target.nodes.size(); j++) {
                this.nodes.get(i).disconnect(target.nodes.get(j), twosided);

                for (int k = this.connections.out.size() - 1; k >= 0; k--) {
                    final Connection conn = this.connections.out.get(k);
                    if (conn.from.equals(this.nodes.get(i)) && conn.to.equals(target.nodes.get(j))) {
                        this.connections.out.remove(k);
                        break;
                    }
                }
                if (twosided) {
                    for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                        final Connection conn = this.connections.in.get(k);

                        if (conn.from.equals(target.nodes.get(j)) && conn.to.equals(this.nodes.get(i))) {
                            this.connections.in.remove(k);
                            break;
                        }
                    }
                }
            }
        }
    }
}
