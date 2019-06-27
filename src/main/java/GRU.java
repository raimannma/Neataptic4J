import java.util.ArrayList;

public class GRU extends Layer {
    private final NodeGroup updateGate;
    private final NodeGroup inverseUpdateGate;
    private final NodeGroup resetGate;
    private final NodeGroup memoryCell;
    private final NodeGroup outputGroup;
    private final NodeGroup previousOutput;

    public GRU(final int size) {
        super(size);
        this.updateGate = new NodeGroup(size);
        this.inverseUpdateGate = new NodeGroup(size);
        this.resetGate = new NodeGroup(size);
        this.memoryCell = new NodeGroup(size);
        this.outputGroup = new NodeGroup(size);
        this.previousOutput = new NodeGroup(size);

        this.previousOutput.set(0.0, Activation.IDENTITY, NodeType.CONSTANT);
        this.memoryCell.set(null, Activation.TANH, null);
        this.inverseUpdateGate.set(0.0, Activation.INVERSE, NodeType.CONSTANT);
        this.updateGate.set(1.0, null, null);
        this.resetGate.set(0.0, null, null);

        this.previousOutput.connect(this.updateGate, Connection.Method.ALL_TO_ALL, null);
        this.updateGate.connect(this.inverseUpdateGate, Connection.Method.ONE_TO_ONE, 1.0);
        this.previousOutput.connect(this.resetGate, Connection.Method.ALL_TO_ALL, null);
        final ArrayList<Connection> reset = this.previousOutput.connect(this.memoryCell, Connection.Method.ALL_TO_ALL, null);
        this.resetGate.gate(reset, Connection.Gating.OUTPUT);

        final ArrayList<Connection> update1 = this.previousOutput.connect(this.outputGroup, Connection.Method.ALL_TO_ALL, null);
        final ArrayList<Connection> update2 = this.memoryCell.connect(this.outputGroup, Connection.Method.ALL_TO_ALL, null);

        this.updateGate.gate(update1, Connection.Gating.OUTPUT);
        this.inverseUpdateGate.gate(update2, Connection.Gating.OUTPUT);

        this.output.connect(this.previousOutput, Connection.Method.ONE_TO_ONE, 1.0);

        this.nodes = new ArrayList<>();
        this.nodes.addAll(this.updateGate.nodes);
        this.nodes.addAll(this.inverseUpdateGate.nodes);
        this.nodes.addAll(this.resetGate.nodes);
        this.nodes.addAll(this.memoryCell.nodes);
        this.nodes.addAll(this.output.nodes);
        this.nodes.addAll(this.previousOutput.nodes);

        this.output = this.outputGroup;
    }

    @Override
    public ArrayList<Connection> input(final NodeGroup from, Connection.Method method, final double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        final ArrayList<Connection> connections = new ArrayList<>(from.connect(this.updateGate, method, weight));
        connections.addAll(from.connect(this.resetGate, method, weight));
        connections.addAll(from.connect(this.memoryCell, method, weight));
        return connections;
    }

    @Override
    public ArrayList<Connection> input(final Layer from, final Connection.Method method, final double weight) {
        return this.input(from.output, method, weight);
    }
}
