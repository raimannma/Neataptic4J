import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Network {
    int input;
    int output;
    double score = 0;
    ArrayList<Node> nodes;
    ArrayList<Connection> connections;
    ArrayList<Connection> gates;
    ArrayList<Connection> selfConnections;
    private double dropout;

    public Network(final int input, final int output) {
        this.input = input;
        this.output = output;

        this.nodes = new ArrayList<>();
        this.connections = new ArrayList<>();

        this.gates = new ArrayList<>();
        this.selfConnections = new ArrayList<>();

        this.dropout = 0;

        for (int i = 0; i < input + output; i++) {
            this.nodes.add(new Node(i < input ? NodeType.INPUT : NodeType.OUTPUT));
        }
        for (int i = 0; i < input; i++) {
            for (int j = 0; j < output + input; j++) {
                final double weight = Math.random() * input * Math.sqrt((double) 2 / input);
                this.connect(this.nodes.get(i), this.nodes.get(j), weight);
            }
        }
    }


    public static Network merge(final Network network1, final Network network2) {
        if (network1.output != network2.output) {
            throw new RuntimeException("Output size of network1 should be the same as the input size of network2!");
        }
        network2.connections.stream()
                .filter(conn -> conn.from.type == NodeType.INPUT)
                .forEach(conn -> {
                    final int index = network2.nodes.indexOf(conn.from);
                    conn.from = network1.nodes.get(network1.nodes.size() - 1 - index);
                });
        if (network2.input > 0) {
            network2.nodes.subList(0, network2.input).clear();
        }

        for (int i = network1.nodes.size() - network1.output; i < network1.nodes.size(); i++) {
            network1.nodes.get(i).type = NodeType.HIDDEN;
        }

        network1.connections.addAll(network2.connections);
        network1.nodes.addAll(network2.nodes);
        return network1;
    }

    public static List<Integer> getKeys(final List<Connection> list) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i) != null)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Network fromJSON(final JsonObject json) {
        final Network network = new Network(0, 0);
        network.input = json.get("input").getAsInt();
        network.output = json.get("output").getAsInt();
        network.dropout = json.get("dropout").getAsInt();
        final JsonArray nodes = json.get("nodes").getAsJsonArray();
        for (int i = 0; i < nodes.size(); i++) {
            network.nodes.add(Node.fromJSON(nodes.get(i).getAsJsonObject()));
        }
        final JsonArray connections = json.get("connections").getAsJsonArray();
        for (int i = 0; i < connections.size(); i++) {
            network.connections.add(Connection.fromJSON(connections.get(i).getAsJsonObject(), network.nodes));
        }
        final JsonArray gates = json.get("gates").getAsJsonArray();
        for (int i = 0; i < gates.size(); i++) {
            network.gates.add(Connection.fromJSON(gates.get(i).getAsJsonObject(), network.nodes));
        }
        final JsonArray selfConnections = json.get("selfConnections").getAsJsonArray();
        for (int i = 0; i < selfConnections.size(); i++) {
            network.selfConnections.add(Connection.fromJSON(selfConnections.get(i).getAsJsonObject(), network.nodes));
        }
        return network;
    }

    public List<Double> activate(final List<Double> input, final boolean training) {
        final List<Double> output = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == NodeType.INPUT) {
                this.nodes.get(i).activate(input.get(i));
            } else if (this.nodes.get(i).type == NodeType.OUTPUT) {
                output.add(this.nodes.get(i).activate());
            } else {
                if (training) {
                    this.nodes.get(i).mask = Math.random() < this.dropout ? 0 : 1;
                }
                this.nodes.get(i).activate();
            }
        }
        return output;
    }

    public List<Double> noTraceActivate(final List<Double> input) {
        final List<Double> output = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == NodeType.INPUT) {
                this.nodes.get(i).noTraceActivate(input.get(i));
            } else if (this.nodes.get(i).type == NodeType.OUTPUT) {
                output.add(this.nodes.get(i).noTraceActivate());
            } else {
                this.nodes.get(i).noTraceActivate();
            }
        }

        return output;
    }

    public void propagate(final double rate, final double momentum, final boolean update, final ArrayList<Double> target) {
        if (target == null || target.size() != this.output) {
            throw new RuntimeException("Output target length should match network output length!");
        }

        int targetIndex = target.size();
        for (int i = this.nodes.size() - 1; i >= this.nodes.size() - this.output; i--) {
            targetIndex--;
            this.nodes.get(i).propagate(rate, momentum, update, target.get(targetIndex));
        }
        for (int i = this.nodes.size() - this.output - 1; i >= this.input; i--) {
            this.nodes.get(i).propagate(rate, momentum, update);
        }
    }

    public void clear() {
        for (final Node node : this.nodes) {
            node.clear();
        }
    }

    public List<Connection> connect(final Node from, final Node to) {
        return this.connect(from, to, null);
    }

    public List<Connection> connect(final Node from, final Node to, final Double weight) {
        final List<Connection> connections = from.connect(to, weight);
        final int bound = connections.size();
        for (int i = 0; i < bound; i++) {
            if (!from.equals(to)) {
                connections.add(connections.get(i));
            } else {
                this.selfConnections.add(connections.get(i));
            }
        }
        return connections;
    }

    public void disconnect(final Node from, final Node to) {
        final List<Connection> connections = from.equals(to) ? this.selfConnections : this.connections;

        for (int i = 0; i < connections.size(); i++) {
            final Connection connection = connections.get(i);
            if (connection.from.equals(from) && connection.to.equals(to)) {
                if (connection.gater != null) {
                    this.ungate(connection);
                }
                connections.remove(connection);
                break;
            }
        }
        from.disconnect(to);
    }

    public void gate(final Node node, final Connection connection) {
        if (this.nodes.indexOf(node) == -1) {
            throw new RuntimeException("This node is not part of the network!");
        } else if (connection.gater != null) {
            throw new RuntimeException("This connection is already gated!");
        }
        node.gate(connection);
        this.gates.add(connection);
    }

    public void ungate(final Connection connection) {
        final int index = this.gates.indexOf(connection);
        if (index == -1) {
            throw new RuntimeException("This connection is not gated!");
        }
        this.gates.remove(index);
        connection.gater.ungate(connection);
    }

    public void remove(final Node node) {
        final int index = this.nodes.indexOf(node);
        if (index == -1) {
            throw new RuntimeException("This node does not exist in the network!");
        }

        final List<Node> gaters = new ArrayList<>();

        this.disconnect(node, node);

        final List<Node> inputs = new ArrayList<>();

        for (int i = node.connections.in.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.in.get(i);
            if (Mutation.SUB_NODE.keepGates() && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            inputs.add(connection.from);
            this.disconnect(connection.from, node);
        }

        final List<Node> outputs = new ArrayList<>();
        for (int i = node.connections.out.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.out.get(i);
            if (Mutation.SUB_NODE.keepGates() && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            outputs.add(connection.to);
            this.disconnect(node, connection.to);
        }

        final ArrayList<Connection> connections = new ArrayList<>();
        for (final Node input : inputs) {
            for (final Node output : outputs) {
                if (!input.isProjectingTo(output)) {
                    connections.addAll(this.connect(input, output, null));
                }
            }
        }

        for (final Node gater : gaters) {
            if (connections.size() == 0) {
                break;
            }
            final int connectionIndex = Utils.randInt(connections.size());
            this.gate(gater, connections.get(connectionIndex));
            connections.remove(connectionIndex);
        }
        for (int i = node.connections.gated.size() - 1; i >= 0; i--) {
            this.ungate(node.connections.gated.get(i));
        }
        this.disconnect(node, node);
        this.nodes.remove(index);
    }

    public void mutate(final Mutation method) {
        if (method == Mutation.NULL) {
            throw new RuntimeException("No mutate method given!");
        }

        switch (method) {
            case ADD_NODE:
                final Connection connection = Utils.getRandomElem(this.connections);
                if (connection == null) {
                    throw new RuntimeException("No node can be added!");
                }
                final Node gater = connection.gater;
                this.disconnect(connection.from, connection.to);
                final int toIndex = this.nodes.indexOf(connection.to);
                Node node = new Node(NodeType.HIDDEN);
                node.mutate(Mutation.MOD_ACTIVATION);
                final int minBound = Math.min(toIndex, this.nodes.size() - this.output);
                this.nodes.add(minBound, node);

                final Connection newConnection1 = this.connect(connection.from, node).get(0);
                final Connection newConnection2 = this.connect(node, connection.to).get(0);
                if (gater != null) {
                    this.gate(gater, Utils.random.nextBoolean() ? newConnection1 : newConnection2);
                }
                break;
            case SUB_NODE:
                if (this.nodes.size() == this.input + this.output) {
                    throw new RuntimeException("No more nodes left to remove!");
                }
                final int index = (int) Math.floor(Math.random() * (this.nodes.size() - this.output - this.input) + this.input);
                this.remove(this.nodes.get(index));
                break;
            case ADD_CONN:
                final List<Node[]> available = new ArrayList<>();
                for (int i = 0; i < this.nodes.size() - this.output; i++) {
                    final Node node1 = this.nodes.get(i);
                    for (int j = Math.max(i + 1, this.input); j < this.nodes.size(); j++) {
                        final Node node2 = this.nodes.get(j);
                        if (!node1.isProjectingTo(node2)) {
                            available.add(new Node[]{node1, node2});
                        }
                    }
                }
                if (available.size() == 0) {
                    throw new RuntimeException("No more connections to be made!");
                }
                final Node[] pair = available.get((int) Math.floor(Math.random() * available.size()));
                this.connect(pair[0], pair[1]);
                break;
            case SUB_CONN:
                final ArrayList<Connection> possible = new ArrayList<>();
                for (final Connection conn : this.connections) {
                    if (conn.from.connections.out.size() > 1 &&
                            conn.to.connections.in.size() > 1 &&
                            this.nodes.indexOf(conn.to) > this.nodes.indexOf(conn.from)) {
                        possible.add(conn);
                    }
                }
                if (possible.size() == 0) {
                    throw new RuntimeException("No connections to remove!");
                }
                final Connection randomConn = Utils.getRandomElem(possible);
                this.disconnect(randomConn.from, randomConn.to);
                break;
            case MOD_WEIGHT:
                final List<Connection> allConnections = new ArrayList<>(this.connections);
                allConnections.addAll(this.selfConnections);

                final Connection conn = allConnections.get((int) Math.floor(Math.random() * allConnections.size()));
                final double modification = Utils.randDouble(method.min(), method.max());
                conn.weight += modification;
                break;
            case MOD_BIAS:
                final int index1 = Utils.randInt(this.input, this.nodes.size());
                this.nodes.get(index1).mutate(method);
                break;
            case MOD_ACTIVATION:
                if (!method.mutateOutput() && this.input + this.output == this.nodes.size()) {
                    throw new RuntimeException("No nodes that allow mutation of activation function!");
                }
                final int index2 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput() ? 0 : this.output) - this.input) + this.input);
                this.nodes.get(index2).mutate(method);
                break;
            case ADD_SELF_CONN:
                final ArrayList<Node> poss = new ArrayList<>();
                for (int i = this.input; i < this.nodes.size(); i++) {
                    node = this.nodes.get(i);
                    if (node.connections.self.weight == 0) {
                        poss.add(node);
                    }
                }
                if (poss.size() == 0) {
                    throw new RuntimeException("No more self-connections to add!");
                }
                node = poss.get((int) Math.floor(Math.random() * poss.size()));
                this.connect(node, node);
                break;
            case SUB_SELF_CONN:
                if (this.selfConnections.size() == 0) {
                    throw new RuntimeException("No more self-connections to remove!");
                }

                final Connection conn1 = this.selfConnections.get((int) Math.floor(Math.random() * this.selfConnections.size()));
                this.disconnect(conn1.from, conn1.to);
                break;
            case ADD_GATE:
                final List<Connection> allConnections1 = new ArrayList<>(this.connections);
                allConnections1.addAll(this.selfConnections);

                final ArrayList<Connection> possible1 = new ArrayList<>();
                for (final Connection connection1 : allConnections1) {
                    if (connection1.gater == null) {
                        possible1.add(connection1);
                    }
                }
                if (possible1.size() == 0) {
                    throw new RuntimeException("No more connections to gate!");
                }

                final int index3 = (int) Math.floor(Math.random() * (this.nodes.size() - this.input) + this.input);
                this.gate(this.nodes.get(index3), possible1.get((int) Math.floor(Math.random() * possible1.size())));

                break;
            case SUB_GATE:
                if (this.gates.size() == 0) {
                    throw new RuntimeException("No more connections to ungate!");
                }

                final int index4 = (int) Math.floor(Math.random() * this.gates.size());
                final Connection gatedconn = this.gates.get(index4);

                this.ungate(gatedconn);
                break;
            case ADD_BACK_CONN:
                final ArrayList<Node[]> available1 = new ArrayList<>();
                for (int i = this.input; i < this.nodes.size(); i++) {
                    final Node node1 = this.nodes.get(i);
                    for (int j = this.input; j < i; j++) {
                        final Node node2 = this.nodes.get(j);
                        if (!node1.isProjectingTo(node2)) {
                            available1.add(new Node[]{node1, node2});
                        }
                    }
                }
                if (available1.size() == 0) {
                    throw new RuntimeException("No more connections to be made!");
                }
                final Node[] pair1 = available1.get((int) Math.floor(Math.random() * available1.size()));
                this.connect(pair1[0], pair1[1]);
                break;
            case SUB_BACK_CONN:
                final ArrayList<Connection> possible2 = new ArrayList<>();

                for (final Connection conn3 : this.connections) {
                    if (conn3.from.connections.out.size() > 1 &&
                            conn3.to.connections.in.size() > 1 &&
                            this.nodes.indexOf(conn3.from) > this.nodes.indexOf(conn3.to)) {
                        possible2.add(conn3);
                    }
                }

                if (possible2.size() == 0) {
                    throw new RuntimeException("No connections to remove!");
                }

                final Connection randomConn1 = possible2.get((int) Math.floor(Math.random() * possible2.size()));
                this.disconnect(randomConn1.from, randomConn1.to);
                break;
            case SWAP_NODES:
                if (method.mutateOutput() && this.nodes.size() - this.input < 2 ||
                        (!method.mutateOutput() && this.nodes.size() - this.input - this.output < 2)) {
                    throw new RuntimeException("No nodes that allow swapping of bias and activation function");
                }

                int index5 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput() ? 0 : this.output) - this.input) + this.input);
                final Node node1 = this.nodes.get(index5);
                index5 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput() ? 0 : this.output) - this.input) + this.input);
                final Node node2 = this.nodes.get(index5);

                final double biasTemp = node1.bias;
                final Activation squashTemp = node1.squash;

                node1.bias = node2.bias;
                node1.squash = node2.squash;
                node2.bias = biasTemp;
                node2.squash = squashTemp;
                break;
        }
    }

    public double train(DataSet[] set, TrainingOptions options) {
        if (set[0].input.length != this.input || set[0].output.length != this.output) {
            throw new RuntimeException("Dataset input/output size should be same as network input/output size!");
        }
        if (options == null) {
            options = new TrainingOptions();
        }

        double targetError = options.getTargetError(0.05);
        final Cost cost = options.getCost(Cost.MSE);
        final double baseRate = options.getRate(0.3);
        final double dropout = options.getDropout(0);
        final double momentum = options.getMomentum(0);
        final int batchSize = options.getBatchSize(1);
        final Rate ratePolicy = options.getRatePolicy(Rate.FIXED);

        if (batchSize > set.length) {
            throw new RuntimeException("Batch size must be smaller or equal to dataset length!");
        } else if (options.iterations == null && options.error == -1) {
            throw new RuntimeException("At least one of the following options must be specified: error, iterations");
        } else if (options.error == -1) {
            targetError = -1; // run until iterations
        } else if (options.iterations == null) {
            options.iterations = 0; // run until target error
        }
        this.dropout = dropout;
        DataSet[] trainSet = set;
        DataSet[] testSet = null;
        if (options.crossValidate) {
            final int numTrain = (int) Math.ceil((1 - options.crossValidateTestSize) * set.length);
            trainSet = Arrays.copyOfRange(set, 0, numTrain);
            testSet = Arrays.copyOfRange(set, numTrain, set.length);
        }
        double currentRate;
        int iteration = 0;
        double error = 1;
        while (error > targetError && (options.iterations == 0 || iteration < options.iterations)) {
            if (options.crossValidate && error <= options.crossValidateTestError) {
                break;
            }
            iteration++;
            currentRate = ratePolicy.getRate(baseRate, iteration);

            if (options.crossValidate) {
                error = this.trainSet(trainSet, batchSize, currentRate, momentum, cost);
                if (options.clear) {
                    this.clear();
                }

                error = this.test(testSet, cost);
                if (options.clear) {
                    this.clear();
                }
            } else {
                error = this.trainSet(set, batchSize, currentRate, momentum, cost);
                if (options.clear) {
                    this.clear();
                }
            }

            if (options.shuffle) {
                final List<DataSet> list = Arrays.stream(set).collect(Collectors.toList());
                Collections.shuffle(list);
                final DataSet[] newSet = new DataSet[set.length];
                for (int i = 0; i < list.size(); i++) {
                    newSet[i] = list.get(i);
                }
                set = newSet;
            }
            System.out.println("Iteration: " + iteration + "; Error: " + error + "; Rate: " + currentRate);

            if (options.schedule && iteration % options.scheduleIterations == 0) {
                options.scheduleError = error;
                options.scheduleIteration = iteration;
            }
        }
        if (options.clear) {
            this.clear();
        }

        if (dropout != 0) {
            for (final Node node : this.nodes) {
                if (node.type == NodeType.HIDDEN || node.type == NodeType.CONSTANT) {
                    node.mask = 1 - dropout;
                }
            }
        }
        return error;
    }

    double test(final DataSet[] testSet, Cost cost) {
        if (cost == null) {
            cost = Cost.MSE;
        }
        if (this.dropout != 0) {
            for (final Node node : this.nodes) {
                if (node.type == NodeType.HIDDEN || node.type == NodeType.CONSTANT) {
                    node.mask = 1 - this.dropout;
                }
            }
        }
        double error = 0;
        for (final DataSet dataSet : testSet) {
            final List<Double> input = Utils.toList(dataSet.input);
            final double[] target = dataSet.output;
            final List<Double> output = this.noTraceActivate(input);
            error += cost.run(target, output);
        }

        return error / testSet.length;
    }

    private double trainSet(final DataSet[] trainSet, final int batchSize, final double currentRate, final double momentum, final Cost cost) {
        double errorSum = 0;
        for (int i = 0; i < trainSet.length; i++) {
            final List<Double> input = Utils.toList(trainSet[i].input);
            final double[] target = trainSet[i].output;
            final boolean update = (i + 1) % batchSize == 0 || (i + 1) == trainSet.length;
            final List<Double> output = this.activate(input, true);
            this.propagate(currentRate, momentum, update, Utils.toList(target));
            errorSum += cost.run(target, output);
        }
        return errorSum / trainSet.length;
    }

    public JsonObject toJSON() {
        final JsonArray jsonNodes = new JsonArray();
        final JsonArray jsonConnections = new JsonArray();
        final JsonArray jsonGates = new JsonArray();
        final JsonArray jsonSelfConnections = new JsonArray();

        IntStream.range(0, this.nodes.size()).forEach(i -> this.nodes.get(i).index = i);

        this.nodes.stream().map(Node::toJSON).forEach(jsonNodes::add);
        this.connections.stream().map(Connection::toJSON).forEach(jsonConnections::add);
        this.gates.stream().map(Connection::toJSON).forEach(jsonGates::add);
        this.selfConnections.stream().map(Connection::toJSON).forEach(jsonSelfConnections::add);

        final JsonObject object = new JsonObject();
        object.addProperty("input", this.input);
        object.addProperty("output", this.output);
        object.addProperty("dropout", this.dropout);
        object.add("nodes", jsonNodes);
        object.add("connections", jsonConnections);
        object.add("gates", jsonGates);
        object.add("selfConnections", jsonSelfConnections);
        return object;
    }

    public void set(final Double biasValue, final Activation squashValue) {
        for (final Node node : this.nodes) {
            node.bias = biasValue == null ? node.bias : biasValue;
            node.squash = squashValue == null ? node.squash : squashValue;
        }
    }

    public double evolve(final DataSet[] set, final TrainingOptions options) {
        if (set[0].input.length != this.input || set[0].output.length != this.output) {
            throw new RuntimeException("Dataset input/output size should be same as network input/output size!");
        }

        double targetError = options.getTargetError(0.05);
        final double growth = options.getGrowth(0.0001);
        final Cost cost = options.getCost(Cost.MSE);
        final int amount = options.getAmount(1);

        if (options.iterations == null && options.error == -1) {
            throw new RuntimeException("At least one of the following options must be specified: error, iterations");
        } else if (options.error == -1) {
            targetError = -1;
        } else if (options.iterations == null) {
            options.iterations = 0;
        }


        options.network = this;
        final NEAT neat = new NEAT(this.input, this.output, options, set);

        double error = Integer.MIN_VALUE;
        double bestFitness = Integer.MIN_VALUE;
        Network bestGenome = null;

        while (error < -targetError && (options.iterations == 0 | neat.getGeneration() < options.iterations)) {
            final Network fittest = neat.evolve();
            final double fitness = fittest.score;
            error = fitness + (fittest.nodes.size() - fittest.input - fittest.output + fittest.connections.size() + fittest.gates.size()) * growth;
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestGenome = fittest;
            }
            System.out.println("Iteration: " + neat.getGeneration() + "; Fitness: " + fitness + "; Error: " + -error);
            if (options.schedule && neat.getGeneration() % options.scheduleIterations == 0) {
                options.scheduleFitness = fitness;
                options.scheduleError = -error;
                options.scheduleIteration = neat.getGeneration();
            }
        }

        if (bestGenome != null) {
            this.nodes = bestGenome.nodes;
            this.connections = bestGenome.connections;
            this.selfConnections = bestGenome.selfConnections;
            this.gates = bestGenome.gates;
            if (options.clear) {
                this.clear();
            }
        }
        return -error;
    }

    public Network copy() {
        return Network.fromJSON(this.toJSON());
    }
}
