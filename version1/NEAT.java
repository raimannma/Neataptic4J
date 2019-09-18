package version1;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class NEAT {
    private final boolean equal;
    private final boolean clear;
    private final int popSize;
    private final int elitism;
    private final double provenance;
    private final double mutationRate;
    private final int mutationAmount;
    private final int output;
    private final int input;
    private final int maxGates;
    private final int maxConns;
    private final int maxNodes;
    private final Mutation[] mutation;
    private final Crossover[] crossover;
    private final Selection selection;
    private final boolean fitnessPopulation;
    private final Network template;
    private final TrainingOptions options;
    private final DataSet[] set;
    private int generation;
    private ArrayList<Network> population;

    public NEAT(final int input, final int output, TrainingOptions options, final DataSet[] set) {
        this.options = options;
        this.set = set;
        this.input = input;
        this.output = output;
        options = options == null ? new TrainingOptions() : options;

        this.equal = options.getEqual(false);
        this.clear = options.getClear(false);
        this.popSize = options.getPopSize(50);
        this.elitism = options.getElitism(0);
        this.provenance = options.getProvinance(0);
        this.mutationRate = options.getMutationRate(0.3);
        this.mutationAmount = options.getMutationAmount(1);

        this.fitnessPopulation = options.getFitnessPopulation(false);

        this.selection = options.getSelection(Selection.POWER);
        this.crossover = options.getCrossover(new Crossover[]{
                Crossover.SINGLE_POINT,
                Crossover.TWO_POINT,
                Crossover.UNIFORM,
                Crossover.AVERAGE
        });
        this.mutation = options.getMutation(Mutation.FFW);

        this.template = options.getNetwork(null);

        this.maxNodes = Integer.MAX_VALUE;
        this.maxConns = Integer.MAX_VALUE;
        this.maxGates = Integer.MAX_VALUE;

        this.generation = 0;
        this.createPool(null);
    }

    public static Network crossOver(final Network network1, final Network network2, final boolean equal) {
        if (network1.input != network2.input || network1.output != network2.output) {
            throw new RuntimeException("Networks don't have the same input/output size!");
        }
        final Network offspring = new Network(network1.input, network1.output);
        offspring.connections = new ArrayList<>();
        offspring.nodes = new ArrayList<>();

        final double score1 = network1.score;
        final double score2 = network2.score;
        final int size;
        if (equal || score1 == score2) {
            final int max = Math.max(network1.nodes.size(), network2.nodes.size());
            final int min = Math.min(network1.nodes.size(), network2.nodes.size());
            size = (int) Math.floor(Math.random() * (max - min + 1) + 1);
        } else if (score1 > score2) {
            size = network1.nodes.size();
        } else {
            size = network2.nodes.size();
        }

        final int outputSize = network1.output;

        for (int i = 0; i < network1.nodes.size(); i++) {
            network1.nodes.get(i).index = i;
        }
        for (int i = 0; i < network2.nodes.size(); i++) {
            network2.nodes.get(i).index = i;
        }

        for (int i = 0; i < size; i++) {
            Node node;
            if (i < size - outputSize) {
                final double random = Math.random();
                node = random >= 0.5 ? network1.nodes.get(i) : network2.nodes.get(i);
                final Node other = random < 0.5 ? network1.nodes.get(i) : network2.nodes.get(i);
                if (node == null || node.type == NodeType.OUTPUT) {
                    node = other;
                }
            } else {
                if (Math.random() >= 0.5) {
                    node = network1.nodes.get(network1.nodes.size() + i - size);
                } else {
                    node = network2.nodes.get(network2.nodes.size() + i - size);
                }
            }

            final Node newNode = new Node(node.type);
            newNode.bias = node.bias;
            newNode.squash = node.squash;
            newNode.type = node.type;

            offspring.nodes.add(newNode);
        }
        final List<Connection> n1Connections = new ArrayList<>();
        final List<Connection> n2Connections = new ArrayList<>();

        network1.connections
                .forEach(conn -> n1Connections.set(Connection.getInnovationID(conn.from.index, conn.to.index), conn));
        network1.selfConnections
                .forEach(conn -> n1Connections.set(Connection.getInnovationID(conn.from.index, conn.to.index), conn));
        network2.connections
                .forEach(conn -> n1Connections.set(Connection.getInnovationID(conn.from.index, conn.to.index), conn));
        network2.selfConnections
                .forEach(conn -> n1Connections.set(Connection.getInnovationID(conn.from.index, conn.to.index), conn));

        final List<Connection> connections = new ArrayList<>();
        final List<Integer> keys1 = Network.getKeys(n1Connections);
        final List<Integer> keys2 = Network.getKeys(n2Connections);
        for (int i = keys1.size() - 1; i >= 0; i--) {
            if (n2Connections.get(keys1.get(i)) != null) {
                final Connection conn = Math.random() >= 0.5 ? n1Connections.get(keys1.get(i)) : n2Connections.get(keys1.get(i));
                connections.add(conn);
                n2Connections.set(keys1.get(1), null);
            } else if (score1 >= score2 || equal) {
                connections.add(n1Connections.get(keys1.get(1)));
            }
        }

        if (score2 >= score1 || equal) {
            keys2.stream()
                    .filter(index -> n2Connections.get(index) != null)
                    .map(n2Connections::get)
                    .forEach(connections::add);
        }
        for (final Connection connData : connections) {
            if (connData.to.index < size && connData.from.index < size) {
                final Node from = offspring.nodes.get(connData.from.index);
                final Node to = offspring.nodes.get(connData.to.index);
                final Connection conn = offspring.connect(from, to).get(0);

                conn.weight = connData.weight;
                assert connData.gater != null;
                if (connData.gater.index != -1 && connData.gater.index < size) {
                    offspring.gate(offspring.nodes.get(connData.gater.index), conn);
                }
            }
        }
        return offspring;
    }

    private void createPool(final Network network) {
        this.population = new ArrayList<>();
        for (int i = 0; i < this.popSize; i++) {
            final Network copy;
            if (this.template != null) {
                copy = network.copy();
            } else {
                copy = new Network(this.input, this.output);
            }
            copy.score = -1;
            this.population.add(copy);
        }
    }


    private void fitness(final Iterable<Network> population) {
        for (final Network network : population) {
            this.fitness(network);
        }
    }

    private double fitness(final Network genome) {
        final int amount = this.options.getAmount(1);
        final Cost cost = this.options.getCost(Cost.MSE);
        final double growth = this.options.getGrowth(0.0001);

        final double score = IntStream.range(0, amount).mapToDouble(i -> -genome.test(this.set, cost)).sum() -
                (genome.nodes.size() - genome.input - genome.output + genome.connections.size() + genome.gates.size()) * growth;
        genome.score = score / amount;
        return score / amount;
    }

    public int getGeneration() {
        return this.generation;
    }

    public Network evolve() {
        if (this.population.get(this.population.size() - 1).score == -1) {
            this.evaluate();
        }
        this.sort();
        final Network fittest = this.population.get(0).copy();
        fittest.score = this.population.get(0).score;

        final ArrayList<Network> newPopulation = new ArrayList<>();

        final List<Network> elitists = new ArrayList<>();
        for (int i = 0; i < this.elitism; i++) {
            elitists.add(this.population.get(i));
        }

        for (int i = 0; i < this.provenance; i++) {
            newPopulation.add(this.template.copy());
        }

        for (int i = 0; i < this.popSize - this.elitism - this.provenance; i++) {
            newPopulation.add(this.getOffspring());
        }
        this.population = newPopulation;
        this.mutate();
        this.population.addAll(elitists);
        for (final Network network : this.population) {
            network.score = -1;
        }
        this.generation++;
        return fittest;
    }

    private void sort() {
        this.population.sort(Comparator.comparingDouble(value -> value.score));
    }

    private void evaluate() {
        if (this.fitnessPopulation) {
            if (this.clear) {
                for (final Network network : this.population) {
                    network.clear();
                }
            }
            this.fitness(this.population);
        } else {
            for (final Network genome : this.population) {
                if (this.clear) {
                    genome.clear();
                }
                genome.score = this.fitness(genome);
            }
        }
    }

    private Network getOffspring() {
        return NEAT.crossOver(this.getParent(), this.getParent(), this.equal);
    }

    private Network getParent() {
        switch (this.selection) {
            case POWER:
                if (this.population.get(0).score < this.population.get(1).score) {
                    this.sort();
                }
                final int index = (int) Math.floor(Math.pow(Math.random(), this.selection.power()));
                return this.population.get(index);
            case FITNESS_PROPORTIONATE:
                double totalFitness = 0;
                double minimalFitness = 0;
                for (final Network genome : this.population) {
                    final double score = genome.score;
                    minimalFitness = score < minimalFitness ? score : minimalFitness;
                    totalFitness += score;
                }
                minimalFitness = Math.abs(minimalFitness);
                totalFitness += minimalFitness * this.population.size();

                final double random = Math.random() * totalFitness;
                double value = 0;
                for (final Network genome : this.population) {
                    value += genome.score + minimalFitness;
                    if (random < value) {
                        return genome;
                    }
                }
                return this.population.get((int) Math.floor(Math.random() * this.population.size()));
            case TOURNAMENT:
                if (this.selection.size() > this.popSize) {
                    throw new RuntimeException("Your tournament size should be lower than the population size, please change methods.selection.TOURNAMENT.size");
                }
                final ArrayList<Network> individuals = new ArrayList<>();
                for (int i = 0; i < this.selection.size(); i++) {
                    individuals.add(this.population.get((int) Math.floor(Math.random() * this.population.size())));
                }
                individuals.sort(Comparator.comparingDouble(o -> o.score));

                for (int i = 0; i < this.selection.size(); i++) {
                    if (Math.random() < this.selection.probability() || i == this.selection.size() - 1) {
                        return individuals.get(i);
                    }
                }
                break;
        }
        throw new RuntimeException("Should not end here!");
    }

    public String toJSON() {
        //TODO
        return "";
    }

    public void fromJSON(final String json) {
        //TODO
    }

    public Mutation selectMutationMethod(final Network genome) {
        final Mutation mutationMethod = this.mutation[(int) Math.floor(Math.random() * this.mutation.length)];
        if (mutationMethod == Mutation.ADD_NODE && genome.nodes.size() >= this.maxNodes) {
            throw new RuntimeException("maxNodes exceeded!");
        }
        if (mutationMethod == Mutation.ADD_CONN && genome.connections.size() >= this.maxConns) {
            throw new RuntimeException("maxConns exceeded!");
        }
        if (mutationMethod == Mutation.ADD_GATE && genome.connections.size() >= this.maxGates) {
            throw new RuntimeException("maxGates exceeded!");
        }
        return mutationMethod;
    }

    public void mutate() {
        for (final Network network : this.population) {
            if (Math.random() <= this.mutationRate) {
                for (int j = 0; j < this.mutationAmount; j++) {
                    network.mutate(this.selectMutationMethod(network));
                }
            }
        }
    }

    public Network getFittest() {
        if (this.population.get(this.population.size() - 1).score == -1) {
            this.evaluate();
        }
        this.sort();
        return this.population.get(0);
    }

    public double getAverage() {
        if (this.population.get(this.population.size() - 1).score == -1) {
            this.evaluate();
        }
        return this.population.stream().mapToDouble(network -> network.score).average().orElseThrow();
    }
}
