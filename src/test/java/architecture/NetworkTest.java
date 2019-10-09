package architecture;

import methods.MutationType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class NetworkTest {

    @Test
    void testANDGate() {
        learnSet(new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        }, 1000, 0.002);
    }

    private static void learnSet(final DataEntry[] set, final int iterations, final double error) {
        final Network network = Architect.createPerceptron(set[0].input.length, 5, set[0].output.length);

        final TrainOptions options = new TrainOptions();
        options.setIterations(iterations);
        options.setError(error);
        options.setShuffle(true);
        options.setRate(0.05);
        options.setMomentum(0.9);

        assertTrue(network.train(set, options) <= error);
    }

    @Test
    void testXORGate() {
        learnSet(new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{1}),
                new DataEntry(new double[]{1, 0}, new double[]{1}),
                new DataEntry(new double[]{1, 1}, new double[]{0})
        }, 10000, 0.002);
    }

    @Test
    void testORGate() {
        learnSet(new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{0}),
                new DataEntry(new double[]{0, 1}, new double[]{1}),
                new DataEntry(new double[]{1, 0}, new double[]{1}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        }, 1000, 0.002);
    }

    @Test
    void testSIN() {
        final DataEntry[] set = new DataEntry[100];
        for (int i = 0; i < set.length; i++) {
            final double inputValue = Math.random() * Math.PI * 2;
            set[i] = new DataEntry(
                    new double[]{inputValue / (Math.PI * 2)},
                    new double[]{(Math.sin(inputValue) + 1) / 2}
            );
        }
        learnSet(set, 1000, 0.05);
    }

    @Test
    void testBiggerThan() {
        final DataEntry[] set = new DataEntry[100];
        for (int i = 0; i < set.length; i++) {
            final double x = Math.random();
            final double y = Math.random();
            final double z = x > y ? 1 : 0;
            set[i] = new DataEntry(new double[]{x, y}, new double[]{z});
        }
        learnSet(set, 500, 0.05);
    }

    @Test
    void testGRU_XOR() {
        final Network gru = Architect.createGRU(1, 2, 1);

        final TrainOptions options = new TrainOptions();
        options.setError(0.001);
        options.setIterations(5000);
        options.setRate(0.1);
        options.setClear(true);

        gru.train(new DataEntry[]{
                new DataEntry(new double[]{0}, new double[]{0}),
                new DataEntry(new double[]{1}, new double[]{1}),
                new DataEntry(new double[]{1}, new double[]{0}),
                new DataEntry(new double[]{0}, new double[]{1}),
                new DataEntry(new double[]{0}, new double[]{0}),
        }, options);

        gru.activate(new double[]{0});

        assertTrue(0.9 < gru.activate(new double[]{1})[0], "GRU Error");
        assertTrue(gru.activate(new double[]{1})[0] < 0.1, "GRU Error");
        assertTrue(0.9 < gru.activate(new double[]{0})[0], "GRU Error");
        assertTrue(gru.activate(new double[]{0})[0] < 0.1, "GRU Error");
    }

    @Test
    void testSIN_COS() {
        final DataEntry[] set = new DataEntry[100];
        for (int i = 0; i < set.length; i++) {
            final double inputValue = Math.random() * Math.PI * 2;
            set[i] = new DataEntry(new double[]{inputValue / (Math.PI * 2)}, new double[]{(Math.sin(inputValue) + 1) / 2, (Math.cos(inputValue) + 1) / 2});
        }
        learnSet(set, 1000, 0.05);
    }

    @Test
    void testSHIFT() {
        final DataEntry[] set = new DataEntry[1000];
        for (int i = 0; i < set.length; i++) {
            final double x = Math.random();
            final double y = Math.random();
            final double z = Math.random();
            set[i] = new DataEntry(new double[]{x, y, z}, new double[]{z, x, y});
        }
        learnSet(set, 500, 0.03);
    }

    @Test
    void testNOTGate() {
        learnSet(new DataEntry[]{
                new DataEntry(new double[]{0}, new double[]{1}),
                new DataEntry(new double[]{1}, new double[]{0}),
        }, 1000, 0.002);
    }

    @Test
    void testXNORGate() {
        learnSet(new DataEntry[]{
                new DataEntry(new double[]{0, 0}, new double[]{1}),
                new DataEntry(new double[]{0, 1}, new double[]{0}),
                new DataEntry(new double[]{1, 0}, new double[]{0}),
                new DataEntry(new double[]{1, 1}, new double[]{1})
        }, 10000, 0.002);
    }

    @Test
    void fromToJSON() {
        Network original;
        Network copy;
        original = Architect.createPerceptron((int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = new Network((int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = Architect.createLSTM(new HashMap<>(), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = Architect.createGRU((int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = Architect.createRandom((int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 10 + 1), (int) Math.floor(Math.random() * 5 + 1), new HashMap<>());
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = Architect.createNARX((int) Math.floor(Math.random() * 5 + 1), new int[]{(int) Math.floor(Math.random() * 10 + 1)}, (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1), (int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);

        original = Architect.createHopfield((int) Math.floor(Math.random() * 5 + 1));
        copy = Network.fromJSON(original.toJSON());
        testEquality(original, copy);
    }

    private static void testEquality(final Network original, final Network copied) {
        for (int j = 0; j < 50; j++) {
            final double[] input = IntStream.range(0, original.input).mapToDouble(a -> Math.random()).toArray();
            final double[] ORout = original.activate(input, false);
            final double[] COout = copied.activate(input, false);

            assertArrayEquals(ORout, COout);
        }
    }

    @Test
    void feedForward() {
        final Network network1 = new Network(2, 2);
        final Network network2 = new Network(2, 2);

        for (int i = 0; i < 100; i++) {
            network1.mutate(MutationType.ADD_NODE);
            network2.mutate(MutationType.ADD_NODE);
        }
        for (int i = 0; i < 400; i++) {
            network1.mutate(MutationType.ADD_CONN);
            network2.mutate(MutationType.ADD_CONN);
        }

        final Network network = Network.crossover(network1, network2, false);

        for (int i = 0; i < network.connections.size(); i++) {
            final int from = network.nodes.indexOf(network.connections.get(i).from);
            final int to = network.nodes.indexOf(network.connections.get(i).to);
            assertTrue(from < to, "Network is not feeding forward correctly");
        }
    }

    @Test
    void networkTest() {
        final Network network = Architect.createGRU(2, 4, 4, 4);
    }

    @Test
    void mutate() {
        for (final MutationType mutationType : MutationType.ALL) {
            checkMutation(mutationType);
        }
    }

    private static void checkMutation(final MutationType method) {
        final Network network = Architect.createPerceptron(2, 4, 4, 4, 2);
        network.mutate(MutationType.ADD_GATE);
        network.mutate(MutationType.ADD_BACK_CONN);
        network.mutate(MutationType.ADD_SELF_CONN);

        final List<Double[]> originalOutput = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 10; j++) {
                originalOutput.add(
                        Arrays.stream(network.activate(new double[]{(double) i / 10, (double) j / 10}, false))
                                .boxed()
                                .toArray(Double[]::new));
            }
        }

        network.mutate(method);

        final List<Double[]> mutatedOutput = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            for (int j = 0; j <= 100; j++) {
                mutatedOutput.add(
                        Arrays.stream(network.activate(new double[]{(double) i / 50, (double) j / 50}, false))
                                .boxed()
                                .toArray(Double[]::new));
            }
        }

        final boolean isEqual = Arrays.deepEquals(originalOutput.toArray(Double[][]::new), mutatedOutput.toArray(Double[][]::new));

        assertFalse(isEqual);
    }
}