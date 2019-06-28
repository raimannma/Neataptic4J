import java.util.Collections;
import java.util.List;

public class Memory extends Layer {

    private NodeGroup previous;

    public Memory(final int size, final int memory) {
        super(size);

        this.previous = null;
        for (int i = 0; i < memory; i++) {
            final NodeGroup block = new NodeGroup(size);
            block.set(0.0, Activation.IDENTITY, NodeType.CONSTANT);

            if (this.previous != null) {
                this.previous.connect(block, Connection.Method.ONE_TO_ONE, null);
            }
            this.nodes.add(block);
            this.previous = block;
        }
        Collections.reverse(this.nodes);

        final NodeGroup outputGroup = new NodeGroup(0);
        for (final NodeGroup group : this.nodes) {
            outputGroup.nodes.addAll(group.nodes);
        }
        this.output = outputGroup;
    }

    @Override
    public List<Connection> input(final NodeGroup from, Connection.Method method, final Double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        return from.connect(this.nodes.get(this.nodes.size() - 1), Connection.Method.ONE_TO_ONE, 1.0);
    }

    @Override
    public List<Connection> input(final Layer from, final Connection.Method method, final Double weight) {
        return this.input(from.output, method, weight);
    }
}
