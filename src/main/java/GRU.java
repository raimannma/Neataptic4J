import methods.Activation;
import methods.ConnectionType;
import methods.GatingType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GRU extends Layer {

    private final NodeGroup resetGate;
    private final NodeGroup updateGate;
    private final NodeGroup memoryCell;

    public GRU(final int size) {
        super();

        this.updateGate = new NodeGroup(size);
        final NodeGroup inverseUpdateGate = new NodeGroup(size);
        this.resetGate = new NodeGroup(size);
        this.memoryCell = new NodeGroup(size);
        final NodeGroup output = new NodeGroup(size);
        final NodeGroup previousOutput = new NodeGroup(size);

        previousOutput.set(new Node.NodeValues().setBias(0).setSquash(Activation.IDENTITY).setType(Node.NodeType.CONSTANT));
        this.memoryCell.set(new Node.NodeValues().setSquash(Activation.TANH));
        inverseUpdateGate.set(new Node.NodeValues().setBias(0).setSquash(Activation.INVERSE).setType(Node.NodeType.CONSTANT));
        this.updateGate.set(new Node.NodeValues().setBias(1));
        this.resetGate.set(new Node.NodeValues().setBias(0));

        previousOutput.connect(this.updateGate, ConnectionType.ALL_TO_ALL);
        this.updateGate.connect(inverseUpdateGate, ConnectionType.ONE_TO_ONE, 1);
        previousOutput.connect(this.resetGate, ConnectionType.ALL_TO_ALL);
        final List<Connection> reset = previousOutput.connect(this.memoryCell, ConnectionType.ALL_TO_ALL);
        this.resetGate.gate(reset, GatingType.OUTPUT);

        final List<Connection> update1 = previousOutput.connect(output, ConnectionType.ALL_TO_ALL);
        final List<Connection> update2 = this.memoryCell.connect(output, ConnectionType.ALL_TO_ALL);

        this.updateGate.gate(update1, GatingType.OUTPUT);
        inverseUpdateGate.gate(update2, GatingType.OUTPUT);

        output.connect(previousOutput, ConnectionType.ONE_TO_ONE, 1);

        this.nodes = Arrays.asList(this.updateGate, inverseUpdateGate, this.resetGate, this.memoryCell, output, previousOutput);

        this.output = output;
    }

    @Override
    public List<Connection> input(final NodeGroup from, ConnectionType method, final double weight) {
        method = method == null ? ConnectionType.ALL_TO_ALL : method;
        final List<Connection> connections = new ArrayList<>(from.connect(this.updateGate, method, weight));
        connections.addAll(from.connect(this.resetGate, method, weight));
        connections.addAll(from.connect(this.memoryCell, method, weight));
        return connections;
    }
}
