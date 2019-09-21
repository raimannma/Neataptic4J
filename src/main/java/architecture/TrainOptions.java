package architecture;

import methods.Cost;
import methods.Rate;

import static methods.Cost.MSE;

public class TrainOptions {
    private double rate;
    private double error;
    private double dropout;
    private double momentum;
    private int batchSize;
    private Rate ratePolicy;
    private Cost cost;
    private int iterations;
    private boolean crossValidate;
    private int crossValidateTestSize;
    private double crossValidateTestError;
    private boolean clear;
    private boolean shuffle;

    public TrainOptions() {
        this.rate = 0.3;
        this.cost = MSE;
        this.error = 0.05;
        this.dropout = 0;
        this.momentum = 0;
        this.batchSize = 1;
        this.ratePolicy = new Rate.FIXED();
        this.iterations = -1;
        this.error = -1;
        this.crossValidate = false;
        this.crossValidateTestSize = -1;
        this.crossValidateTestError = Double.NaN;
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(final double rate) {
        this.rate = rate;
    }

    double getError() {
        return this.error;
    }

    public void setError(final double error) {
        this.error = error;
    }

    double getDropout() {
        return this.dropout;
    }

    public void setDropout(final double dropout) {
        this.dropout = dropout;
    }

    public double getMomentum() {
        return this.momentum;
    }

    public void setMomentum(final double momentum) {
        this.momentum = momentum;
    }

    int getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(final int batchSize) {
        this.batchSize = batchSize;
    }

    Rate getRatePolicy() {
        return this.ratePolicy;
    }

    public void setRatePolicy(final Rate ratePolicy) {
        this.ratePolicy = ratePolicy;
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

    void setIterations(final int iterations) {
        this.iterations = iterations;
    }

    boolean isCrossValidate() {
        return this.crossValidate;
    }

    public void setCrossValidate(final boolean crossValidate) {
        this.crossValidate = crossValidate;
    }

    int getCrossValidateTestSize() {
        return this.crossValidateTestSize;
    }

    public void setCrossValidateTestSize(final int crossValidateTestSize) {
        this.crossValidateTestSize = crossValidateTestSize;
    }

    double getCrossValidateTestError() {
        return this.crossValidateTestError;
    }

    public void setCrossValidateTestError(final double crossValidateTestError) {
        this.crossValidateTestError = crossValidateTestError;
    }

    public boolean isClear() {
        return this.clear;
    }

    public void setClear(final boolean clear) {
        this.clear = clear;
    }

    boolean isShuffle() {
        return this.shuffle;
    }

    public void setShuffle(final boolean shuffle) {
        this.shuffle = shuffle;
    }
}
