import enums.Cost;
import enums.Rate;

public class TrainingOptions {
    public Integer iterations;
    public Character error;
    public boolean crossValidate;
    public int crossValidateTestSize;
    public double crossValidateTestError;
    public boolean clear;
    public boolean shuffle;
    public boolean schedule;
    public int scheduleIterations;
    public int scheduleIteration;
    public double scheduleError;
    public Network network;
    public double scheduleFitness;
    private Cost cost;
    private Double targetError;
    private Double rate;
    private Double dropout;
    private Double momentum;
    private Rate ratePolicy;
    private Integer batchSize;
    private Double growth;
    private Integer amount;

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
}
