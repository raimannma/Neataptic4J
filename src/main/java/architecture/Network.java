package architecture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import methods.Activation;
import methods.Cost;
import methods.MutationType;
import methods.Rate;

import java.util.*;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static methods.MutationType.MOD_ACTIVATION;
import static methods.MutationType.SUB_NODE;

public class Network {
    int input;
    int output;
    List<Connection> gates;
    double score;
    List<Node> nodes;
    List<Connection> connections;
    List<Connection> selfConns;
    private double dropout;

    public Network(final int input, final int output) {
        this.input = input;
        this.output = output;

        this.score = Double.NaN;

        this.nodes = new ArrayList<>();
        this.connections = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.selfConns = new ArrayList<>();

        this.dropout = 0;
        IntStream.range(0, this.input + this.output)
                .mapToObj(i -> new Node(i < input ? Node.NodeType.INPUT : Node.NodeType.OUTPUT))
                .forEach(this.nodes::add);
        for (int i = 0; i < this.input; i++) {
            for (int j = this.input; j < this.output + this.input; j++) {
                this.connect(this.nodes.get(i),
                        this.nodes.get(j),
                        Math.random() * this.input * Math.sqrt((double) 2 / this.input));
            }
        }
    }

    private List<Connection> connect(final Node from, final Node to, final double weight) {
        final List<Connection> connections = from.connect(to, weight);
        connections.forEach(connection -> {
            if (!from.equals(to)) {
                this.connections.add(connection);
            } else {
                this.selfConns.add(connection);
            }
        });
        return connections;
    }

    static Network merge(Network network1, Network network2) {
        network1 = Network.fromJSON(network1.toJSON());
        network2 = Network.fromJSON(network2.toJSON());

        if (network1.output != network2.input) {
            throw new RuntimeException("Output size of network1 should be the same as the input size of network2!");
        }

        for (int i = 0; i < network2.connections.size(); i++) {
            final Connection conn = network2.connections.get(i);
            if (conn.from.type == Node.NodeType.INPUT) {
                final int index = network2.nodes.indexOf(conn.from);
                conn.from = network1.nodes.get(network1.nodes.size() - 1 - index);
            }
        }
        if (network2.input > 0) {
            network2.nodes.subList(0, network2.input).clear();
        }
        for (int i = network1.nodes.size() - network1.output; i < network1.nodes.size(); i++) {
            network1.nodes.get(i).type = Node.NodeType.HIDDEN;
        }

        network1.connections.addAll(network2.connections);
        network1.nodes.addAll(network2.nodes);
        return network1;
    }

    static Network fromJSON(final JsonObject json) {
        final Network network = new Network(json.get("input").getAsInt(), json.get("output").getAsInt());
        network.dropout = json.get("dropout").getAsDouble();
        network.nodes = new ArrayList<>();
        network.connections = new ArrayList<>();

        final JsonArray nodes = json.get("nodes").getAsJsonArray();
        final JsonArray connections = json.get("connections").getAsJsonArray();
        for (int i = 0; i < nodes.size(); i++) {
            network.nodes.add(Node.fromJSON(nodes.get(i).getAsJsonObject()));
        }
        for (int i = 0; i < connections.size(); i++) {
            final JsonObject connJSON = connections.get(i).getAsJsonObject();
            final List<Connection> connection = network.connect(network.nodes.get(connJSON.get("from").getAsInt()), network.nodes.get(connJSON.get("to").getAsInt()));
            connection.get(0).weight = connJSON.get("weight").getAsDouble();

            if (connJSON.has("gater") && connJSON.get("gater").getAsInt() != -1) {
                network.gate(network.nodes.get(connJSON.get("gater").getAsInt()), connection.get(0));
            }
        }
        return network;
    }

