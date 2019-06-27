public class TrainingOptions {
    public Integer iterations;
    public Character error;
    public boolean crossValidate;
    public int crossValidateTestSize;
    public double crossValidateTestError;
    public Boolean clear;
    public boolean shuffle;
    public boolean schedule;
    public int scheduleIterations;
    public int scheduleIteration;
    public double scheduleError;
    public Network network;
    public double scheduleFitness;
    private Boolean withNetwork;
    private Cost cost;
    private Double targetError;
    private Double rate;
    private Double dropout;
    private Double momentum;
    private Rate ratePolicy;
    private Integer batchSize;
    private Double growth;
    private Integer amount;
    private Boolean equal;
    private Integer popSize;
    private Integer elitism;
    private Double provinance;
    private Double mutationRate;
    private Integer mutationAmount;
    private Boolean fitnessPopulation;
    private Selection selection;
    private Crossover[] crossover;
    private Mutation[] mutation;

    public double getTargetError(final double defaultValue) {
        return this.targetError != null ? this.targetError : defaultValue;
    }

    public Cost getCost(final Cost defaultValue) {
        return this.cost != null ? this.cost : defaultValue;
    }

    public double getRate(final double defaultValue) {
        return this.rate != null ? this.rate : defaultValue;
    }

    public double getDropout(final int defaultValue) {
        return this.dropout != null ? this.dropout : defaultValue;
    }

    public double getMomentum(final int defaultValue) {
        return this.momentum != null ? this.momentum : defaultValue;
    }

    public Rate getRatePolicy(final Rate defaultValue) {
        return this.ratePolicy != null ? this.ratePolicy : defaultValue;
    }

    public int getBatchSize(final int defaultValue) {
        return this.batchSize != null ? this.batchSize : defaultValue;
    }

    public void setCost(final Cost cost) {
        this.cost = cost;
    }

    public void setTargetError(final Double targetError) {
        this.targetError = targetError;
    }

    public void setRate(final Double rate) {
        this.rate = rate;
    }

    public void setDropout(final Double dropout) {
        this.dropout = dropout;
    }

    public void setMomentum(final Double momentum) {
        this.momentum = momentum;
    }

    public void setRatePolicy(final Rate ratePolicy) {
        this.ratePolicy = ratePolicy;
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    public void setIterations(final Integer iterations) {
        this.iterations = iterations;
    }

    public double getGrowth(final double defaultValue) {
        return this.growth != null ? this.growth : defaultValue;
    }

    public int getAmount(final int defaultValue) {
        return this.amount != null ? this.amount : defaultValue;
    }

    public boolean getEqual(final boolean defaultValue) {
        return this.equal == null ? defaultValue : this.equal;
    }

    public boolean getClear(final boolean defaultValue) {
        return this.clear == null ? defaultValue : this.clear;
    }

    public int getPopSize(final int defaultValue) {
        return this.popSize == null ? defaultValue : this.popSize;
    }

    public int getElitism(final int defaultValue) {
        return this.elitism == null ? defaultValue : this.elitism;
    }

    public double getProvinance(final double defaultValue) {
        return this.provinance == null ? defaultValue : this.provinance;
    }

    public double getMutationRate(final double defaultValue) {
        return this.mutationRate == null ? defaultValue : this.mutationRate;
    }

    public int getMutationAmount(final int defaultValue) {
        return this.mutationAmount == null ? defaultValue : this.mutationAmount;
    }

    public boolean getFitnessPopulation(final boolean defaultValue) {
        return this.fitnessPopulation == null ? defaultValue : this.fitnessPopulation;
    }

    public Selection getSelection(final Selection defaultValue) {
        return this.selection == null ? defaultValue : this.selection;
    }

    public Crossover[] getCrossover(final Crossover[] defaultValue) {
        return this.crossover == null ? defaultValue : this.crossover;
    }

    public Mutation[] getMutation(final Mutation[] defaultValue) {
        return this.mutation == null ? defaultValue : this.mutation;
    }

    public Network getNetwork(final Network defaultValue) {
        return this.network == null ? defaultValue : this.network;
    }
}
