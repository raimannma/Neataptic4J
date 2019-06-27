import java.util.ArrayList;
import java.util.List;

public class LSTM extends Layer {
    private final NodeGroup inputGate;
    private final NodeGroup forgetGate;
    private final NodeGroup memoryCell;
    private final NodeGroup outputGate;
    private final NodeGroup outputBlock;

    public LSTM(final int size) {
        super(size);
        this.inputGate = new NodeGroup(size);
        this.forgetGate = new NodeGroup(size);
        this.memoryCell = new NodeGroup(size);
        this.outputGate = new NodeGroup(size);
        this.outputBlock = new NodeGroup(size);

        this.inputGate.set(1.0, null, null);
        this.forgetGate.set(1.0, null, null);
        this.outputGate.set(1.0, null, null);

        this.memoryCell.connect(this.inputGate, Connection.Method.ALL_TO_ALL, null);
        this.memoryCell.connect(this.forgetGate, Connection.Method.ALL_TO_ALL, null);
        this.memoryCell.connect(this.outputGate, Connection.Method.ALL_TO_ALL, null);
        final List<Connection> forget = this.memoryCell.connect(this.memoryCell, Connection.Method.ONE_TO_ONE, null);
        final List<Connection> output = this.memoryCell.connect(this.outputBlock, Connection.Method.ALL_TO_ALL, null);

        this.forgetGate.gate(forget, Connection.Gating.SELF);
        this.outputGate.gate(output, Connection.Gating.OUTPUT);

        this.nodes = new ArrayList<>();
        this.nodes.add(this.inputGate);
        this.nodes.add(this.forgetGate);
        this.nodes.add(this.memoryCell);
        this.nodes.add(this.outputGate);
        this.nodes.add(this.outputBlock);

        this.output = this.outputBlock;
    }

    @Override
    public ArrayList<Connection> input(final NodeGroup from, Connection.Method method, final Double weight) {
        method = method == null ? Connection.Method.ALL_TO_ALL : method;
        final ArrayList<Connection> connections = new ArrayList<>();

        final List<Connection> input = from.connect(this.memoryCell, method, weight);
        connections.addAll(input);

        connections.addAll(from.connect(this.inputGate, method, weight));
        connections.addAll(from.connect(this.outputGate, method, weight));
        connections.addAll(from.connect(this.forgetGate, method, weight));

        this.inputGate.gate(input, Connection.Gating.INPUT);

        return connections;
    }

    @Override
    public ArrayList<Connection> input(final Layer from, final Connection.Method method, final Double weight) {
        return this.input(from.output, method, weight);
    }
}
