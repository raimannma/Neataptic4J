import enums.Activation;
import enums.NodeType;

import java.util.ArrayList;
import java.util.Collections;

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
            this.nodes.addAll(block.nodes);
            this.previous = block;
        }
        Collections.reverse(this.nodes);

        final NodeGroup outputGroup = new NodeGroup(0);
        outputGroup.nodes = this.nodes;
        this.output = outputGroup;
    }

    @Override
    public ArrayList<Connection> input(final NodeGroup from, Connection.Method method, final double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        return from.connect(this.nodes.get(this.nodes.size() - 1), Connection.Method.ONE_TO_ONE, 1.0);
    }

    @Override
    public ArrayList<Connection> input(final Layer from, final Connection.Method method, final double weight) {
        return this.input(from.output, method, weight);
    }
}
