import java.util.ArrayList;

public class Dense extends Layer {


    public Dense(final int size) {
        super(size);
        final NodeGroup block = new NodeGroup(size);

        this.nodes.addAll(block.nodes);
        this.output = block;
    }

    @Override
    public ArrayList<Connection> input(final NodeGroup from, Connection.Method method, final double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        return from.connect(this.output, method, weight);
    }

    @Override
    public ArrayList<Connection> input(final Layer from, final Connection.Method method, final double weight) {
        return this.input(from.output, method, weight);
    }
}
