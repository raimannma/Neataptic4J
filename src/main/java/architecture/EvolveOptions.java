package architecture;

import methods.Cost;
import methods.MutationType;
import methods.SelectionType;

public class EvolveOptions {
    private boolean fitnessPopulation;
    private int populationSize;
    private int elitism;
    private int provenance;
    private double mutationRate;
    private int mutationAmount;
    private SelectionType selection;
    private MutationType[] mutationTypes;
    private Network template;
    private int maxNodes;
    private int maxConns;
    private int maxGates;
    private boolean equal;
    private boolean clear;
    private double error;
    private double growth;
    private int amount;
    private Cost cost;
    private int iterations;
    private Network network;

    public EvolveOptions() {
        this.error = Double.NaN;
        this.growth = 0.0001;
        this.cost = Cost.MSE;
        this.amount = 1;
        this.iterations = -1;
        this.network = null;
        this.clear = false;
        this.populationSize = 50;
        this.elitism = 0;
        this.provenance = 0;
        this.mutationRate = 0.3;
        this.mutationAmount = 1;
        this.selection = SelectionType.POWER;
        this.mutationTypes = MutationType.FFW;
        this.template = null;
        this.maxNodes = Integer.MAX_VALUE;
        this.maxConns = Integer.MAX_VALUE;
        this.maxGates = Integer.MAX_VALUE;
        this.fitnessPopulation = false;
    }

    boolean isFitnessPopulation() {
        return this.fitnessPopulation;
    }

    public void setFitnessPopulation(final boolean fitnessPopulation) {
        this.fitnessPopulation = fitnessPopulation;
    }

    int getPopulationSize() {
        return this.populationSize;
    }

    public void setPopulationSize(final int populationSize) {
        this.populationSize = populationSize;
    }

    int getElitism() {
        return this.elitism;
    }

    public void setElitism(final int elitism) {
        this.elitism = elitism;
    }

    int getProvenance() {
        return this.provenance;
    }

    public void setProvenance(final int provenance) {
        this.provenance = provenance;
    }

    double getMutationRate() {
        return this.mutationRate;
    }

    public void setMutationRate(final double mutationRate) {
        this.mutationRate = mutationRate;
    }

    int getMutationAmount() {
        return this.mutationAmount;
    }

    public void setMutationAmount(final int mutationAmount) {
        this.mutationAmount = mutationAmount;
    }

    SelectionType getSelection() {
        return this.selection;
    }

    public void setSelection(final SelectionType selection) {
        this.selection = selection;
    }

    MutationType[] getMutationTypes() {
        return this.mutationTypes;
    }

    public void setMutationTypes(final MutationType[] mutationTypes) {
        this.mutationTypes = mutationTypes;
    }

    Network getTemplate() {
        return this.template;
    }

    public void setTemplate(final Network template) {
        this.template = template;
    }

    int getMaxNodes() {
        return this.maxNodes;
    }

    public void setMaxNodes(final int maxNodes) {
        this.maxNodes = maxNodes;
    }

    int getMaxConns() {
        return this.maxConns;
    }

    public void setMaxConns(final int maxConns) {
        this.maxConns = maxConns;
    }

    int getMaxGates() {
        return this.maxGates;
    }

    public void setMaxGates(final int maxGates) {
        this.maxGates = maxGates;
    }

    boolean isEqual() {
        return this.equal;
    }

    public void setEqual(final boolean equal) {
        this.equal = equal;
    }

    double getError() {
        return this.error;
    }

    public void setError(final double error) {
        this.error = error;
    }

    double getGrowth() {
        return this.growth;
    }

    public void setGrowth(final double growth) {
        this.growth = growth;
    }

    int getAmount() {
        return this.amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public boolean isClear() {
        return this.clear;
    }

    Cost getCost() {
        return this.cost;
    }

    public void setCost(final Cost cost) {
        this.cost = cost;
    }

    int getIterations() {
        return this.iterations;
    }

    public void setIterations(final int iterations) {
        this.iterations = iterations;
    }

    public Network getNetwork() {
        return this.network;
    }

    void setNetwork(final Network network) {
        this.network = network;
    }

    public boolean getClear() {
        return this.clear;
    }

    public void setClear(final boolean clear) {
        this.clear = clear;
    }
}