    JsonObject toJSON() {
        final JsonObject json = new JsonObject();
        json.addProperty("input", this.input);
        json.addProperty("output", this.output);
        json.addProperty("dropout", this.dropout);
        final JsonArray nodes = new JsonArray();
        final JsonArray connections = new JsonArray();

        IntStream.range(0, this.nodes.size()).forEach(i -> this.nodes.get(i).index = i);
        for (int i = 0; i < this.nodes.size(); i++) {
            final Node node = this.nodes.get(i);
            final JsonObject nodeJSON = node.toJSON();
            nodeJSON.addProperty("index", i);
            nodes.add(nodeJSON);

            if (node.connections.self.weight != 0) {
                final JsonObject connectionJSON = node.connections.self.toJSON();
                connectionJSON.addProperty("from", i);
                connectionJSON.addProperty("to", i);

                if (node.connections.self.gater == null) {
                    connectionJSON.addProperty("gater", -1);
                } else {
                    connectionJSON.addProperty("gater", node.connections.self.gater.index);
                }
                connections.add(connectionJSON);
            }
        }

        for (final Connection connection : this.connections) {
            final JsonObject toJSON = connection.toJSON();
            toJSON.addProperty("from", connection.from.index);
            toJSON.addProperty("to", connection.to.index);
            toJSON.addProperty("gater", connection.gater != null ? connection.gater.index : -1);
            connections.add(toJSON);
        }

        json.add("nodes", nodes);
        json.add("connections", connections);
        return json;
    }

    private List<Connection> connect(final Node from, final Node to) {
        return this.connect(from, to, 0);
    }

    private void gate(final Node node, final Connection connection) {
        if (this.nodes.indexOf(node) == -1) {
            throw new RuntimeException("This node is not part of the network!");
        } else if (connection.gater != null) {
            return;
        }
        node.gate(connection);
        this.gates.add(connection);
    }

    static Network crossover(final Network network1, final Network network2, final boolean equal) {
        if (network1.input != network2.input || network1.output != network2.output) {
            throw new RuntimeException("Networks don't have the same input/output size!");
        }

        final Network offspring = new Network(network1.input, network1.output);
        offspring.connections = new ArrayList<>();
        offspring.nodes = new ArrayList<>();

        final double score1 = Double.isNaN(network1.score) ? 0 : network1.score;
        final double score2 = Double.isNaN(network2.score) ? 0 : network2.score;

        final int size;
        if (equal || score1 == score2) {
            final int max = Math.max(network1.nodes.size(), network2.nodes.size());
            final int min = Math.min(network1.nodes.size(), network2.nodes.size());
            size = (int) Math.floor(Math.random() * (max - min + 1) + min);
        } else if (score1 > score2) {
            size = network1.nodes.size();
        } else {
            size = network2.nodes.size();
        }

        final int outputSize = network1.output;
        IntStream.range(0, network1.nodes.size()).forEach(i -> network1.nodes.get(i).index = i);
        IntStream.range(0, network2.nodes.size()).forEach(i -> network2.nodes.get(i).index = i);

        for (int i = 0; i < size; i++) {
            Node node;
            final Node other;
            if (i < size - outputSize) {
                final double random = Math.random();
                node = random >= 0.5 ?
                        i >= network1.nodes.size() ? null : network1.nodes.get(i) :
                        i >= network2.nodes.size() ? null : network2.nodes.get(i);
                other = random < 0.5 ?
                        i >= network1.nodes.size() ? null : network1.nodes.get(i) :
                        i >= network2.nodes.size() ? null : network2.nodes.get(i);
                if (node == null || node.type == Node.NodeType.OUTPUT) {
                    node = other;
                    if (other == null) {
                        throw new RuntimeException();
                    }
                }
            } else {
                node = Math.random() >= 0.5 ?
                        network1.nodes.get(network1.nodes.size() + i - size) :
                        network2.nodes.get(network2.nodes.size() + i - size);
            }

            final Node newNode = new Node();
            newNode.bias = node.bias;
            newNode.squash = node.squash;
            newNode.type = node.type;
            offspring.nodes.add(newNode);
        }

        final Map<Integer, JsonObject> n1Conns = new HashMap<>();
        final Map<Integer, JsonObject> n2Conns = new HashMap<>();

        getConnectionsData(network1, n1Conns);
        getConnectionsData(network2, n2Conns);

        final List<JsonObject> connections = new ArrayList<>();
        final List<Integer> keys1 = new ArrayList<>(n1Conns.keySet());
        final List<Integer> keys2 = new ArrayList<>(n2Conns.keySet());

        for (int i = keys1.size() - 1; i >= 0; i--) {
            if (n2Conns.get(keys1.get(i)) != null) {
                final JsonObject connectionJSON = Math.random() >= 0.5 ? n1Conns.get(keys1.get(i)) : n2Conns.get(keys1.get(i));
                connections.add(connectionJSON);

                n2Conns.put(keys1.get(i), null);
            } else if (score1 >= score2 || equal) {
                connections.add(n1Conns.get(keys1.get(i)));
            }
        }

        if (score2 >= score1 || equal) {
            keys2.stream()
                    .filter(integer -> n2Conns.get(integer) != null)
                    .map(n2Conns::get)
                    .forEach(connections::add);
        }

        for (final JsonObject connData : connections) {
            if (connData.get("to").getAsInt() < size && connData.get("from").getAsInt() < size) {
                final Node from = offspring.nodes.get(connData.get("from").getAsInt());
                final Node to = offspring.nodes.get(connData.get("to").getAsInt());
                final Connection connection = offspring.connect(from, to).get(0);
                connection.weight = connData.get("weight").getAsDouble();
                if (connData.get("gater").getAsInt() != -1 && connData.get("gater").getAsInt() < size) {
                    offspring.gate(offspring.nodes.get(connData.get("gater").getAsInt()), connection);
                }
            }
        }
        return offspring;
    }

