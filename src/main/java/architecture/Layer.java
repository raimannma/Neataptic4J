package architecture;

import methods.ConnectionType;
import methods.GatingType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class Layer extends NodeGroup {
    private final ConnectionListLayer connections;
    NodeGroup output;
    List<NodeGroup> nodes;

    public Layer() {
        super(0);
        this.output = null;
        this.nodes = new ArrayList<>();
        this.connections = new ConnectionListLayer();
    }

    @Override
    void gate(final List<Connection> connections, final GatingType method) {
        this.output.gate(connections, method);
    }

    @Override
    List<Connection> connect(final NodeGroup target, final ConnectionType method, final double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    void disconnect(final NodeGroup target) {
        this.disconnect(target, false);
    }

    private void disconnect(final NodeGroup target, final boolean twoSided) {
        for (final NodeGroup nodeGroup : this.nodes) {
            for (int j = 0; j < target.nodes.size(); j++) {
                this.nodes.get(j).disconnect(target.nodes.get(j), twoSided);

                for (int k = this.connections.out.size() - 1; k >= 0; k--) {
                    final Connection connection = this.connections.out.get(k);

                    if (connection.from.equals(nodeGroup.nodes.get(0)) && connection.to.equals(target.nodes.get(j))) {
                        this.connections.in.remove(k);
                    }
                }

                if (twoSided) {
                    for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                        final Connection connection = this.connections.in.get(k);
                        if (connection.from.equals(target.nodes.get(j)) && connection.to.equals(nodeGroup.nodes.get(0))) {
                            this.connections.in.remove(k);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void disconnect(final Node target) {
        this.disconnect(target, false);
    }

    @Override
    public void disconnect(final Node target, final boolean twoSided) {
        for (final NodeGroup nodeGroup : this.nodes) {
            nodeGroup.disconnect(target, twoSided);
            for (int j = this.connections.out.size() - 1; j >= 0; j--) {
                final Connection connection = this.connections.out.get(j);

                if (connection.from.equals(nodeGroup.nodes.get(0)) && connection.to.equals(target)) {
                    this.connections.out.remove(j);
                    break;
                }
            }
            if (twoSided) {
                for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                    final Connection connection = this.connections.in.get(k);
                    if (connection.from.equals(target) && connection.to.equals(nodeGroup.nodes.get(0))) {
                        this.connections.in.remove(k);
                        break;
                    }
                }
            }
        }
    }

    @Override
    double[] activate(final double[] value) {
        if (value != null && value.length != this.nodes.size()) {
            throw new RuntimeException("Array with values should be same as the amount of nodes!");
        }
        return IntStream.range(0, this.nodes.size())
                .mapToDouble(i -> value == null ? this.nodes.get(i).nodes.get(0).activate() : this.nodes.get(i).nodes.get(0).activate(value[i]))
                .toArray();
    }

    @Override
    void propagate(final double rate, final double momentum, final double[] target) {
        if (target != null && target.length != this.nodes.size()) {
            throw new RuntimeException("Array with values should be same as the amount of nodes!");
        }

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            if (target == null) {
                this.nodes.get(i).nodes.get(0).propagate(rate, momentum, true);
            } else {
                this.nodes.get(i).nodes.get(0).propagate(rate, momentum, true, target[i]);
            }
        }
    }

    @Override
    List<Connection> connect(final Layer target, final ConnectionType method, final double weight) {
        return target.input(this, method, weight);
    }

    @Override
    List<Connection> connect(final Node target, final ConnectionType method, final double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    void set(final Node.NodeValues values) {
        this.nodes.forEach(node -> node.set(values));
    }

    @Override
    void clear() {
        this.nodes.forEach(NodeGroup::clear);
    }

    public List<Connection> input(final Layer layer, final ConnectionType method, final double weight) {
        return this.input(layer.output, method, weight);
    }

    public abstract List<Connection> input(NodeGroup from, ConnectionType method, double weight);
}
