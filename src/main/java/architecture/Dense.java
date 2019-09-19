package architecture;

import methods.ConnectionType;

import java.util.List;

public class Dense extends Layer {
    private final NodeGroup block;

    public Dense(final int size) {
        this.block = new NodeGroup(size);
        this.nodes.add(this.block);
        this.output = this.block;
    }

    @Override
    public List<Connection> input(final NodeGroup from, ConnectionType method, final double weight) {
        method = method == null ? ConnectionType.ALL_TO_ALL : method;
        return from.connect(this.block, method, weight);
    }
}