    private static void getConnectionsData(Network network, Map<Integer, JsonObject> conns) {
        for (int i = 0; i < network.connections.size(); i++) {
            final Connection connection = network.connections.get(i);
            final JsonObject data = new JsonObject();
            data.addProperty("weight", connection.weight);
            data.addProperty("from", connection.from.index);
            data.addProperty("to", connection.to.index);
            data.addProperty("gater", connection.gater != null ? connection.gater.index : -1);
            conns.put(Connection.getInnovationID(connection.from.index, connection.to.index), data);
        }
        for (int i = 0; i < network.selfConns.size(); i++) {
            final Connection connection = network.selfConns.get(i);
            final JsonObject data = new JsonObject();
            data.addProperty("weight", connection.weight);
            data.addProperty("from", connection.from.index);
            data.addProperty("to", connection.to.index);
            data.addProperty("gater", connection.gater != null ? connection.gater.index : -1);
            conns.put(Connection.getInnovationID(connection.from.index, connection.to.index), data);
        }
    }

    @Override
    public String toString() {
        return "Network{" +
                "input=" + this.input +
                ", output=" + this.output +
                ", gates=" + this.gates +
                ", nodes=" + this.nodes +
                ", connections=" + this.connections +
                ", selfConns=" + this.selfConns +
                '}';
    }

    public double evolve(final DataEntry[] set) {
        return this.evolve(set, new EvolveOptions());
    }

    public double evolve(final DataEntry[] set, final EvolveOptions options) {
        if (options == null) {
            return this.evolve(set);
        }
        if (set[0].input.length != this.input || set[0].output.length != this.output) {
            throw new RuntimeException("Dataset input/output size should be same as network input/output size!");
        }

        double targetError = Double.isNaN(options.getError()) ? 0.05 : options.getError();
        final double growth = options.getGrowth();
        final Cost cost = options.getCost();
        final int amount = options.getAmount();
        if (options.getIterations() == -1 && Double.isNaN(options.getError())) {
            throw new RuntimeException("At least one of the following options must be specified: error, iterations");
        } else if (Double.isNaN(options.getError())) {
            targetError = -1;
        } else if (options.getIterations() == -1) {
            options.setIterations(0);
        }

        final ToDoubleFunction<Network> fitnesFunction = genome -> {
            double score = IntStream.range(0, amount)
                    .mapToDouble(i -> -genome.test(Arrays.asList(set), cost))
                    .sum() -
                    growth * (genome.nodes.size() -
                            genome.input -
                            genome.output +
                            genome.connections.size() +
                            genome.gates.size()
                    );
            score = Double.isNaN(score) ? -Double.MAX_VALUE : score;
            return score / amount;
        };
        options.setNetwork(this);
        final NEAT neat = new NEAT(this.input, this.output, fitnesFunction, options);

        double error = -Double.MAX_VALUE;
        double bestFitness = -Double.MAX_VALUE;
        Network bestGenome = null;

        while (error < -targetError && (options.getIterations() == 0 || neat.generation < options.getIterations())) {
            final Network fittest = neat.evolve();
            final double fitness = fittest.score;
            error = fitness + (fittest.nodes.size() - fittest.input - fittest.output + fittest.connections.size() + fittest.gates.size()) * growth;
            if (fitness > bestFitness) {
                bestFitness = fitness;
                bestGenome = fittest;
            }
            System.out.println("Iteration: " + neat.generation + "; Fitness: " + fitness + "; Error: " + -error);
        }

        if (bestGenome != null) {
            this.nodes = bestGenome.nodes;
            this.connections = bestGenome.connections;
            this.selfConns = bestGenome.selfConns;
            this.gates = bestGenome.gates;
            if (options.isClear()) {
                this.clear();
            }
        }
        return -error;
    }

