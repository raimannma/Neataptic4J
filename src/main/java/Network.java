import enums.Mutation;
import enums.NodeType;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private final ArrayList<Node> nodes;
    private final int dropout;
    private final ArrayList<Connection> connections;
    private final int input;
    private final int output;
    private final ArrayList<Connection> selfConnections;
    private final ArrayList<Connection> gates;

    public Network(final int input, final int output) {
        this.input = input;
        this.output = output;

        this.nodes = new ArrayList<>();
        this.connections = new ArrayList<>();

        this.gates = new ArrayList<>();
        this.selfConnections = new ArrayList<>();

        this.dropout = 0;

        for (int i = 0; i < input + output; i++) {
            final NodeType type = i < input ? NodeType.INPUT : NodeType.OUTPUT;
            this.nodes.add(new Node(type));
        }
        for (int i = 0; i < input; i++) {
            for (int j = 0; j < output + input; j++) {
                final double weight = Math.random() * input * Math.sqrt((double) 2 / input);
                this.connect(this.nodes.get(i), this.nodes.get(j), weight);
            }
        }
    }

    public ArrayList<Double> activate(final ArrayList<Double> input, final boolean training) {
        final ArrayList<Double> output = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == NodeType.INPUT) {
                this.nodes.get(i).activate(input.get(i));
            } else if (this.nodes.get(i).type == NodeType.OUTPUT) {
                output.add(this.nodes.get(i).activate());
            } else {
                if (training) {
                    this.nodes.get(i).mask = Math.random() < this.dropout ? 0 : 1;
                }
                this.nodes.get(i).activate();
            }
        }
        return output;
    }

    public ArrayList<Double> noTraceActivate(final ArrayList<Double> input) {
        final ArrayList<Double> output = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == NodeType.INPUT) {
                this.nodes.get(i).noTraceActivate(input.get(i));
            } else if (this.nodes.get(i).type == NodeType.OUTPUT) {
                output.add(this.nodes.get(i).noTraceActivate());
            } else {
                this.nodes.get(i).noTraceActivate();
            }
        }

        return output;
    }

    public void propagate(final double rate, final double momentum, final boolean update, final ArrayList<Double> target) {
        if (target == null || target.size() != this.output) {
            throw new RuntimeException("Output target length should match network output length!");
        }

        int targetIndex = target.size();
        for (int i = this.nodes.size() - 1; i >= this.nodes.size() - this.output; i--) {
            targetIndex--;
            this.nodes.get(i).propagate(rate, momentum, update, target.get(targetIndex));
        }
        for (int i = this.nodes.size() - this.output - 1; i >= this.input; i--) {
            this.nodes.get(i).propagate(rate, momentum, update);
        }
    }

    public void clear() {
        for (final Node node : this.nodes) {
            node.clear();
        }
    }

    public List<Connection> connect(final Node from, final Node to, final Double weight) {
        final List<Connection> connections = from.connect(to, weight);
        for (int i = 0; i < connections.size(); i++) {
            if (!from.equals(to)) {
                connections.add(connections.get(i));
            } else {
                this.selfConnections.add(connections.get(i));
            }
        }
        return connections;
    }

    public void disconnect(final Node from, final Node to) {
        final List<Connection> connections = from.equals(to) ? this.selfConnections : this.connections;

        for (int i = 0; i < connections.size(); i++) {
            final Connection connection = connections.get(i);
            if (connection.from.equals(from) && connection.to.equals(to)) {
                if (connection.gater != null) {
                    this.ungate(connection);
                }
                connections.remove(connection);
                break;
            }
        }
        from.disconnect(to);
    }

    public void gate(final Node node, final Connection connection) {
        if (this.nodes.indexOf(node) == -1) {
            throw new RuntimeException("This node is not part of the network!");
        } else if (connection.gater != null) {
            throw new RuntimeException("This connection is already gated!");
        }
        node.gate(connection);
        this.gates.add(connection);
    }

    public void ungate(final Connection connection) {
        final int index = this.gates.indexOf(connection);
        if (index == -1) {
            throw new RuntimeException("This connection is not gated!");
        }
        this.gates.remove(index);
        connection.gater.ungate(connection);
    }

    public void remove(final Node node) {
        final int index = this.nodes.indexOf(node);
        if (index == -1) {
            throw new RuntimeException("This node does not exist in the network!");
        }

        final ArrayList<Node> gaters = new ArrayList<>();

        this.disconnect(node, node);

        final ArrayList<Node> inputs = new ArrayList<>();

        for (int i = node.connections.in.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.in.get(i);
            if (Mutation.SUB_NODE.keepGates && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            inputs.add(connection.from);
            this.disconnect(connection.from, node);
        }

        final ArrayList<Node> outputs = new ArrayList<>();
        for (int i = node.connections.out.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.out.get(i);
            if (Mutation.SUB_NODE.keepGates && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            outputs.add(connection.to);
            this.disconnect(node, connection.to);
        }

        final ArrayList<Connection> connections = new ArrayList<>();
        for (final Node input : inputs) {
            for (final Node output : outputs) {
                if (!input.isProjectingTo(output)) {
                    connections.addAll(this.connect(input, output, null));
                }
            }
        }

        for (final Node gater : gaters) {
            if (connections.size() == 0) {
                break;
            }
            final int connectionIndex = (int) Math.floor(Math.random() * connections.size());
            this.gate(gater, connections.get(connectionIndex));
            connections.remove(connectionIndex);
        }
        for (int i = node.connections.gated.size() - 1; i >= 0; i--) {
            this.ungate(node.connections.gated.get(i));
        }
        this.disconnect(node, node);
        this.nodes.remove(index);
    }
}
