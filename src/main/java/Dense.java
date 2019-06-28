import java.util.List;

public class Dense extends Layer {


    public Dense(final int size) {
        super(size);
        final NodeGroup block = new NodeGroup(size);

        this.nodes.add(block);
        this.output = block;
    }

    @Override
    public List<Connection> input(final NodeGroup from, Connection.Method method, final Double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        return from.connect(this.output, method, weight);
    }

    @Override
    public List<Connection> input(final Layer from, final Connection.Method method, final Double weight) {
        return this.input(from.output, method, weight);
    }
}