    double test(final List<DataEntry> testSet) {
        return this.test(testSet, Cost.MSE);
    }

    private double test(final List<DataEntry> set, final Cost cost) {
        if (cost == null) {
            return this.test(set);
        }
        if (this.dropout != 0) {
            this.nodes.stream()
                    .filter(node -> node.type == Node.NodeType.HIDDEN || node.type == Node.NodeType.CONSTANT)
                    .forEach(node -> node.mask = 1 - this.dropout);
        }
        double error = 0;
        for (final DataEntry dataEntry : set) {
            final double[] input = dataEntry.input;
            final double[] target = dataEntry.output;
            final double[] output = this.noTraceActivate(input);
            error += cost.calc(target, output);
        }
        return error / set.size();
    }

    private double[] noTraceActivate(final double[] input) {
        final List<Double> output = new ArrayList<>();
        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == Node.NodeType.INPUT) {
                this.nodes.get(i).noTraceActivation(input[i]);
            } else if (this.nodes.get(i).type == Node.NodeType.OUTPUT) {
                output.add(this.nodes.get(i).noTraceActivation());
            } else {
                this.nodes.get(i).noTraceActivation();
            }
        }
        return output.stream().mapToDouble(i -> i).toArray();
    }

    void mutate(final MutationType method) {
        if (method == null) {
            throw new RuntimeException("No (correct) mutate method given!");
        }

        switch (method) {
            case ADD_NODE:
                final Connection connection = this.connections.get((int) Math.floor(Math.random() * this.connections.size()));
                final Node gater = connection.gater;
                this.disconnect(connection.from, connection.to);

                final int toIndex = this.nodes.indexOf(connection.to);
                final Node node = new Node(Node.NodeType.HIDDEN);

                node.mutate(MOD_ACTIVATION);
                final int minBound = Math.min(toIndex, this.nodes.size() - this.output);
                this.nodes.add(minBound, node);

                final Connection newConn1 = this.connect(connection.from, node).get(0);
                final Connection newConn2 = this.connect(node, connection.to).get(0);

                if (gater != null) {
                    this.gate(gater, Math.random() >= 0.5 ? newConn1 : newConn2);
                }
                break;
            case SUB_NODE:
                if (this.nodes.size() == this.input + this.output) {
                    break;
                }

                final int index = (int) Math.floor(Math.random() * (this.nodes.size() - this.output - this.input) + this.input);
                this.remove(this.nodes.get(index));
                break;
            case ADD_CONN:
                final List<Node[]> available = new ArrayList<>();
                IntStream.range(0, this.nodes.size() - this.output)
                        .forEach(i -> {
                            final Node node1 = this.nodes.get(i);
                            IntStream.range(Math.max(i + 1, this.input), this.nodes.size())
                                    .mapToObj(this.nodes::get)
                                    .filter(node2 -> !node1.isProjectingTo(node2))
                                    .map(node2 -> new Node[]{node1, node2})
                                    .forEach(available::add);
                        });
                if (available.size() == 0) {
                    break;
                }
                final Node[] pair = available.get((int) Math.floor(Math.random() * available.size()));
                this.connect(pair[0], pair[1]);
                break;
            case SUB_CONN:
                final List<Connection> possible = new ArrayList<>();
                for (final Connection conn : this.connections) {
                    if (conn.from.connections.out.size() > 1 && conn.to.connections.in.size() > 1 &&
                            this.nodes.indexOf(conn.to) > this.nodes.indexOf(conn.from)) {
                        possible.add(conn);
                    }
                }
                if (possible.size() == 0) {
                    break;
                }
                final Connection randomConn = possible.get((int) Math.floor(Math.random() * possible.size()));
                this.disconnect(randomConn.from, randomConn.to);
                break;
            case MOD_WEIGHT:
                final List<Connection> allConnections = new ArrayList<>(this.connections);
                allConnections.addAll(this.selfConns);

                final Connection connection1 = allConnections.get((int) Math.floor(Math.random() * allConnections.size()));
                final double modification = Math.random() * (method.max - method.min) + method.min;
                connection1.weight += modification;
                break;
            case MOD_BIAS:
                final int index1 = (int) Math.floor(Math.random() * (this.nodes.size() - this.input) + this.input);
                this.nodes.get(index1).mutate(method);
                break;
            case MOD_ACTIVATION:
                if (!method.mutateOutput && this.input + this.output == this.nodes.size()) {
                    break;
                }
                final int ix = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                this.nodes.get(ix).mutate(method);
                break;
            case ADD_SELF_CONN:
                final List<Node> poss = IntStream.range(this.input, this.nodes.size()).mapToObj(this.nodes::get).filter(node1 -> node1.connections.self.weight == 0).collect(Collectors.toList());
                if (poss.size() == 0) {
                    break;
                }
                final Node node1 = poss.get((int) Math.floor(Math.random() * poss.size()));
                this.connect(node1, node1);
                break;
            case SUB_SELF_CONN:
                if (this.selfConns.size() == 0) {
                    break;
                }
                final Connection connection2 = this.selfConns.get((int) Math.floor(Math.random() * this.selfConns.size()));
                this.disconnect(connection2.from, connection2.to);
                break;
            case ADD_GATE:
                final List<Connection> allConnections1 = new ArrayList<>(this.connections);
                allConnections1.addAll(this.selfConns);

                final List<Connection> possible1 = allConnections1.stream().filter(connection3 -> connection3.gater == null).collect(Collectors.toList());
                if (possible1.size() == 0) {
                    break;
                }

                final int index2 = (int) Math.floor(Math.random() * (this.nodes.size() - this.input) + this.input);
                this.gate(this.nodes.get(index2), possible1.get((int) Math.floor(Math.random() * possible1.size())));
                break;
            case SUB_GATE:
                if (this.gates.size() == 0) {
                    break;
                }
                final int index3 = (int) Math.floor(Math.random() * this.gates.size());
                this.ungate(this.gates.get(index3));
                break;
            case ADD_BACK_CONN:
                final List<Node[]> available1 = new ArrayList<>();
                IntStream.range(this.input, this.nodes.size())
                        .forEach(i -> {
                            final Node node2 = this.nodes.get(i);
                            IntStream.range(this.input, i)
                                    .mapToObj(this.nodes::get)
                                    .filter(node3 -> !node2.isProjectingTo(node3))
                                    .map(node3 -> new Node[]{node2, node3})
                                    .forEach(available1::add);
                        });

                if (available1.size() == 0) {
                    break;
                }

                final Node[] pair1 = available1.get((int) Math.floor(Math.random() * available1.size()));
                this.connect(pair1[0], pair1[1]);
                break;
            case SUB_BACK_CONN:
                final List<Connection> possible2 = this.connections.stream().filter(connection3 -> connection3.from.connections.out.size() > 1 &&
                        connection3.to.connections.in.size() > 1 &&
                        this.nodes.indexOf(connection3.from) > this.nodes.indexOf(connection3.to)).collect(Collectors.toList());
                if (possible2.size() == 0) {
                    break;
                }
                final Connection randomConn1 = possible2.get((int) Math.floor(Math.random() * possible2.size()));
                this.disconnect(randomConn1.from, randomConn1.to);
                break;
            case SWAP_NODES:
                if ((method.mutateOutput && this.nodes.size() - this.input < 2) ||
                        (!method.mutateOutput && this.nodes.size() - this.input - this.output < 2)) {
                    break;
                }
                int index4 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                final Node node3 = this.nodes.get(index4);
                index4 = (int) Math.floor(Math.random() * (this.nodes.size() - (method.mutateOutput ? 0 : this.output) - this.input) + this.input);
                final Node node2 = this.nodes.get(index4);

                final double biasTemp = node3.bias;
                final Activation squashTemp = node3.squash;

                node3.bias = node2.bias;
                node3.squash = node2.squash;
                node2.bias = biasTemp;
                node2.squash = squashTemp;
                break;
        }
    }

    private void disconnect(final Node from, final Node to) {
        final List<Connection> connections = from.equals(to) ? this.selfConns : this.connections;
        for (int i = 0; i < connections.size(); i++) {
            final Connection connection = connections.get(i);
            if (connection.from.equals(from) && connection.to.equals(to)) {
                if (connection.gater != null) {
                    this.ungate(connection);
                }
                connections.remove(i);
                break;
            }
        }
        from.disconnect(to);
    }

    private void remove(final Node node) {
        final int index = this.nodes.indexOf(node);
        if (index == -1) {
            throw new RuntimeException("This node does not exist in the network!");
        }

        final List<Node> gaters = new ArrayList<>();
        this.disconnect(node, node);

        final List<Node> inputs = new ArrayList<>();
        for (int i = node.connections.in.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.in.get(i);
            if (SUB_NODE.keepGates && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            inputs.add(connection.from);
            this.disconnect(connection.from, node);
        }
        final List<Node> outputs = new ArrayList<>();
        for (int i = node.connections.out.size() - 1; i >= 0; i--) {
            final Connection connection = node.connections.out.get(i);
            if (SUB_NODE.keepGates && connection.gater != null && !connection.gater.equals(node)) {
                gaters.add(connection.gater);
            }
            outputs.add(connection.to);
            this.disconnect(node, connection.to);
        }

        final List<Connection> connections = new ArrayList<>();
        for (final Node input : inputs) {
            for (final Node output : outputs) {
                if (!input.isProjectingTo(output)) {
                    connections.add(this.connect(input, output, 0).get(0));
                }
            }
        }

        for (final Node gater : gaters) {
            if (connections.size() == 0) {
                break;
            }
            final int connIndex = (int) Math.floor(Math.random() * connections.size());
            this.gate(gater, connections.get(connIndex));
            connections.remove(connIndex);
        }
        for (int i = node.connections.gated.size() - 1; i >= 0; i--) {
            this.ungate(node.connections.gated.get(i));
        }

        this.disconnect(node, node);
        this.nodes.remove(index);
    }

    private void ungate(final Connection connection) {
        final int index = this.gates.indexOf(connection);
        if (index == -1) {
            throw new RuntimeException("This connection is not gated!");
        }
        this.gates.remove(index);
        connection.gater.ungate(connection);
    }

    void set(final Node.NodeValues values) {
        this.nodes.forEach(node -> {
            node.bias = values.bias == null ? node.bias : values.bias;
            node.squash = values.squash == null ? node.squash : values.squash;
        });
    }

    public double train(final DataEntry[] set) {
        return this.train(set, new TrainOptions());
    }

    public double train(final DataEntry[] set, final TrainOptions options) {
        if (set[0].input.length != this.input || set[0].output.length != this.output) {
            throw new RuntimeException("Dataset input/output size should be same as network input/output size!" + System.lineSeparator() +
                    set[0].input.length + " - " + this.input + ";" + set[0].output.length + " - " + this.output);
        }

        final double targetError = options.getError();
        final Cost cost = options.getCost();
        final double baseRate = options.getRate();
        final double dropout = options.getDropout();
        final double momentum = options.getMomentum();
        final int batchSize = options.getBatchSize();
        final Rate ratePolicy = options.getRatePolicy();

        if (batchSize > set.length) {
            throw new RuntimeException("Batch size must be smaller or equal to dataset length!");
        } else if (options.getIterations() == -1 && Double.isNaN(options.getError())) {
            throw new RuntimeException("At least one of the following options must be specified: error, iterations");
        } else if (options.getIterations() == -1) {
            options.setIterations(0);
        }

        this.dropout = dropout;
        List<DataEntry> trainSet = Arrays.asList(set);
        List<DataEntry> testSet = null;

        if (options.isCrossValidate()) {
            final int numTrain = (int) Math.ceil((1 - options.getCrossValidateTestSize()) * set.length);
            testSet = trainSet.subList(numTrain, trainSet.size());
            trainSet = trainSet.subList(0, numTrain);
        }

        double currentRate;
        int iteration = 0;
        double error = 1;

        while (error > targetError && (options.getIterations() == 0 || iteration < options.getIterations())) {
            if (options.isCrossValidate() && error <= options.getCrossValidateTestError()) {
                break;
            }
            iteration++;

            currentRate = ratePolicy.calc(baseRate, iteration);

            if (options.isCrossValidate()) {
                this._trainSet(trainSet, batchSize, currentRate, momentum, cost);
                if (options.isClear()) {
                    this.clear();
                }
                error = this.test(testSet, cost);
                if (options.isClear()) {
                    this.clear();
                }
            } else {
                error = this._trainSet(trainSet, batchSize, currentRate, momentum, cost);
                if (options.isClear()) {
                    this.clear();
                }
            }
            if (options.isShuffle()) {
                Collections.shuffle(trainSet);
            }

            System.out.println("Iteration: " + iteration + "; Error: " + error + "; Rate: " + currentRate);
        }
        if (options.isClear()) {
            this.clear();
        }
        if (dropout != 0) {
            this.nodes.stream()
                    .filter(node -> node.type == Node.NodeType.HIDDEN || node.type == Node.NodeType.CONSTANT)
                    .forEach(node -> node.mask = 1 - dropout);
        }
        return error;
    }

    private double _trainSet(final List<DataEntry> set, final int batchSize, final double currentRate, final double momentum, final Cost cost) {
        double errorSum = 0;
        for (int i = 0; i < set.size(); i++) {
            final double[] input = set.get(i).input;
            final double[] target = set.get(i).output;

            final boolean update = (i + 1) % batchSize == 0 || (i + 1) == set.size();

            final double[] output = this.activate(input, true);
            this.propagate(currentRate, momentum, update, target);
            errorSum += cost.calc(target, output);
        }
        return errorSum / set.size();
    }

    void clear() {
        this.nodes.forEach(Node::clear);
    }

    private void propagate(final double rate, final double momentum, final boolean update, final double[] target) {
        if (target == null || target.length != this.output) {
            throw new RuntimeException("Output target length should match network output length");
        }

        int targetIndex = target.length;
        for (int i = this.nodes.size() - 1; i >= this.nodes.size() - this.output; i--) {
            this.nodes.get(i).propagate(rate, momentum, update, target[--targetIndex]);
        }
        for (int i = this.nodes.size() - this.output - 1; i >= this.input; i--) {
            this.nodes.get(i).propagate(rate, momentum, update);
        }
    }

    public double[] activate(final double[] input) {
        return this.activate(input, false);
    }

    public double[] activate(final double[] input, final boolean training) {
        final List<Double> output = new ArrayList<>();

        for (int i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).type == Node.NodeType.INPUT) {
                this.nodes.get(i).activate(input[i]);
            } else if (this.nodes.get(i).type == Node.NodeType.OUTPUT) {
                output.add(this.nodes.get(i).activate());
            } else {
                if (training) {
                    this.nodes.get(i).mask = Math.random() < this.dropout ? 0 : 1;
                }
                this.nodes.get(i).activate();
            }
        }
        return output.stream().mapToDouble(i -> i).toArray();
    }
}
