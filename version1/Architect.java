package version1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Architect {

    public Network construct(final List<Layer> list, final Layer group) {
        final Network network = new Network(0, 0);
        final ArrayList<Node> nodes = new ArrayList<>();
        for (final Layer layer : list) {
            for (int j = 0; j < layer.nodes.size(); j++) {
                nodes.addAll(layer.nodes.get(j).nodes);
            }
        }
        final List<Node> inputs = new ArrayList<>();
        final List<Node> outputs = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).type == NodeType.OUTPUT ||
                    nodes.get(i).connections.out.size() + nodes.get(i).connections.gated.size() == 0) {
                nodes.get(i).type = NodeType.OUTPUT;
                outputs.add(nodes.get(i));
                nodes.remove(i);
                network.output++;
            } else if (nodes.get(i).type == NodeType.INPUT ||
                    nodes.get(i).connections.in.size() == 0) {
                nodes.get(i).type = NodeType.INPUT;
                network.input++;
                inputs.add(nodes.get(i));
                nodes.remove(i);
            }
        }

        nodes.addAll(inputs);
        nodes.addAll(outputs);

        if (network.input == 0 || network.output == 0) {
            throw new RuntimeException("Given nodes have no clear input/output node!");
        }
        for (final Node node : nodes) {
            network.connections.addAll(node.connections.out);
            network.gates.addAll(node.connections.gated);
            if (node.connections.self.weight != 0) {
                network.selfConnections.add(node.connections.self);
            }
        }
        network.nodes = nodes;
        return network;
    }

    public Network construct(final List<NodeGroup> list, final NodeGroup group) {
        final Network network = new Network(0, 0);
        final ArrayList<Node> nodes = new ArrayList<>();
        for (final NodeGroup nodeGroup : list) {
            nodes.addAll(nodeGroup.nodes);
        }
        final List<Node> inputs = new ArrayList<>();
        final List<Node> outputs = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).type == NodeType.OUTPUT ||
                    nodes.get(i).connections.out.size() + nodes.get(i).connections.gated.size() == 0) {
                nodes.get(i).type = NodeType.OUTPUT;
                outputs.add(nodes.get(i));
                nodes.remove(i);
                network.output++;
            } else if (nodes.get(i).type == NodeType.INPUT ||
                    nodes.get(i).connections.in.size() == 0) {
                nodes.get(i).type = NodeType.INPUT;
                network.input++;
                inputs.add(nodes.get(i));
                nodes.remove(i);
            }
        }

        nodes.addAll(inputs);
        nodes.addAll(outputs);

        if (network.input == 0 || network.output == 0) {
            throw new RuntimeException("Given nodes have no clear input/output node!");
        }
        for (final Node node : nodes) {
            network.connections.addAll(node.connections.out);
            network.gates.addAll(node.connections.gated);
            if (node.connections.self.weight != 0) {
                network.selfConnections.add(node.connections.self);
            }
        }
        network.nodes = nodes;
        return network;
    }

    public Network construct(final List<Node> list) {
        final Network network = new Network(0, 0);
        final ArrayList<Node> nodes = new ArrayList<>(list);

        final List<Node> inputs = new ArrayList<>();
        final List<Node> outputs = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).type == NodeType.OUTPUT ||
                    nodes.get(i).connections.out.size() + nodes.get(i).connections.gated.size() == 0) {
                nodes.get(i).type = NodeType.OUTPUT;
                outputs.add(nodes.get(i));
                nodes.remove(i);
                network.output++;
            } else if (nodes.get(i).type == NodeType.INPUT ||
                    nodes.get(i).connections.in.size() == 0) {
                nodes.get(i).type = NodeType.INPUT;
                network.input++;
                inputs.add(nodes.get(i));
                nodes.remove(i);
            }
        }

        nodes.addAll(inputs);
        nodes.addAll(outputs);

        if (network.input == 0 || network.output == 0) {
            throw new RuntimeException("Given nodes have no clear input/output node!");
        }
        for (final Node node : nodes) {
            network.connections.addAll(node.connections.out);
            network.gates.addAll(node.connections.gated);
            if (node.connections.self.weight != 0) {
                network.selfConnections.add(node.connections.self);
            }
        }
        network.nodes = nodes;
        return network;
    }

    public Network Perceptron(final int[] layers) {
        if (layers.length < 3) {
            throw new RuntimeException("You have to specify at least 3 layers");
        }
        final ArrayList<NodeGroup> nodes = new ArrayList<>();
        nodes.add(new NodeGroup(layers[0]));
        for (int i = 1; i < layers.length; i++) {
            final NodeGroup layer = new NodeGroup(layers[i]);
            nodes.add(layer);
            nodes.get(i - 1).connect(nodes.get(i), Connection.Method.ALL_TO_ALL, null);
        }
        return this.construct(nodes, new NodeGroup(0));
    }

    public Network Random(final int input, final int hidden, final int output, TrainingOptions options) {
        options = options == null ? new TrainingOptions() : options;

        final int connections = options.getConnections(hidden * 2);
        final int backConnections = options.getBackConnections(0);
        final int selfConnections = options.getSelfConnections(0);
        final int gates = options.getGates(0);
        final Network network = new Network(input, output);

        for (int i = 0; i < hidden; i++) {
            network.mutate(Mutation.ADD_NODE);
        }

        for (int i = 0; i < connections - hidden; i++) {
            network.mutate(Mutation.ADD_CONN);
        }

        for (int i = 0; i < backConnections; i++) {
            network.mutate(Mutation.ADD_BACK_CONN);
        }

        for (int i = 0; i < selfConnections; i++) {
            network.mutate(Mutation.ADD_SELF_CONN);
        }

        for (int i = 0; i < gates; i++) {
            network.mutate(Mutation.ADD_GATE);
        }

        return network;
    }

    public Network LSTM(final int[] layers) {
        if (layers.length < 3) {
            throw new RuntimeException("You have to specify at least 3 layers!");
        }

        final int last = layers[layers.length - 1];
        final NodeGroup outputLayer = new NodeGroup(last);
        outputLayer.set(0.0, null, NodeType.OUTPUT);

        final boolean memoryToMemory = false;
        final boolean outputToMemory = false;
        final boolean outputToGates = false;
        final boolean inputToOutput = true;
        final boolean inputToDeep = true;

        final NodeGroup inputLayer = new NodeGroup(layers[0]);
        inputLayer.set(0.0, null, NodeType.INPUT);

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
            final NodeGroup outputBlock = i == blocks.length - 1 ? outputLayer : new NodeGroup(block);

            inputGate.set(1.0, null, null);
            forgetGate.set(1.0, null, null);
            outputGate.set(1.0, null, null);

            List<Connection> input = previous.connect(memoryCell, Connection.Method.ALL_TO_ALL, null);
            previous.connect(inputGate, Connection.Method.ALL_TO_ALL, null);
            previous.connect(outputGate, Connection.Method.ALL_TO_ALL, null);
            previous.connect(forgetGate, Connection.Method.ALL_TO_ALL, null);

            memoryCell.connect(inputGate, Connection.Method.ALL_TO_ALL, null);
            memoryCell.connect(forgetGate, Connection.Method.ALL_TO_ALL, null);
            memoryCell.connect(outputGate, Connection.Method.ALL_TO_ALL, null);
            final List<Connection> forget = memoryCell.connect(memoryCell, Connection.Method.ONE_TO_ONE, null);
            final List<Connection> output = memoryCell.connect(outputBlock, Connection.Method.ALL_TO_ALL, null);

            inputGate.gate(input, Connection.Gating.INPUT);
            forgetGate.gate(forget, Connection.Gating.SELF);
            outputGate.gate(output, Connection.Gating.OUTPUT);

            if (inputToDeep && i > 0) {
                input = inputLayer.connect(memoryCell, Connection.Method.ALL_TO_ALL, null);
                inputGate.gate(input, Connection.Gating.INPUT);
            }
            if (memoryToMemory) {
                input = memoryCell.connect(memoryCell, Connection.Method.ALL_TO_ELSE, null);
                inputGate.gate(input, Connection.Gating.INPUT);
            }
            if (outputToMemory) {
                input = outputLayer.connect(memoryCell, Connection.Method.ALL_TO_ALL, null);
                inputGate.gate(input, Connection.Gating.INPUT);
            }
            if (outputToGates) {
                outputLayer.connect(inputGate, Connection.Method.ALL_TO_ALL, null);
                outputLayer.connect(forgetGate, Connection.Method.ALL_TO_ALL, null);
                outputLayer.connect(outputGate, Connection.Method.ALL_TO_ALL, null);
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
        if (inputToOutput) {
            inputLayer.connect(outputLayer, Connection.Method.ALL_TO_ALL, null);
        }
        nodes.add(outputLayer);
        return this.construct(nodes, new NodeGroup(0));
    }

    public Network GRU(final int[] layers) {
        if (layers.length < 3) {
            throw new RuntimeException("You have to specify at least 3 layers!");
        }

        final NodeGroup inputLayer = new NodeGroup(layers[0]);
        final NodeGroup outputLayer = new NodeGroup(layers[layers.length - 1]);
        final int[] blocks = Arrays.copyOfRange(layers, 1, layers.length - 1);

        final List<NodeGroup> nodes = new ArrayList<>();
        nodes.add(inputLayer);

        NodeGroup previous = inputLayer;
        for (final int block : blocks) {
            final Layer layer = new GRU(block);
            previous.connect(layer, null, null);
            previous = layer;
            nodes.add(layer);
        }
        previous.connect(outputLayer, null, null);
        nodes.add(outputLayer);
        return this.construct(nodes, new NodeGroup(0));
    }


    public Network Hopfield(final int size) {
        final NodeGroup input = new NodeGroup(size);
        final NodeGroup output = new NodeGroup(size);

        input.connect(output, Connection.Method.ALL_TO_ALL, null);

        input.set(0.0, null, NodeType.INPUT);
        output.set(0.0, Activation.STEP, NodeType.OUTPUT);

        final List<NodeGroup> nodes = new ArrayList<>();
        nodes.add(input);
        nodes.add(output);
        return this.construct(nodes, new NodeGroup(0));
    }

    public Network NARX(final int inputSize, final int[] hiddenLayers, final int outputSize, final int previousInput, final int previousOutput) {
        final List<NodeGroup> nodes = new ArrayList<>();

        final Dense input = new Dense(inputSize);
        final Memory inputMemory = new Memory(inputSize, previousInput);

        final ArrayList<NodeGroup> hidden = new ArrayList<>();

        final Dense output = new Dense(outputSize);
        final Memory outputMemory = new Memory(outputSize, previousOutput);

        nodes.add(input);
        nodes.add(outputMemory);

        for (int i = 0; i < hiddenLayers.length; i++) {
            final Dense hiddenLayer = new Dense(hiddenLayers[i]);
            hidden.add(hiddenLayer);
            nodes.add(hiddenLayer);
            if (hidden.get(i - 1) != null) {
                hidden.get(i - 1).connect(hiddenLayer, Connection.Method.ALL_TO_ALL, null);
            }
        }

        nodes.add(inputMemory);
        nodes.add(output);

        input.connect(hidden.get(0), Connection.Method.ALL_TO_ALL, null);
        input.connect(inputMemory, Connection.Method.ONE_TO_ONE, 1.0);

        inputMemory.connect(hidden.get(0), Connection.Method.ALL_TO_ALL, null);
        hidden.get(hidden.size() - 1).connect(output, Connection.Method.ALL_TO_ALL, null);
        output.connect(outputMemory, Connection.Method.ONE_TO_ONE, 1.0);
        outputMemory.connect(hidden.get(0), Connection.Method.ALL_TO_ALL, null);

        input.set(0.0, null, NodeType.INPUT);
        output.set(0.0, null, NodeType.OUTPUT);
        return this.construct(nodes, new NodeGroup(0));
    }
}
