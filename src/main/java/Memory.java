import methods.Activation;
import methods.ConnectionType;

import java.util.Collections;
import java.util.List;

public class Memory extends Layer {
    public Memory(final int size, final int memory) {
        NodeGroup previous = null;
        for (int i = 0; i < memory; i++) {
            final NodeGroup block = new NodeGroup(size);

            block.set(new Node.NodeValues().setSquash(Activation.IDENTITY).setBias(0).setType(Node.NodeType.CONSTANT));

            if (previous != null) {
                previous.connect(block, ConnectionType.ONE_TO_ONE, 1);
            }
            this.nodes.add(block);
            previous = block;
        }

        Collections.reverse(this.nodes);
        this.nodes.stream().map(node -> node.nodes).forEach(Collections::reverse);
        final NodeGroup outputGroup = new NodeGroup(0);
        for (final NodeGroup group : this.nodes) {
            outputGroup.nodes.addAll(group.nodes);
        }
        this.output = outputGroup;
    }

    @Override
    public List<Connection> input(final NodeGroup from, final ConnectionType method, final double weight) {
        if (from.nodes.size() != this.nodes.get(this.nodes.size() - 1).nodes.size()) {
            throw new RuntimeException("Previous layer size must be same as memory size");
        }
        return from.connect(this.nodes.get(this.nodes.size() - 1), ConnectionType.ONE_TO_ONE, 1);
    }
}
