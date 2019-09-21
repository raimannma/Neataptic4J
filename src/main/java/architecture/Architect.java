package architecture;

import methods.Activation;
import methods.ConnectionType;
import methods.GatingType;
import methods.MutationType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static methods.GatingType.*;

public class Architect {
    public static Network createPerceptron(final int... layers) {
        if (layers.length < 3) {
            throw new RuntimeException("You have to specify at least 3 layers");
        }

        final List<Object> nodes = new ArrayList<>();
        NodeGroup last = new NodeGroup(layers[0]);
        nodes.add(last);
        for (int i = 1; i < layers.length; i++) {
            final NodeGroup current = new NodeGroup(layers[i]);
            nodes.add(current);
            last.connect(current, ConnectionType.ALL_TO_ALL);
            last = current;
        }
        return construct(nodes);
    }

    public static Network construct(final List<Object> list) {
        final Network network = new Network(0, 0);

        List<Node> nodes = new ArrayList<>();

        for (final Object elem : list) {
            if (elem instanceof Layer) {
                for (final NodeGroup node : ((Layer) elem).nodes) {
                    final ArrayList<Node> nodeArrayList = node.nodes;
                    nodes.addAll(nodeArrayList);
                }
            } else if (elem instanceof NodeGroup) {
                nodes.addAll(((NodeGroup) elem).nodes);
            } else if (elem instanceof Node) {
                nodes.add((Node) elem);
            }
        }

        final List<Node> inputs = new ArrayList<>();
        final List<Node> outputs = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).type == Node.NodeType.OUTPUT || nodes.get(i).connections.out.size() + nodes.get(i).connections.gated.size() == 0) {
                nodes.get(i).type = Node.NodeType.OUTPUT;
                network.output++;
                outputs.add(nodes.get(i));
                nodes.remove(i);
            } else if (nodes.get(i).type == Node.NodeType.INPUT || nodes.get(i).connections.in.size() == 0) {
                nodes.get(i).type = Node.NodeType.INPUT;
                network.input++;
                inputs.add(nodes.get(i));
                nodes.remove(i);
            }
        }
        final ArrayList<Node> temp = new ArrayList<>();
        temp.addAll(inputs);
        temp.addAll(nodes);
        temp.addAll(outputs);
        nodes = temp;
        if (network.input == 0 || network.output == 0) {
            throw new RuntimeException("Given nodes have no clear input/output node!");
        }

        nodes.forEach(node -> {
            network.connections.addAll(node.connections.out);
            network.gates.addAll(node.connections.gated);
            if (node.connections.self.weight != 0) {
                network.selfConns.add(node.connections.self);
            }
        });
        network.nodes = nodes;
        return network;
    }

    public static Network createRandom(final int input, final int hidden, final int output, final Map<Option, Integer> options) {
        final int connections = options.getOrDefault(Option.CONNECTIONS, hidden * 2);
        final int backConnections = options.getOrDefault(Option.BACK_CONNECTIONS, 0);
        final int selfConnections = options.getOrDefault(Option.SELF_CONNECTIONS, 0);
        final int gates = options.getOrDefault(Option.GATES, 0);

        final Network network = new Network(input, output);
        IntStream.range(0, hidden).mapToObj(i -> MutationType.ADD_NODE).forEach(network::mutate);
        IntStream.range(0, connections - hidden).mapToObj(i -> MutationType.ADD_CONN).forEach(network::mutate);
        IntStream.range(0, backConnections).mapToObj(i -> MutationType.ADD_BACK_CONN).forEach(network::mutate);
        IntStream.range(0, selfConnections).mapToObj(i -> MutationType.ADD_SELF_CONN).forEach(network::mutate);
        IntStream.range(0, gates).mapToObj(i -> MutationType.ADD_GATE).forEach(network::mutate);
        return network;
    }

    public static Network createGRU(final int... layers) {
        final NodeGroup inputLayer = new NodeGroup(layers[0]);
        final NodeGroup outputLayer = new NodeGroup(layers[layers.length - 1]);

        final int[] blocks = Arrays.copyOfRange(layers, 1, layers.length - 1);
        final List<Object> nodes = new ArrayList<>();
        nodes.add(inputLayer);

        NodeGroup previous = inputLayer;
        for (final int block : blocks) {
            final GRU layer = new GRU(block);
            previous.connect(layer, null, 0);
            previous = layer;
            nodes.add(layer);
        }
        previous.connect(outputLayer, null, 0);
        nodes.add(outputLayer);
        return construct(nodes);
    }

    public static Network createHopfield(final int size) {
        final NodeGroup input = new NodeGroup(size);
        final NodeGroup output = new NodeGroup(size);

        input.connect(output, ConnectionType.ALL_TO_ALL);

        input.set(new Node.NodeValues().setType(Node.NodeType.INPUT));
        output.set(new Node.NodeValues().setSquash(Activation.STEP).setType(Node.NodeType.OUTPUT));

        final List<Object> nodes = new ArrayList<>();
        nodes.add(input);
        nodes.add(output);
        return construct(nodes);
    }

    public static Network createNARX(final int inputSize, final int[] hiddenLayers, final int outputSize, final int previousInput, final int previousOutput) {
        final List<Object> nodes = new ArrayList<>();
        final Dense input = new Dense(inputSize);
        final Memory inputMemory = new Memory(inputSize, previousInput);
        final List<Layer> hidden = new ArrayList<>();
        final Dense output = new Dense(outputSize);
        final Memory outputMemory = new Memory(outputSize, previousOutput);

        nodes.add(input);
        nodes.add(outputMemory);
        for (int i = 0; i < hiddenLayers.length; i++) {
            final Dense hiddenLayer = new Dense(hiddenLayers[i]);
            hidden.add(hiddenLayer);
            nodes.add(hiddenLayer);
            if (i - 1 < hidden.size() && i >= 1 && hidden.get(i - 1) != null) {
                hidden.get(i - 1).connect(hiddenLayer, ConnectionType.ALL_TO_ALL);
            }
        }

        nodes.add(inputMemory);
        nodes.add(output);

        input.connect(hidden.get(0), ConnectionType.ALL_TO_ALL);
        input.connect(inputMemory, ConnectionType.ONE_TO_ONE, 1);
        inputMemory.connect(hidden.get(0), ConnectionType.ALL_TO_ALL);
        hidden.get(hidden.size() - 1).connect(output, ConnectionType.ALL_TO_ALL);
        output.connect(outputMemory, ConnectionType.ONE_TO_ONE, 1);
        outputMemory.connect(hidden.get(0), ConnectionType.ALL_TO_ALL);

        input.set(new Node.NodeValues().setType(Node.NodeType.INPUT));
        output.set(new Node.NodeValues().setType(Node.NodeType.OUTPUT));
        return construct(nodes);
    }

    public static Network createLSTM(final int... layers) {
        return createLSTM(new HashMap<>(), layers);
    }

    public static Network createLSTM(Map<Option, Boolean> options, final int... layers) {
        if (options == null) {
            options = new HashMap<>();
        }
        final int last = layers[layers.length - 1];
        final NodeGroup oututLayer = new NodeGroup(last);
        oututLayer.set(new Node.NodeValues().setType(Node.NodeType.OUTPUT));

        final NodeGroup inputLayer = new NodeGroup(layers[0]);
        inputLayer.set(new Node.NodeValues().setType(Node.NodeType.INPUT));

        final int[] blocks = Arrays.copyOfRange(layers, 1, layers.length - 1);
        final List<NodeGroup> nodes = new ArrayList<>();
        nodes.add(inputLayer);

        NodeGroup previous = inputLayer;
        for (int i = 0; i < blocks.length; i++) {
            final int block = blocks[i];

            final NodeGroup inputGate = new NodeGroup(block);
            final NodeGroup forgetGate = new NodeGroup(block);
            final NodeGroup memoryCell = new NodeGroup(block);
            final NodeGroup outputGate = new NodeGroup(block);
            final NodeGroup outputBlock = i == blocks.length - 1 ? oututLayer : new NodeGroup(block);

            inputGate.set(new Node.NodeValues().setBias(1));
            forgetGate.set(new Node.NodeValues().setBias(1));
            outputGate.set(new Node.NodeValues().setBias(1));

            final List<Connection> input = previous.connect(memoryCell, ConnectionType.ALL_TO_ALL);
            previous.connect(inputGate, ConnectionType.ALL_TO_ALL);
            previous.connect(outputGate, ConnectionType.ALL_TO_ALL);
            previous.connect(forgetGate, ConnectionType.ALL_TO_ALL);

            memoryCell.connect(inputGate, ConnectionType.ALL_TO_ALL);
            memoryCell.connect(forgetGate, ConnectionType.ALL_TO_ALL);
            memoryCell.connect(outputGate, ConnectionType.ALL_TO_ALL);
            final List<Connection> forget = memoryCell.connect(memoryCell, ConnectionType.ONE_TO_ONE);
            final List<Connection> output = memoryCell.connect(outputBlock, ConnectionType.ALL_TO_ALL);

            inputGate.gate(input, GatingType.INPUT);
            forgetGate.gate(forget, SELF);
            outputGate.gate(output, OUTPUT);

            if (options.getOrDefault(Option.INPUT_TO_DEEP, true) && i > 0) {
                inputGate.gate(inputLayer.connect(memoryCell, ConnectionType.ALL_TO_ALL), INPUT);
            }

            if (options.getOrDefault(Option.MEMORY_TO_MEMORY, false)) {
                inputGate.gate(memoryCell.connect(memoryCell, ConnectionType.ALL_TO_ELSE), INPUT);
            }

            if (options.getOrDefault(Option.OUTPUT_TO_MEMORY, false)) {
                inputGate.gate(oututLayer.connect(memoryCell, ConnectionType.ALL_TO_ALL), INPUT);
            }

            if (options.getOrDefault(Option.OUTPUT_TO_GATES, false)) {
                oututLayer.connect(inputGate, ConnectionType.ALL_TO_ALL);
                oututLayer.connect(forgetGate, ConnectionType.ALL_TO_ALL);
                oututLayer.connect(outputGate, ConnectionType.ALL_TO_ALL);
            }

            nodes.add(inputGate);
            nodes.add(forgetGate);
            nodes.add(memoryCell);
            nodes.add(outputGate);
            if (i != blocks.length - 1) {
                nodes.add(outputBlock);
            }
            previous = outputBlock;
        }
        if (options.getOrDefault(Option.INPUT_TO_OUTPUT, true)) {
            inputLayer.connect(oututLayer, ConnectionType.ALL_TO_ALL);
        }
        nodes.add(oututLayer);
        return construct(nodes.stream().map((Function<NodeGroup, Object>) nodeGroup -> nodeGroup).collect(Collectors.toList()));
    }

    enum Option {CONNECTIONS, BACK_CONNECTIONS, SELF_CONNECTIONS, GATES, MEMORY_TO_MEMORY, OUTPUT_TO_MEMORY, OUTPUT_TO_GATES, INPUT_TO_OUTPUT, INPUT_TO_DEEP}
}
