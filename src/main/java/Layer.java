import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Layer extends NodeGroup {
    private final ConnectionHistory connections;
    ArrayList<NodeGroup> nodes;
    @Nullable
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
        this.nodes.stream()
                .map(node -> node.activate(value))
                .forEach(values::addAll);
        return values;
    }

    @Override
    public void propagate(final double rate, final double momentum, final double[] target) {
        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            this.nodes.get(i).propagate(rate, momentum, target);
        }
    }

    @Override
    public List<Connection> connect(final NodeGroup target, final Connection.Method method, final Double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    public List<Connection> connect(final Node target, final Connection.Method method, final Double weight) {
        return this.output.connect(target, method, weight);
    }

    @Override
    public void connect(final Layer target, final Connection.Method method, final Double weight) {
        target.input(this, method, weight);
    }

    @Override
    public void gate(final List<Connection> connections, final Connection.Gating method) {
        this.output.gate(connections, method);
    }

    public void set(final Activation squash, final Double bias, final NodeType type) {
        this.nodes.stream().filter(Objects::nonNull).forEach(node -> node.set(bias, squash, type));
    }

    public void disconnect(final Layer target, Boolean twosided) {
        twosided = twosided != null ? twosided : false;
        for (int i = 0; i < this.nodes.size(); i++) {
            for (int j = 0; j < this.nodes.size(); j++) {
                this.nodes.get(i).disconnect(target.nodes.get(j), twosided);
                for (int k = this.connections.out.size() - 1; k >= 0; k--) {
                    final Connection conn = this.connections.out.get(k);
                    if (this.nodes.get(i).nodes.contains(conn.from) &&
                            target.nodes.get(j).nodes.contains(conn.to)) {
                        this.connections.out.remove(k);
                        break;
                    }
                }
                if (twosided) {
                    for (int k = this.connections.in.size() - 1; k >= 0; k--) {
                        final Connection conn = this.connections.in.get(k);

                        if (this.nodes.get(i).nodes.contains(conn.to) &&
                                target.nodes.get(j).nodes.contains(conn.from)) {
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
        this.nodes.forEach(NodeGroup::clear);
    }

    public abstract List<Connection> input(final NodeGroup from, final Connection.Method method, final Double weight);

    public abstract List<Connection> input(final Layer from, final Connection.Method method, final Double weight);
}
