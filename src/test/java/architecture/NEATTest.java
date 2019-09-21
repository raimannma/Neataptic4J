package architecture;

import methods.MutationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NEATTest {
    @Test
    void testAND() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        };

        final Network network = new Network(2, 1);
        final EvolveOptions options = new EvolveOptions();
        options.setMutationTypes(MutationType.FFW);
        options.setEqual(true);
        options.setElitism(10);
        options.setMutationRate(0.5);
        options.setError(0.03);
        assertTrue(network.evolve(trainingSet, options) <= 0.03);
    }

    @Test
    void testXOR() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{1}),
                new DataEntry(new double[]{1, 0}, new double[]{1}),
                new DataEntry(new double[]{1, 1}, new double[]{0})
        };

        final Network network = new Network(2, 1);
        final EvolveOptions options = new EvolveOptions();
        options.setMutationTypes(MutationType.FFW);
        options.setEqual(true);
        options.setElitism(10);
        options.setMutationRate(0.5);
        options.setError(0.03);
        assertTrue(network.evolve(trainingSet, options) <= 0.03);
    }

    @Test
    void testXNOR() {
        final DataEntry[] trainingSet = new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{1}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        };

        final Network network = new Network(2, 1);
        final EvolveOptions options = new EvolveOptions();
        options.setMutationTypes(MutationType.FFW);
        options.setEqual(true);
        options.setElitism(10);
        options.setMutationRate(0.5);
        options.setError(0.03);
        assertTrue(network.evolve(trainingSet, options) <= 0.03);
    }
}
