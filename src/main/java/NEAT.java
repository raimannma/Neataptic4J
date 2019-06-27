import enums.Cost;

public class NEAT {
    private final int generation;

    public NEAT(final int input, final int output, final TrainingOptions fitness) {
        this.generation = 0;
    }


    private double fitness(final int amount, final Network genome, final DataSet[] set, final Cost cost, final double growth) {
        double score = 0;
        for (int i = 0; i < amount; i++) {
            score -= genome.test(set, cost);
        }

        score -= (genome.nodes.size() - genome.input - genome.output + genome.connections.size() + genome.gates.size()) * growth;

        return score / amount;
    }

    public int getGeneration() {
        return this.generation;
    }

    public Network evolve() {
        return null;
    }
}
