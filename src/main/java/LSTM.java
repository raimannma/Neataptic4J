import methods.ConnectionType;
import methods.GatingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LSTM extends Layer {

    private final NodeGroup memoryCell;
    private final NodeGroup inputGate;
    private final NodeGroup outputGate;
    private final NodeGroup forgetGate;

    public LSTM(final int size) {
        this.inputGate = new NodeGroup(size);
        this.forgetGate = new NodeGroup(size);
        this.memoryCell = new NodeGroup(size);
        this.outputGate = new NodeGroup(size);
        final NodeGroup outputBlock = new NodeGroup(size);

        this.inputGate.set(new Node.NodeValues().setBias(1));
        this.forgetGate.set(new Node.NodeValues().setBias(1));
        this.outputGate.set(new Node.NodeValues().setBias(1));

        this.memoryCell.connect(this.inputGate, ConnectionType.ALL_TO_ALL);
        this.memoryCell.connect(this.forgetGate, ConnectionType.ALL_TO_ALL);
        this.memoryCell.connect(this.outputGate, ConnectionType.ALL_TO_ALL);
        final List<Connection> forget = this.memoryCell.connect(this.memoryCell, ConnectionType.ONE_TO_ONE);
        final List<Connection> output = this.memoryCell.connect(outputBlock, ConnectionType.ALL_TO_ALL);

        this.forgetGate.gate(forget, GatingType.SELF);
        this.outputGate.gate(output, GatingType.OUTPUT);

        this.nodes = Arrays.asList(this.inputGate, this.forgetGate, this.memoryCell, this.outputGate, outputBlock);

        this.output = outputBlock;
    }

    @Override
    public List<Connection> input(final NodeGroup from, ConnectionType method, final double weight) {
        method = method == null ? ConnectionType.ALL_TO_ALL : method;

        final List<Connection> input = from.connect(this.memoryCell, method, weight);
        final ArrayList<Connection> connections = new ArrayList<>(input);
        connections.addAll(from.connect(this.inputGate, method, weight));
        connections.addAll(from.connect(this.outputGate, method, weight));
        connections.addAll(from.connect(this.forgetGate, method, weight));
        this.inputGate.gate(input, GatingType.INPUT);
        return connections;
    }
}
