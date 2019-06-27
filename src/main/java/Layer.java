import enums.Activation;
import enums.NodeType;

import java.util.ArrayList;

public abstract class Layer extends NodeGroup {
    private final ConnectionHistory connections;
    ArrayList<Node> nodes;
    NodeGroup output;

    public Layer(final int size) {
        super(size);
        this.output = null;
        this.nodes = new ArrayList<>();
        this.connections = new ConnectionHistory();
    }

    @Override
    public ArrayList<Double> activate(final double[] value) {
        final ArrayList<Double> values = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            double activation = 0;
            if (value == null) {
                activation = this.nodes.get(i).activate();
            } else {
                activation = this.nodes.get(i).activate(value[i]);
            }
            values.add(activation);
        }
        return values;
    }

    @Override
    public void propagate(final double rate, final double momentum, final double[] target) {
        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            if (target == null) {
                this.nodes.get(i).propagate(rate, momentum, true);
            } else {
                this.nodes.get(i).propagate(rate, momentum, true, target[i]);
            }
        }
    }

    @Override
    public ArrayList<Connection> connect(final NodeGroup target, final Connection.Method method, final Double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    public ArrayList<Connection> connect(final Node target, final Connection.Method method, final Double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    public ArrayList<Connection> connect(final Layer target, final Connection.Method method, final Double weight) {
        return target.input(this, method, weight);
    }

    public void gate(final ArrayList<Connection> connections, final Connection.Gating method) {
        this.output.gate(connections, method);
    }

    public void set(final Activation squash, final Double bias, final NodeType type) {
        this.nodes.stream().filter(node -> node != null).forEach(node -> {
            if (bias != null) {
                node.bias = bias;
            }
            node.squash = squash == null ? node.squash : squash;
            node.type = type == null ? node.type : type;
        });
    }

    @Override
    public void disconnect(final Node target, final Boolean twosided) {
        for (int i = 0; i < this.nodes.size(); i++) {
            this.nodes.get(i).disconnect(target, twosided);

            for (int j = this.connections.out.size() - 1; j >= 0; j--) {
                final Connection conn = this.connections.out.get(j);

                if (conn.from == this.nodes.get(i) && conn.to == target) {
                    this.connections.out.remove(j);
                    break;
                }
            }

            if (twosided) {
                for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                    final Connection conn = this.connections.in.get(k);

                    if (conn.from == target && conn.to == this.nodes.get(i)) {
                        this.connections.in.remove(k);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void disconnect(final NodeGroup target, Boolean twosided) {
        twosided = twosided != null ? twosided : false;
        for (int i = 0; i < this.nodes.size(); i++) {
            for (int j = 0; j < target.nodes.size(); j++) {
                this.nodes.get(i).disconnect(target.nodes.get(j), twosided);

                for (int k = this.connections.out.size() - 1; k >= 0; k--) {
                    final Connection conn = this.connections.out.get(k);

                    if (conn.from == this.nodes.get(i) && conn.to == target.nodes.get(j)) {
                        this.connections.out.remove(k);
                        break;
                    }
                }

                if (twosided) {
                    for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                        final Connection conn = this.connections.in.get(k);

                        if (conn.from == target.nodes.get(j) && conn.to == this.nodes.get(i)) {
                            this.connections.in.remove(k);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear() {
        this.nodes.forEach(Node::clear);
    }

    public abstract ArrayList<Connection> input(final NodeGroup from, final Connection.Method method, final double weight);

    public abstract ArrayList<Connection> input(final Layer from, final Connection.Method method, final double weight);
}
