import enums.Activation;
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


    public List<Connection> connect(final Node from, final Node to) {
        return this.connect(from, to, null);
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

    public void mutate(final Mutation method) {
        if (method == Mutation.NULL) {
            throw new RuntimeException("No mutate method given!");
        }

        switch (method) {
            case ADD_NODE:
                final Connection connection = this.connections.get((int) Math.floor(Math.random() * this.connections.size()));
                final Node gater = connection.gater;
                this.disconnect(connection.from, connection.to);
                final int toIndex = this.nodes.indexOf(connection.to);
                Node node = new Node(NodeType.HIDDEN);
                node.mutate(Mutation.MOD_ACTIVATION);
                final int minBound = Math.min(toIndex, this.nodes.size() - this.output);
                this.nodes.add(minBound, node);

                final Connection newConnection1 = this.connect(connection.from, node).get(0);
                final Connection newConnection2 = this.connect(node, connection.to).get(0);
                if (gater != null) {
                    this.gate(gater, Math.random() >= 0.5 ? newConnection1 : newConnection2);
                }
                break;
            case SUB_NODE:
                if (this.nodes.size() == this.input + this.output) {
                    throw new RuntimeException("No more nodes left to remove!");
                }
                final int index = (int) Math.floor(Math.random() * (this.nodes.size() - this.output - this.input) + this.input);
                this.remove(this.nodes.get(index));
                break;
            case ADD_CONN:
                final List<Node[]> available = new ArrayList<>();
                for (int i = 0; i < this.nodes.size() - this.output; i++) {
                    final Node node1 = this.nodes.get(i);
                    for (int j = Math.max(i + 1, this.input); j < this.nodes.size(); j++) {
                        final Node node2 = this.nodes.get(j);
                        if (!node1.isProjectingTo(node2)) {
                            available.add(new Node[]{node1, node2});
                        }
                    }
                }
                if (available.size() == 0) {
                    throw new RuntimeException("No more connections to be made!");
                }
                final Node[] pair = available.get((int) Math.floor(Math.random() * available.size()));
                this.connect(pair[0], pair[1]);
                break;
            case SUB_CONN:
                final ArrayList<Connection> possible = new ArrayList<>();
                for (final Connection conn : this.connections) {
                    if (conn.from.connections.out.size() > 1 &&
                            conn.to.connections.in.size() > 1 &&
                            this.nodes.indexOf(conn.to) > this.nodes.indexOf(conn.from)) {
                        possible.add(conn);
                    }
                }
                if (possible.size() == 0) {
                    throw new RuntimeException("No connections to remove!");
                }
                final Connection randomConn = possible.get((int) Math.floor(Math.random() * possible.size()));
                this.disconnect(randomConn.from, randomConn.to);
                break;
            case MOD_WEIGHT:
                final List<Connection> allConnections = new ArrayList<>(this.connections);
                allConnections.addAll(this.selfConnections);

                final Connection conn = allConnections.get((int) Math.floor(Math.random() * allConnections.size()));
                final double modification = Math.random() * (method.max - method.min) + method.min;
                conn.weight += modification;
                break;
            case MOD_BIAS:
                final int index1 = (int) Math.floor(Math.random() * (this.nodes.size() - this.input) + this.input);
                this.nodes.get(index1).mutate(method);
                break;
            case MOD_ACTIVATION:
                if (!method.mutateOutput && this.input + this.output == this.nodes.size()) {
                    throw new RuntimeException("No nodes that allow mutation of activation function!");
                }
                final int index2 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                this.nodes.get(index2).mutate(method);
                break;
            case ADD_SELF_CONN:
                final ArrayList<Node> poss = new ArrayList<>();
                for (int i = this.input; i < this.nodes.size(); i++) {
                    node = this.nodes.get(i);
                    if (node.connections.self.weight == 0) {
                        poss.add(node);
                    }
                }
                if (poss.size() == 0) {
                    throw new RuntimeException("No more self-connections to add!");
                }
                node = poss.get((int) Math.floor(Math.random() * poss.size()));
                this.connect(node, node);
                break;
            case SUB_SELF_CONN:
                if (this.selfConnections.size() == 0) {
                    throw new RuntimeException("No more self-connections to remove!");
                }

                final Connection conn1 = this.selfConnections.get((int) Math.floor(Math.random() * this.selfConnections.size()));
                this.disconnect(conn1.from, conn1.to);
                break;
            case ADD_GATE:
                final ArrayList<Connection> allConnections1 = new ArrayList<>(this.connections);
                allConnections1.addAll(this.selfConnections);

                final ArrayList<Connection> possible1 = new ArrayList<>();
                for (final Connection connection1 : allConnections1) {
                    if (connection1.gater == null) {
                        possible1.add(connection1);
                    }
                }
                if (possible1.size() == 0) {
                    throw new RuntimeException("No more connections to gate!");
                }

                final int index3 = (int) Math.floor(Math.random() * (this.nodes.size() - this.input) + this.input);
                this.gate(this.nodes.get(index3), possible1.get((int) Math.floor(Math.random() * possible1.size())));

                break;
            case SUB_GATE:
                if (this.gates.size() == 0) {
                    throw new RuntimeException("No more connections to ungate!");
                }

                final int index4 = (int) Math.floor(Math.random() * this.gates.size());
                final Connection gatedconn = this.gates.get(index4);

                this.ungate(gatedconn);
                break;
            case ADD_BACK_CONN:
                final ArrayList<Node[]> available1 = new ArrayList<>();
                for (int i = this.input; i < this.nodes.size(); i++) {
                    final Node node1 = this.nodes.get(i);
                    for (int j = this.input; j < i; j++) {
                        final Node node2 = this.nodes.get(j);
                        if (!node1.isProjectingTo(node2)) {
                            available1.add(new Node[]{node1, node2});
                        }
                    }
                }
                if (available1.size() == 0) {
                    throw new RuntimeException("No more connections to be made!");
                }
                final Node[] pair1 = available1.get((int) Math.floor(Math.random() * available1.size()));
                this.connect(pair1[0], pair1[1]);
                break;
            case SUB_BACK_CONN:
                final ArrayList<Connection> possible2 = new ArrayList<>();

                for (final Connection conn3 : this.connections) {
                    if (conn3.from.connections.out.size() > 1 &&
                            conn3.to.connections.in.size() > 1 &&
                            this.nodes.indexOf(conn3.from) > this.nodes.indexOf(conn3.to)) {
                        possible2.add(conn3);
                    }
                }

                if (possible2.size() == 0) {
                    throw new RuntimeException("No connections to remove!");
                }

                final Connection randomConn1 = possible2.get((int) Math.floor(Math.random() * possible2.size()));
                this.disconnect(randomConn1.from, randomConn1.to);
                break;
            case SWAP_NODES:
                if (method.mutateOutput && this.nodes.size() - this.input < 2 ||
                        (!method.mutateOutput && this.nodes.size() - this.input - this.output < 2)) {
                    throw new RuntimeException("No nodes that allow swapping of bias and activation function");
                }

                int index5 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                final Node node1 = this.nodes.get(index5);
                index5 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                final Node node2 = this.nodes.get(index5);

                final double biasTemp = node1.bias;
                final Activation squashTemp = node1.squash;

                node1.bias = node2.bias;
                node1.squash = node2.squash;
                node2.bias = biasTemp;
                node2.squash = squashTemp;
                break;
        }
    }
}
