package architecture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import methods.CrossOverType;
import methods.MutationType;
import methods.SelectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class NEAT {
    private final int output;
    private final int input;
    private final ToDoubleFunction<Network> fitness;
    private final boolean equal;
    private final boolean clear;
    private final double mutationRate;
    private final int mutationAmount;
    private final int provenance;
    private final int elitism;
    private final boolean fitnessPopulation;
    private final int maxGates;
    private final int maxConns;
    private final int maxNodes;
    private final Network template;
    private final MutationType[] mutation;
    private final CrossOverType[] crossOver;
    private final SelectionType selection;
    int generation;
    private int popSize;
    private List<Network> population;

    NEAT(final int input, final int output, final ToDoubleFunction<Network> fitnesFunction, final EvolveOptions options) {
        this.input = input;
        this.output = output;
        this.fitness = fitnesFunction;

        this.equal = options.isEqual();
        this.clear = options.getClear();

        this.popSize = options.getPopulationSize();
        this.elitism = options.getElitism();
        this.provenance = options.getProvenance();
        this.mutationRate = options.getMutationRate();
        this.mutationAmount = options.getMutationAmount();

        this.fitnessPopulation = options.isFitnessPopulation();

        this.selection = options.getSelection();
        this.crossOver = options.getCrossOverTypes();
        this.mutation = options.getMutationTypes();

        this.template = options.getTemplate();

        this.maxNodes = options.getMaxNodes();
        this.maxConns = options.getMaxConns();
        this.maxGates = options.getMaxGates();

        this.generation = 0;

        this.createPool(this.template);
    }

    private void createPool(final Network template) {
        this.population = new ArrayList<>();
        for (int i = 0; i < this.popSize; i++) {
            final Network copy = template != null ? Network.fromJSON(template.toJSON()) : new Network(this.input, this.output);
            copy.score = Double.NaN;
            this.population.add(copy);
        }
    }

    Network evolve() {
        if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
            this.evaluate();
        }
        this.sort();
        final Network fittest = Network.fromJSON(this.population.get(0).toJSON());
        fittest.score = this.population.get(0).score;

        final List<Network> elitists = IntStream.range(0, this.elitism)
                .mapToObj(i -> this.population.get(i))
                .collect(Collectors.toList());

        final List<Network> newPopulation = IntStream.range(0, this.provenance)
                .mapToObj(i -> Network.fromJSON(this.template.toJSON()))
                .collect(Collectors.toList());

        IntStream.range(0, this.popSize - this.elitism - this.provenance)
                .mapToObj(i -> this.getOffspring())
                .forEach(newPopulation::add);

        this.population = newPopulation;
        this.mutate();

        this.population.addAll(elitists);

        this.population.forEach(network -> network.score = Double.NaN);

        this.generation++;
        return fittest;
    }

    private void evaluate() {
        if (this.fitnessPopulation) {
            if (this.clear) {
                this.population.forEach(Network::clear);
            }
            this.population
                    .parallelStream()
                    .forEach(value -> value.score = this.fitness.applyAsDouble(value));
        } else {
            this.population
                    .parallelStream()
                    .forEach(genome -> {
                        if (this.clear) {
                            genome.clear();
                        }
                        genome.score = this.fitness.applyAsDouble(genome);
                    });
        }
    }

    private void sort() {
        this.population.sort((o1, o2) -> Double.compare(o2.score, o1.score));
    }

    private Network getOffspring() {
        return Network.crossover(this.getParent(), this.getParent(), this.equal);
    }

    private void mutate() {
        this.population.stream()
                .parallel()
                .filter(network -> Math.random() <= this.mutationRate)
                .forEach(network -> IntStream.range(0, this.mutationAmount)
                        .forEach(j -> network.mutate(this.selectMutationMethod(network))));
    }

    private Network getParent() {
        switch (this.selection) {
            case POWER:
                if (this.population.get(0).score < this.population.get(1).score) {
                    this.sort();
                }
                return this.population.get((int) Math.floor(Math.pow(Math.random(), this.selection.power) * this.population.size()));
            case FITNESS_PROPORTIONATE:
                double totalFitness = 0;
                double minimalFitness = 0;
                for (final Network network : this.population) {
                    final double score = network.score;
                    minimalFitness = Math.min(score, minimalFitness);
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
                if (this.selection.size > this.popSize) {
                    throw new RuntimeException("Your tournament size should be lower than the population size, please change methods.selection.TOURNAMENT.size");
                }
                final List<Network> individuals = new ArrayList<>();
                for (int i = 0; i < this.selection.size; i++) {
                    individuals.add(this.population.get((int) Math.floor(Math.random() * this.population.size())));
                }
                individuals.sort((o1, o2) -> Double.compare(o2.score, o1.score));

                for (int i = 0; i < this.selection.size; i++) {
                    if (Math.random() < this.selection.probability || i == this.selection.size - 1) {
                        return individuals.get(i);
                    }
                }
                break;
        }
        return this.population.get(0);
    }

    private MutationType selectMutationMethod(final Network genome) {
        final MutationType mutationMethod = this.mutation[(int) Math.floor(Math.random() * this.mutation.length)];

        if (mutationMethod == MutationType.ADD_NODE && genome.nodes.size() >= this.maxNodes) {
            System.err.println("MaxNodes exceeded!");
            return null;
        }
        if (mutationMethod == MutationType.ADD_CONN && genome.connections.size() >= this.maxConns) {
            System.err.println("MaxConns exceeded!");
            return null;
        }
        if (mutationMethod == MutationType.ADD_GATE && genome.gates.size() >= this.maxGates) {
            System.err.println("MaxGates exceeded!");
            return null;
        }
        return mutationMethod;
    }

    Network getFittest() {
        if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
            this.evaluate();
        }
        if (this.population.get(0).score < this.population.get(1).score) {
            this.sort();
        }
        return this.population.get(0);
    }

    double getAverage() {
        if (Double.isNaN(this.population.get(this.population.size() - 1).score)) {
            this.evaluate();
        }
        return this.population.stream().mapToDouble(network -> network.score).average().orElseThrow();
    }

    JsonObject toJSON() {
        final JsonArray jsonArray = new JsonArray();
        this.population.stream().map(Network::toJSON).forEach(jsonArray::add);
        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("genomes", jsonArray);
        return jsonObject;
    }

    void fromJSON(final JsonObject jsonObject) {
        final JsonArray arr = jsonObject.get("genomes").getAsJsonArray();
        IntStream.range(0, arr.size())
                .forEach(i -> this.population.add(Network.fromJSON(arr.get(i).getAsJsonObject())));
        this.popSize = this.population.size();
    }

}
